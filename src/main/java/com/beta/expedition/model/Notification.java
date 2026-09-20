package com.beta.expedition.model;

import com.beta.expedition.model.enums.NotificationType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notification {
    private UUID id;
    private UUID userId;
    private NotificationType type;
    private UUID requestId;
    private String message;
    private boolean isRead = false;
    private Instant createdAt;
}