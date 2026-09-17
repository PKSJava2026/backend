package com.beta.expedition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
}