package com.hieu.ecommerce.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Slf4j
public class AuditConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null && authentication.isAuthenticated() &&
                        !"anonymousUser".equals(authentication.getPrincipal())) {
                    return Optional.of(authentication.getName());
                }
                return Optional.of("SYSTEM");

            } catch (Exception e) {
                log.warn("Could not determine current auditor: {}", e.getMessage());
                return Optional.of("SYSTEM");
            }
        };
    }
}
