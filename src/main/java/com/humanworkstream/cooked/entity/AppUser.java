package com.humanworkstream.cooked.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    // Community handle, e.g. @priya.kitchen
    @Column(unique = true)
    private String handle;

    // BCrypt hash; never serialised in responses
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash")
    private String passwordHash;

    @Convert(converter = com.humanworkstream.cooked.converter.UserRoleConverter.class)
    @Column(name = "role", nullable = false)
    private com.humanworkstream.cooked.enumeration.UserRole role = com.humanworkstream.cooked.enumeration.UserRole.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}