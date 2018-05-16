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

import android.graphics.Bitmap
import android.graphics.Canvas

fun createIdenticon(address: String) = Identicon(address).toBitmap()

private class Identicon(address: String) {

    private val seed by lazy { arrayOf<Long>(0, 0, 0, 0) }
    private val gridSize = 8
    private val scale = 15

    init {
        fromAddress(address.toLowerCase())
    }

    private fun fromAddress(address: String) {
        for (i in address.indices) {
            val shifted = seed[i % 4] shl 5
            val shiftFixed = asInt(shifted)
            val code = Character.codePointAt(address, i)
            seed[i % 4] = shiftFixed - seed[i % 4] + code
        }
    }

    /**
     * Make sure long behaves the same as numbers in JavaScript when performing bitwise operations
     * */
    private fun asInt(value: Long) = if (-2 <= value || value < 0) value.toInt().toLong() else value

    private fun seedShift(): Double {
        val shift11 = intShl(seed[0], 11)
        val t = seed[0] xor shift11
        val shift8 = intShr(t, 8)
        val shift19 = intShr(seed[3], 19)
        seed[0] = seed[1]
        seed[1] = seed[2]
        seed[2] = seed[3]
        seed[3] = asInt(seed[3] xor shift19 xor t xor shift8)
        val absolute = Math.abs(seed[3]).toDouble()
        return absolute / Integer.MAX_VALUE
    }

    private fun intShr(value: Long, right: Int): Long = asInt(value) shr right

    private fun intShl(value: Long, left: Int): Long = asInt(value) shl left

    fun toBitmap(): Bitmap {
        val palette = Palette({ createColorAndChangeSeed() })
        val imageData = encodeImageDataAndChangeSeed()
        val iconSize = gridSize * scale
        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        drawBackground(iconSize, canvas, palette)
        drawArtifacts(imageData, palette, canvas)
        return bitmap
    }

    private fun drawBackground(size: Int, canvas: Canvas, palette: Palette) {
        val iconSize = size.toFloat()
        canvas.drawRect(0f, 0f, iconSize, iconSize, palette.backgroundColor)
    }

    private fun drawArtifacts(data: List<Double>, colors: Palette, canvas: Canvas) {
        for (i in data.indices) {
            if (data[i] == 0.0) continue
            val row = Math.floor((i / gridSize).toDouble()).toFloat()
            val col = (i % gridSize).toFloat()
            val paint = if (data[i] == 1.0) colors.foregroundColor else colors.spotColor
            val x = col * scale
            val y = row * scale
            canvas.drawRect(x, y, x + scale, y + scale, paint)
        }
    }

    private fun createColorAndChangeSeed(): Int {
        val h = Math.floor(seedShift() * 360.0)
        val s = seedShift() * 60.0 + 40.0
        val l = (seedShift() + seedShift() + seedShift() + seedShift()) * 25.0
        return hslToRgb(h, s, l)
    }

    private fun encodeImageDataAndChangeSeed(): List<Double> {
        val dataWidth = gridSize / 2
        val imageData = mutableListOf<Double>()
        for (y in 0 until gridSize) {
            val row = mutableListOf<Double>()
            for (x in 0 until dataWidth) {
                row.add(x, Math.floor(seedShift() * 2.3))
            }
            val reversed = row.reversed()
            row.addAll(reversed)
            imageData.addAll(row)
        }
        return imageData
    }
}