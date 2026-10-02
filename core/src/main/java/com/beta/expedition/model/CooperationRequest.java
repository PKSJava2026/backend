package com.beta.expedition.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CooperationRequest {

    private Long id;
    private Long carrierId;
    private String description;
    private String vehicleInfo;
    private CooperationStatus status = CooperationStatus.NEW;
    private OffsetDateTime createdAt;

    @Override
    public String toString() {
        return "Заявка о сотрудничестве #" + id + " [" + status + "] перевозчик #" + carrierId
                + ", транспорт: " + vehicleInfo + ", о себе: " + description;
    }
}
