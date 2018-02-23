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

import com.toshi.crypto.util.TypeConverter;

import org.junit.Before;
import org.junit.Test;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TypeConverterTest {

    private DecimalFormat df;

    @Before
    public void setup() {
        df = new DecimalFormat(EthUtil.ETH_FORMAT);
        df.setRoundingMode(RoundingMode.DOWN);
    }

    @Test
    public void formatHexToDecimalWith18Decimals() {
        final int decimals = 18;
        final String pattern = EthUtil.ETH_FORMAT;
        final String hexValue = "0x60b414fceda000"; //27219600000000000
        final String decimalValue = TypeConverter.formatHexString(hexValue, decimals, pattern);
        final String expectedValue = df.format(0.0272196);
        assertThat(decimalValue, is(expectedValue));
    }

    @Test
    public void formatHexToDecimalWith16Decimals() {
        final int decimals = 16;
        final String pattern = EthUtil.ETH_FORMAT;
        final String hexValue = "0x60b414fceda000"; //27219600000000000
        final String decimalValue = TypeConverter.formatHexString(hexValue, decimals, pattern);
        final String expectedValue = df.format(2.72196);
        assertThat(decimalValue, is(expectedValue));
    }

    @Test
    public void formatHexToDecimalWith18DecimalsWithoutPattern() {
        final int decimals = 18;
        final String hexValue = "0x60b414fceda000"; //27219600000000000
        final String decimalValue = TypeConverter.formatHexString(hexValue, decimals, null);
        assertThat(decimalValue, is("0.0272196"));
    }

    @Test
    public void formatHexToDecimalWith16DecimalsWithoutPattern() {
        final int decimals = 16;
        final String hexValue = "0x60b414fceda000"; //27219600000000000
        final String decimalValue = TypeConverter.formatHexString(hexValue, decimals, null);
        assertThat(decimalValue, is("2.72196"));
    }

    @Test
    public void formatHexToDecimalWith0Decimals() {
        final int decimals = 0;
        final String hexValue = "0x60b414fceda000"; //27219600000000000
        final String decimalValue = TypeConverter.formatHexString(hexValue, decimals, null);
        assertThat(decimalValue, is("27219600000000000"));
    }
}
