package com.beta.expedition.model;

import com.beta.expedition.model.enums.ApplicationStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CarrierApplication {
    private UUID id;
    private UUID carrierId;
    private String description;
    private String vehicleInfo;
    private ApplicationStatus status = ApplicationStatus.NEW;
    private Instant createdAt;
}