package com.toshi.util;

import com.toshi.model.network.Currency;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CurrencyComparator implements Comparator<Currency> {

    private static final List<String> popularCurrencies = Arrays.asList("USD", "EUR", "CNY", "GBP", "CAD");

    @Override
    public int compare(Currency o1, Currency o2) {
        if (popularCurrencies.contains(o1.getId())) {
            return -1;
        } else if (popularCurrencies.contains(o2.getId())) {
            return 1;
        }
        return o1.getName().compareTo(o2.getName());
    }
}
