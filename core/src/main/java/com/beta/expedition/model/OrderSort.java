package com.beta.expedition.model;

import java.math.BigDecimal;
import java.util.Comparator;

public enum OrderSort {
    DATE("по дате создания"),
    STATUS("по статусу"),
    WEIGHT("по весу (заявки без веса в конце)");

    private final String title;

    OrderSort(String title) {
        this.title = title;
    }

    public Comparator<Order> comparator(boolean descending) {
        if (this == WEIGHT) {
            Comparator<BigDecimal> weights = descending ? Comparator.reverseOrder() : Comparator.naturalOrder();
            return Comparator.comparing(Order::getWeightKg, Comparator.nullsLast(weights));
        }
        Comparator<Order> ascending = this == DATE
                ? Comparator.comparing(Order::getCreatedAt)
                : Comparator.comparing(Order::getStatus);
        return descending ? ascending.reversed() : ascending;
    }

    @Override
    public String toString() {
        return title;
    }
}
