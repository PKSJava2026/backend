package com.beta.expedition.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class User {

    private Long id;
    private String nickname;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active = true;
    private OffsetDateTime createdAt;

    public User(String nickname, String email, String passwordHash, Role role) {
        this.nickname = nickname;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @Override
    public String toString() {
        return "Пользователь #" + id + " " + nickname + " (" + role + (active ? "" : ", неактивен") + ")";
    }
}
