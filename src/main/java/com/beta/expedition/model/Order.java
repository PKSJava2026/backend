package com.beta.expedition.model;

import com.beta.expedition.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private AppUser customer;

    @Column(nullable = false, columnDefinition = "text")
    private String cargoDescription;

    private BigDecimal weightKg;
    private BigDecimal volumeM3;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    private LocalDate desiredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
