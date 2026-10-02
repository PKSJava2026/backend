package com.beta.expedition.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface Contract {

    Long getId();

    Long getOrderId();

    Long getForwarderId();

    Long getCounterpartyId();

    String getCounterpartyTitle();

    BigDecimal getPrice();

    String getTerms();

    LocalDate getStartDate();

    LocalDate getEndDate();

    ContractStatus getStatus();

    Long getCreatedBy();

    OffsetDateTime getCreatedAt();
}
