package com.beta.expedition.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarrierContract extends AbstractContract {

    private Long carrierId;

    @Override
    public Long getCounterpartyId() {
        return carrierId;
    }

    @Override
    public String getCounterpartyTitle() {
        return "Перевозчик";
    }
}
