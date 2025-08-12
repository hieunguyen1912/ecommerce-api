package com.hieu.ecommerce.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hieu.ecommerce.common.anotation.EnumPattern;
import com.hieu.ecommerce.common.enums.Gender;
import com.hieu.ecommerce.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @EnumPattern(name = "status", regexp = "^(ACTIVE|INACTIVE|NONE)$", message = "Status must be one of: ACTIVE, INACTIVE, NONE")
    @Builder.Default
    private UserStatus  status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
}
