package com.beta.expedition.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ContractChangeRequest {

    private Long id;
    private ContractKind kind;
    private Long contractId;
    private ChangeRequestType type;
    private Long initiatedBy;
    private BigDecimal proposedPrice;
    private String proposedTerms;
    private LocalDate proposedStartDate;
    private LocalDate proposedEndDate;
    private ChangeRequestStatus status = ChangeRequestStatus.PENDING;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("Запрос #" + id + " [" + status + "] "
                + (type == ChangeRequestType.AMEND ? "изменение" : "расторжение")
                + " договора " + kind.getTitle() + " #" + contractId + ", инициатор #" + initiatedBy);
        if (type == ChangeRequestType.AMEND) {
            text.append(", новые значения:");
            if (proposedPrice != null) text.append(" цена=").append(proposedPrice);
            if (proposedTerms != null) text.append(" условия='").append(proposedTerms).append("'");
            if (proposedStartDate != null) text.append(" начало=").append(proposedStartDate);
            if (proposedEndDate != null) text.append(" конец=").append(proposedEndDate);
        }
        return text.toString();
    }
}
