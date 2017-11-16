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

package com.toshi.crypto.util;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TypeConverterTest {

    @Test
    public void stringHexToBigIntegerReturnsZeroIfCalledWithNull() {
        final BigInteger expected = BigInteger.ZERO;
        final BigInteger actual = TypeConverter.StringHexToBigInteger(null);
        assertThat(actual, is(expected));
    }

    @Test
    public void stringHexToBigIntegerConvertsCorrectly() {
        // 1000000000 == 0x3B9ACA00
        final BigInteger expected = BigInteger.valueOf(1000000000L);
        final BigInteger actual = TypeConverter.StringHexToBigInteger("0x3B9ACA00");
        assertThat(actual, is(expected));
    }

    @Test
    public void stringHexToBigIntegerConvertsCorrectlyWithMissingHexPrefix() {
        // 1000000000 == 3B9ACA00
        final BigInteger expected = BigInteger.valueOf(1000000000L);
        final BigInteger actual = TypeConverter.StringHexToBigInteger("3B9ACA00");
        assertThat(actual, is(expected));
    }

    @Test
    public void stringHexToBigIntegerReturnZeroIfCalledWithInvalidHex() {
        final BigInteger expected = BigInteger.ZERO;
        final BigInteger actual = TypeConverter.StringHexToBigInteger("notHex");
        assertThat(actual, is(expected));
    }

    @Test
    public void jsonStringToStringWithStringReturnsString() {
        final String expected = "just a string";
        final String actual = TypeConverter.jsonStringToString(expected);
        assertThat(expected, is(actual));
    }

    @Test
    public void jsonStringToStringWithJsonStringReturnsString() {
        final String expected = "just a string";
        final String input = "\"" + expected + "\"";
        final String actual = TypeConverter.jsonStringToString(input);
        assertThat(expected, is(actual));
    }

    @Test
    public void jsonStringWithMissingClosingQuoteReturnsString() {
        final String expected = "just a string";
        final String input = "\"" + expected;
        final String actual = TypeConverter.jsonStringToString(input);
        assertThat(expected, is(actual));
    }

    @Test
    public void jsonStringWithMissingOpeningQuoteReturnsString() {
        final String expected = "just a string";
        final String input = expected + "\"";
        final String actual = TypeConverter.jsonStringToString(input);
        assertThat(expected, is(actual));
    }

    public void skeletonAndSignatureToRLPEncodedHexEncodesCorrectly() throws Exception {
        final String expected = "0xf8af85746f6b6682832dc6c0832dc6c094dc0a63a5bdb165640661709569816bf08594dfd780b844a9059cbb0000000000000000000000002278562760cf038cb33b7b405c295a4c50db4fdd00000000000000000000000000000000000000000000000000000002540be40082010ca0a343b0140fb8497e86a37e3b4a8329616675cdefed2e3e29654425bd10d749d7a04c22ba969436fced1d5d41d48a1c9e11f059caf54ac5704fa4443ec0aa5cc989";
        final String skeleton = "0xf86d85746f6b6682832dc6c0832dc6c094dc0a63a5bdb165640661709569816bf08594dfd780b844a9059cbb0000000000000000000000002278562760cf038cb33b7b405c295a4c50db4fdd00000000000000000000000000000000000000000000000000000002540be400748080";
        final String signature = "0xa343b0140fb8497e86a37e3b4a8329616675cdefed2e3e29654425bd10d749d74c22ba969436fced1d5d41d48a1c9e11f059caf54ac5704fa4443ec0aa5cc98901";
        final String actual = TypeConverter.skeletonAndSignatureToRLPEncodedHex(skeleton, signature);
        assertThat(actual, is(expected));
    }
}
