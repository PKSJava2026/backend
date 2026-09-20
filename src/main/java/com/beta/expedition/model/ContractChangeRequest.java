package com.beta.expedition.model;

import com.beta.expedition.model.enums.RequestStatus;
import com.beta.expedition.model.enums.RequestType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ContractChangeRequest {
    private UUID id;
    private UUID customerContractId;   // nullable — одно из двух
    private UUID carrierContractId;    // nullable — одно из двух
    private RequestType type;
    private UUID initiatedById;
    private String proposedChangesJson;   // сырой JSON-текст
    private String previousValuesJson;    // сырой JSON-текст
    private RequestStatus status = RequestStatus.PENDING;
    private Instant createdAt;
    private Instant resolvedAt;
}