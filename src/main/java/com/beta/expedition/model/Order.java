package com.beta.expedition.model;

import com.beta.expedition.model.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Order {
    private UUID id;
    private UUID customerId;
    private String cargoDescription;
    private BigDecimal weightKg;
    private BigDecimal volumeM3;
    private String origin;
    private String destination;
    private LocalDate desiredDate;
    private OrderStatus status = OrderStatus.NEW;
    private Instant createdAt;
}