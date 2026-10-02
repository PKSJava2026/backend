package com.beta.expedition.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Setter
public abstract class AbstractContract implements Contract {

    private Long id;
    private Long orderId;
    private Long forwarderId;
    private BigDecimal price;
    private String terms;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status = ContractStatus.PENDING;
    private Long createdBy;
    private OffsetDateTime createdAt;

    public LocalDate getCreatedDate() {
        return createdAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public String toString() {
        return "Договор #" + id + " [" + status + "] " + getCounterpartyTitle() + " #" + getCounterpartyId()
                + ", экспедитор #" + forwarderId + ", заявка #" + orderId + ", цена: " + price
                + (startDate != null ? ", с " + startDate : "")
                + (endDate != null ? " по " + endDate : "")
                + (terms != null ? ", условия: " + terms : "");
    }
}
