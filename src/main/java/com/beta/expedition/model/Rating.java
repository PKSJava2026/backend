package com.beta.expedition.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Rating {
    private UUID id;
    private UUID orderId;
    private UUID fromUserId;
    private UUID toUserId;
    private short score;
    private String comment;
    private Instant createdAt;
}