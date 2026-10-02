package com.beta.expedition.model;

public enum ContractKind {
    CUSTOMER("с заказчиком", "Заказчик"),
    CARRIER("с перевозчиком", "Перевозчик");

    private final String title;
    private final String counterparty;

    ContractKind(String title, String counterparty) {
        this.title = title;
        this.counterparty = counterparty;
    }

    public String getTitle() {
        return title;
    }

    public String getCounterparty() {
        return counterparty;
    }
}
