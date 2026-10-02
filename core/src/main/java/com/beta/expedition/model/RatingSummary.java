package com.beta.expedition.model;

public record RatingSummary(int count, double average) {

    @Override
    public String toString() {
        return count == 0 ? "оценок пока нет" : String.format("%.2f из 5 (оценок: %d)", average, count);
    }
}
