package com.beta.expedition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ShipmentRequest {
    private Long id;
    private Long userId;
    private String cargoDescription;
    private String origin;
    private String destination;
    private RequestStatus status;
    private BigDecimal price;
    private LocalDateTime createdAt;
}