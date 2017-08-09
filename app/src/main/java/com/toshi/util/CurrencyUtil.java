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

import com.toshi.exception.CurrencyException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;

public class CurrencyUtil {

    public static DecimalFormat getNumberFormat() {
        final DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(LocaleUtil.getLocale());
        final DecimalFormatSymbols symbols = numberFormat.getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        numberFormat.setDecimalFormatSymbols(symbols);
        return numberFormat;
    }

    public static String getCurrencyFromLocale() throws CurrencyException {
        try {
            final Currency currency = Currency.getInstance(LocaleUtil.getLocale());
            return currency.getCurrencyCode();
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new CurrencyException(new Throwable("Unsupported currency"));
        }
    }

    public static String getCode(final String currency) {
        try {
            return Currency.getInstance(currency).getCurrencyCode();
        } catch (NullPointerException | IllegalArgumentException e) {
            LogUtil.exception(CurrencyUtil.class, "Error during getting code from currency", e);
            return currency;
        }
    }

    public static String getSymbol(final String currencyCode) {
        try {
            final Currency currency = Currency.getInstance(currencyCode);
            if (currency.getSymbol().length() > 1) return "";
            return currency.getSymbol();
        } catch (NullPointerException | IllegalArgumentException e) {
            LogUtil.exception(CurrencyUtil.class, "Error during getting symbol from currency", e);
            return "";
        }
    }
}
