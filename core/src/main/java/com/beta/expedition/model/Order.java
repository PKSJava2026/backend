package com.beta.expedition.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
public class Order {

    private Long id;
    private Long customerId;
    private String cargoDescription;
    private BigDecimal weightKg;
    private BigDecimal volumeM3;
    private String origin;
    private String destination;
    private LocalDate desiredDate;
    private OrderStatus status = OrderStatus.NEW;
    private OffsetDateTime createdAt;

    public LocalDate getCreatedDate() {
        return createdAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public String toString() {
        return "Заявка #" + id + " [" + status + "] " + origin + " -> " + destination
                + ", груз: " + cargoDescription
                + (weightKg != null ? ", " + weightKg + " кг" : "")
                + (volumeM3 != null ? ", " + volumeM3 + " м3" : "")
                + (desiredDate != null ? ", желаемая дата: " + desiredDate : "")
                + ", заказчик #" + customerId;
    }
}
