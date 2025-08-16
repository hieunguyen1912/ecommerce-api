package com.hieu.ecommerce.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.*;

import static com.hieu.ecommerce.service.impl.AuthServiceImpl.JWT_ALGORITHM;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret-key}")
    private String jwtSigningKey;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

     @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
         http.csrf(AbstractHttpConfigurer::disable)
             .authorizeHttpRequests(auth -> auth
                 .requestMatchers("/api/v1/users/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                 .anyRequest().permitAll())
             .oauth2ResourceServer(oauth2 -> oauth2
                         .jwt(jwt -> jwt
                             .jwtAuthenticationConverter(jwtAuthenticationConverter())
                             .decoder(jwtDecoder())
                         )
                         .authenticationEntryPoint(customAuthenticationEntryPoint)
                     .accessDeniedHandler(customAccessDeniedHandler)
             )
             .sessionManagement(session -> session
                         .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
             );
         return http.build();
     }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(getSecretKey()).macAlgorithm(JWT_ALGORITHM).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        System.out.println("Configuring JwtAuthenticationConverter with custom authorities converter");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        converter.setPrincipalClaimName("sub");
        return converter;
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        System.out.println("Creating JwtGrantedAuthoritiesConverter to extract roles and permissions");
        return jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();

            // Roles với prefix ROLE_
            Collection<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }

            // Permissions không có prefix
            Collection<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            }

            return authorities;
        };
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtSigningKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}
