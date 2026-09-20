package com.beta.expedition.model;

import com.beta.expedition.model.enums.RequestStatus;
import com.beta.expedition.model.enums.RequestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "contract_change_requests")
@Getter @Setter @NoArgsConstructor
public class ContractChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_contract_id")
    private CustomerContract customerContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_contract_id")
    private CarrierContract carrierContract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiated_by")
    private AppUser initiatedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> proposedChanges;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> previousValues;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant resolvedAt;

    @Transient
    public AbstractContract getContract() {
        return customerContract != null ? customerContract : carrierContract;
    }
}
