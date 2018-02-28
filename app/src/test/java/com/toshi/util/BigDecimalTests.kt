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

package com.toshi.util

import com.toshi.extensions.createSafeBigDecimal
import com.toshi.extensions.isValidDecimal
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Locale

class BigDecimalTests {

    @Test
    fun checkIfAllLocalesAreValid() {
        val locales = Locale.getAvailableLocales()
        for (locale in locales) {
            val df = CurrencyUtil.getNumberFormatWithOutGrouping(locale)
            df.maximumFractionDigits = 8
            val valueList = getListOfValues()
            for (value in valueList) {
                val formattedValue = getFormattedValue(df, value)
                assertThat(isValidDecimal(formattedValue), `is`(true))
            }
        }
    }

    private fun getFormattedValue(df: DecimalFormat, value: Double) = df.format(value).trim()

    @Test
    fun createBigDecimalInAllLocalesAndCheckIfValid() {
        val locales = Locale.getAvailableLocales()
        for (locale in locales) {
            val df = CurrencyUtil.getNumberFormatWithOutGrouping(locale)
            df.maximumFractionDigits = 8
            val valueList = getListOfValues()
            for (value in valueList) {
                val safeBigDecimal = createSafeBigDecimal(getFormattedValue(df, value))
                assertThat(safeBigDecimal, not(BigDecimal.ZERO))
            }
        }
    }

    private fun getListOfValues(): List<Double> {
        return listOf(0.00000001, 0.0000001, 0.000001, 0.00001, 0.0001, 0.001, 0.01, 0.1, 1.00, 10.00, 100.00, 1000.00, 1000000.00, 1000000000.00, 1000000000000.00)
    }
}