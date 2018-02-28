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

@file:JvmName("BigDecimalUtil")
package com.toshi.extensions

import com.toshi.util.CurrencyUtil
import com.toshi.util.EthUtil
import java.math.BigDecimal
import java.text.ParseException
import java.util.Locale

fun isValidDecimal(inputValue: String): Boolean {
    return try {
        parseValue(inputValue)
        true
    } catch (e: ParseException) {
        false
    }
}

fun createSafeBigDecimal(inputValue: String): BigDecimal {
    return try {
        parseValue(inputValue)
    } catch (e: ParseException) {
        return BigDecimal("0")
    }
}

@Throws(ParseException::class)
private fun parseValue(inputValue: String): BigDecimal {
    // BigDecimal doesn't handle ",", so force the locale to ENGLISH and replace , with .
    val df = CurrencyUtil.getNumberFormatWithOutGrouping(Locale.ENGLISH)
    df.maximumFractionDigits = EthUtil.BIG_DECIMAL_SCALE
    df.isParseBigDecimal = true
    val safeInput = inputValue.replace(",", ".")
    return df.parse(safeInput) as BigDecimal
}