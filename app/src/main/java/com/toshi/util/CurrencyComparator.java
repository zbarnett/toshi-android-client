/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.util;

import com.toshi.model.network.Currency;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CurrencyComparator implements Comparator<Currency> {

    private static final List<String> popularCurrencies = Arrays.asList("USD", "EUR", "CNY", "GBP", "CAD");

    @Override
    public int compare(Currency o1, Currency o2) {
        if (popularCurrencies.contains(o1.getCode())) {
            return -1;
        } else if (popularCurrencies.contains(o2.getCode())) {
            return 1;
        }
        return o1.getName().compareTo(o2.getName());
    }
}
