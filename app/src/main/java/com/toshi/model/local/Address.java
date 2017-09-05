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

package com.toshi.model.local;


import com.toshi.crypto.util.TypeConverter;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.toshi.util.QrCodeParameterName.AMOUNT;
import static com.toshi.util.QrCodeParameterName.MEMO;
import static com.toshi.util.QrCodeParameterName.VALUE;

public class Address {

    private static final String EXTERNAL_URL_PREFIX = "ethereum:";
    private static final String IBAN_URL_PREFIX = "iban:";
    private static final String PARAMETER_VALUE_REGEX = "=([^&]+)*";

    private final String hexAddress;
    private final String amount;
    private final String memo;

    public Address(final String payload) {
        this.amount = parseAmount(payload);
        this.memo = parseMemo(payload);
        this.hexAddress = parseAddress(payload);
    }

    private String parseAmount(final String payload) {
        //The name of the amount parameter could be amount or value
        final String amountResult = parseGeneric(payload, AMOUNT + PARAMETER_VALUE_REGEX, "");
        return !amountResult.isEmpty()
                ? amountResult
                : parseGeneric(payload, VALUE + PARAMETER_VALUE_REGEX, "");
    }

    private String parseMemo(final String payload) {
        return parseGeneric(payload, MEMO + PARAMETER_VALUE_REGEX, "");
    }

    private String parseGeneric(final String payload, final String regex, final String defaultValue) {
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(payload);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return defaultValue;
    }

    private String parseAddress(final String payload) {
        String address = payload.split("\\?")[0];
        address = handleEthereumPrefix(address);
        address = handleIbanPrefix(address);
        address = handleMissingPrefix(address);
        if (!isHex(address.substring(2))) return "";
        if (!isValid(address)) return "";
        return address;
    }

    private String handleEthereumPrefix(final String address) {
        if (!address.startsWith(EXTERNAL_URL_PREFIX)) return address;
        return address.substring(EXTERNAL_URL_PREFIX.length());
    }

    private String handleIbanPrefix(final String address) {
        if (!address.startsWith(IBAN_URL_PREFIX)) return address;
        return convertIbanAddress(address);
    }

    private String convertIbanAddress(final String address) {
        // Remove "iban:XE**"
        final String cleanedAddress = address.substring(9);
        return new BigInteger(cleanedAddress, 36).toString(16);
    }

    private boolean isHex(final String address) {
        try {
            new BigInteger(address, 16);
            return true;
        } catch (final NumberFormatException ex) {
            return false;
        }
    }

    private boolean isValid(final String address) {
        return 40 <= address.length() && address.length() <= 42;
    }

    private String handleMissingPrefix(final String address) {
        if (address.startsWith("0x")) return address;
        return TypeConverter.toJsonHex(address);
    }

    public String getHexAddress() {
        return this.hexAddress;
    }

    public String getAmount() {
        return this.amount;
    }

    public String getMemo() {
        return this.memo;
    }

    public boolean isValid() {
        return !this.hexAddress.isEmpty();
    }
}
