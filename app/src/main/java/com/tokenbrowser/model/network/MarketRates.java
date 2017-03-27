package com.tokenbrowser.model.network;


import java.math.BigDecimal;
import java.util.Map;

public class MarketRates {

    private DataPoints data;

    // ctors
    public MarketRates() {}

    // Returns market rate for a currency or ZERO if
    // there is no data for that currency.
    public BigDecimal getRate(final String currency) {
        if (data == null || data.rates == null) {
            return BigDecimal.ZERO;
        }

        final BigDecimal rate = data.rates.get(currency);
        if (rate == null) {
            return BigDecimal.ZERO;
        }

        return rate;
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static class DataPoints{
        private Map<String, BigDecimal> rates;
    }
}
