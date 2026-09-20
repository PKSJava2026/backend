package com.beta.expedition.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carrier_contracts")
@Getter @Setter @NoArgsConstructor
public class CarrierContract extends AbstractContract {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrier_id")
    private AppUser carrier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private CarrierApplication application;

    @Override
    public AppUser getCounterparty() {
        return carrier;
    }
}
