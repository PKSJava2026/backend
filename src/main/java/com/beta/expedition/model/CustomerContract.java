package com.beta.expedition.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_contracts")
@Getter @Setter @NoArgsConstructor
public class CustomerContract extends AbstractContract {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private AppUser customer;

    @Override
    public AppUser getCounterparty() {
        return customer;
    }
}
