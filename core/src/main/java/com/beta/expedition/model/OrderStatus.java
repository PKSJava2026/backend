package com.beta.expedition.model;

public enum OrderStatus {
    NEW,
    CONTRACTED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case NEW -> next == CONTRACTED || next == CANCELLED;
            case CONTRACTED -> next == IN_TRANSIT || next == CANCELLED;
            case IN_TRANSIT -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
