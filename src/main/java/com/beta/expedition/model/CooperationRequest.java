package com.beta.expedition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CooperationRequest {
    private Long id;
    private Long carrierId;
    private String description;
    private CooperationStatus status;
    private LocalDateTime createdAt;
}