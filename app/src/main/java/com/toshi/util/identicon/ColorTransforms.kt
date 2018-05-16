/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 *  This program is free software: you can redistribute it and/or modify
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

package com.toshi.util.identicon

import android.graphics.Color

fun hslToRgb(h: Double, s: Double, l: Double): Int {
    val hue = (h % 360.0f) / 360f
    val saturation = s / 100f
    val lightness = l / 100f

    val q = if (lightness < 0.5) lightness * (1 + saturation)
    else lightness + saturation - saturation * lightness
    val p = 2 * lightness - q
    var r = Math.max(0.0, hueToRgb(p, q, hue + 1.0f / 3.0f))
    var g = Math.max(0.0, hueToRgb(p, q, hue))
    var b = Math.max(0.0, hueToRgb(p, q, hue - 1.0f / 3.0f))

    r = Math.min(r, 1.0)
    g = Math.min(g, 1.0)
    b = Math.min(b, 1.0)

    val red = (r * 255).toInt()
    val green = (g * 255).toInt()
    val blue = (b * 255).toInt()
    return Color.rgb(red, green, blue)
}

fun hueToRgb(p: Double, q: Double, h: Double): Double {
    var normalisedH = h
    if (normalisedH < 0) normalisedH += 1f
    if (normalisedH > 1) normalisedH -= 1f
    if (6 * normalisedH < 1) {
        return p + (q - p) * 6f * normalisedH
    }
    if (2 * normalisedH < 1) {
        return q
    }
    if (3 * normalisedH < 2) {
        return p + (q - p) * 6f * (2.0f / 3.0f - normalisedH)
    }
    return p
}