package com.beta.expedition.model;

import java.util.Comparator;

public enum ContractSort {
    DATE("по дате создания", Comparator.comparing(AbstractContract::getCreatedAt)),
    STATUS("по статусу", Comparator.comparing(AbstractContract::getStatus)),
    PRICE("по сумме", Comparator.comparing(AbstractContract::getPrice));

    private final String title;
    private final Comparator<AbstractContract> comparator;

    ContractSort(String title, Comparator<AbstractContract> comparator) {
        this.title = title;
        this.comparator = comparator;
    }

    public Comparator<AbstractContract> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return title;
    }
}
