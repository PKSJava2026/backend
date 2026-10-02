package com.beta.expedition.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerContract extends AbstractContract {

    private Long customerId;

    @Override
    public Long getCounterpartyId() {
        return customerId;
    }

    @Override
    public String getCounterpartyTitle() {
        return "Заказчик";
    }
}
