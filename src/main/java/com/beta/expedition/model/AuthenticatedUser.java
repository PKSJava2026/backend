package com.beta.expedition.model;

import com.beta.expedition.model.enums.RoleName;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String username, RoleName role) {
}