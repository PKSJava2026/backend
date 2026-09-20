package com.beta.expedition.model;

import com.beta.expedition.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "carrier_applications")
@Getter @Setter @NoArgsConstructor
public class CarrierApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "application_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrier_id")
    private AppUser carrier;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String vehicleInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.NEW;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
