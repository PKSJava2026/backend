package com.beta.expedition.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppUser {
    private UUID id;
    private Short roleId;          // вместо объекта Role — просто FK
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String companyName;
    private boolean isActive = true;
    private Instant createdAt;
}