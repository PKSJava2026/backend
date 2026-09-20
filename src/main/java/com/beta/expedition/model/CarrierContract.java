package com.beta.expedition.model;

import com.beta.expedition.model.enums.ContractStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class CarrierContract {
    private UUID id;
    private UUID orderId;
    private UUID forwarderId;
    private UUID carrierId;
    private UUID applicationId;
    private BigDecimal price;
    private String terms;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status = ContractStatus.PENDING;
    private UUID createdById;
    private Instant createdAt;
}