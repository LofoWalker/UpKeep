package com.upkeep.domain.model.budget;

public enum Currency {
    EUR("€"),
    USD("$"),
    GBP("£");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
