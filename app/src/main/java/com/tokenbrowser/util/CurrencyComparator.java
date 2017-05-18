package com.tokenbrowser.util;

import com.tokenbrowser.model.network.Currency;

import java.util.Comparator;

public class CurrencyComparator implements Comparator<Currency> {
    @Override
    public int compare(Currency o1, Currency o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
