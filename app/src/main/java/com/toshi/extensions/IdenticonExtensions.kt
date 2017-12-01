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
@file:JvmName("Identicon")
package com.toshi.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

private val size = 8
private val randomSeed by lazy { arrayOf<Long>(0, 0, 0, 0) }

fun String.toIdenticon(): Bitmap {
    val seed = safeLength(this)
    initRandomSeed(seed)
    return createBitmap()
}

fun safeLength(seed: String): String {
    return if (seed.length > randomSeed.size) seed
    else seed + seed + seed + seed
}

private fun initRandomSeed(seed: String) {
    for (i in randomSeed.indices) {
        var shifted = randomSeed[i % 4] shl 5
        if (shifted > Integer.MAX_VALUE shl 1 || shifted < Integer.MIN_VALUE shl 1) shifted = shifted.toInt().toLong()
        val randomised = shifted - randomSeed[i % 4]
        randomSeed[i % 4] = randomised + Character.codePointAt(seed, i)
    }

    for (i in randomSeed.indices)
        randomSeed[i] = randomSeed[i].toInt().toLong()
}

private fun createBitmap(): Bitmap {
    val imgData = encodeImageData()
    val scale = 15
    val canvasWidth = size * scale
    val canvasHeight = size * scale
    val bmp = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.RGB_565)
    val canvas = Canvas(bmp)
    val foregroundColour = createRandomColour()
    val secondaryColour = createRandomColour()
    val paint = generateBackgroundPaint()
    canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), paint)

    for (i in imgData.indices) {
        val row = Math.floor((i / size).toDouble()).toFloat()
        val col = (i % size).toFloat()
        paint.color = if (imgData[i] == 1.0) foregroundColour else secondaryColour

        if (imgData[i] > 0.0) {
            canvas.drawRect(col * scale, row * scale, col * scale + scale, row * scale + scale, paint)
        }
    }
    return bmp
}

private fun generateBackgroundPaint(): Paint {
    val background = createRandomColour()
    return Paint().apply {
        style = Paint.Style.FILL
        color = background
    }
}

private fun encodeImageData(): List<Double> {
    val dataWidth = size / 2
    val imageData = mutableListOf<Double>()
    for (y in 0 until size) {
        val row = mutableListOf<Double>()
        for (x in 0 until dataWidth) {
            row.add(x, Math.floor(rand() * 2.3))
        }
        val reversed = row.reversed()
        row.addAll(reversed)
        imageData.addAll(row)
    }

    return imageData
}

private fun createRandomColour(): Int {
    val h = Math.floor(rand() * 360.0)
    val s = rand() * 60.0 + 40.0
    val l = (rand() + rand() + rand() + rand()) * 25.0
    return toRGB(h, s, l)
}

private fun rand(): Double {
    val shifted = (randomSeed[0] xor (randomSeed[0] shl 11)).toInt()
    randomSeed[0] = randomSeed[1]
    randomSeed[1] = randomSeed[2]
    randomSeed[2] = randomSeed[3]
    randomSeed[3] = randomSeed[3] xor (randomSeed[3] shr 19) xor shifted.toLong() xor (shifted shr 8).toLong()
    val absolute = Math.abs(randomSeed[3]).toDouble()
    return absolute / Integer.MAX_VALUE
}

private fun toRGB(h: Double, s: Double, l: Double): Int {
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

private fun hueToRgb(p: Double, q: Double, h: Double): Double {
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
