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
import org.spongycastle.util.encoders.Hex;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ByteUtilTest {

    @Test
    public void toZeroPaddedHexStringReturnsCorrectSize() {
        final byte[] unpaddedInput = "Hello".getBytes();
        final int expectedSize = 32;
        final String result = ByteUtil.toZeroPaddedHexString(unpaddedInput, expectedSize);
        assertThat(result.length(), is(expectedSize));

    }

    @Test
    public void toZeroPaddedHexStringDoesNotShortenLongInput() {
        final byte[] unpaddedInput = "Hello".getBytes();
        final int shorterSize = unpaddedInput.length - 1;
        final String result = ByteUtil.toZeroPaddedHexString(unpaddedInput, shorterSize);
        assertThat(result.length(), not(shorterSize));
    }

    @Test
    public void toZeroPaddedHexStringCorrectlyConvertsToHex() {
        final byte[] unpaddedInput = "Hello".getBytes();
        final int size = 32;
        final String asHex = Hex.toHexString(unpaddedInput);

        final String result = ByteUtil.toZeroPaddedHexString(unpaddedInput, size);

        assertThat(result, containsString(asHex));
    }

    @Test
    public void toZeroPaddedHexStringCorrectlyPads() {
        final byte[] unpaddedInput = "Hello".getBytes();
        final int size = 32;
        final String asHex = Hex.toHexString(unpaddedInput);

        final String result = ByteUtil.toZeroPaddedHexString(unpaddedInput, size);
        final String paddedArea = result.substring(0, result.length() - asHex.length());
        final String withoutZero = paddedArea.replace("0", "");

        assertThat(withoutZero.length(), is(0));
    }

    @Test
    public void toZeroPaddedGivesCorrectResult() {
        final byte[] unpaddedInput = "Hello World".getBytes();
        final int size = 64;
        final String expectedOutput = "00000000000000000000000000000000000000000048656c6c6f20576f726c64";

        final String result = ByteUtil.toZeroPaddedHexString(unpaddedInput, size);

        assertThat(result, is(expectedOutput));
    }
}
