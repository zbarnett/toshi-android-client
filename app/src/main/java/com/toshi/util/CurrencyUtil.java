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
import com.toshi.util.logging.LogUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyUtil {

    public static DecimalFormat getNumberFormat() {
        final DecimalFormat numberFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(LocaleUtil.getLocale());
        final DecimalFormatSymbols symbols = numberFormat.getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        numberFormat.setDecimalFormatSymbols(symbols);
        return numberFormat;
    }

    public static DecimalFormat getNumberFormatWithOutGrouping() {
        final DecimalFormat numberFormat = getNumberFormat();
        numberFormat.setGroupingUsed(false);
        return numberFormat;
    }

    public static DecimalFormat getNumberFormatWithOutGrouping(final Locale locale) {
        final DecimalFormat numberFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
        final DecimalFormatSymbols symbols = numberFormat.getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        numberFormat.setDecimalFormatSymbols(symbols);
        numberFormat.setGroupingUsed(false);
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
            LogUtil.exception("Error during getting code from currency", e);
            return currency;
        }
    }

    public static String getSymbol(final String currencyCode) {
        try {
            final Currency currency = Currency.getInstance(currencyCode);
            if (currency.getSymbol().length() > 1) return "";
            return currency.getSymbol();
        } catch (NullPointerException | IllegalArgumentException e) {
            LogUtil.exception("Error during getting symbol from currency", e);
            return "";
        }
    }
}
