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

package com.toshi.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.toshi.R
import com.toshi.extensions.getColorById
import com.toshi.extensions.getPxSize

class ProgressBar : View {

    private val backgroundColor by lazy { getColorById(R.color.web_view_progress_background) }
    private val foregroundColor by lazy { getColorById(R.color.web_view_progress_foreground) }

    private var bgRect: RectF? = null
    private var fgRect: RectF? = null
    private val paint by lazy { Paint() }
    private var progress = 0

    constructor(context: Context): super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        val height = getPxSize(R.dimen.progress_height)
        bgRect = RectF(Rect(0, 0, right, height))
        fgRect = RectF(Rect(0, 0, 0, height))
    }

    override fun onDraw(canvas: Canvas?) {
        // Draw bg
        paint.color = backgroundColor
        canvas?.drawRect(bgRect, paint)

        // Draw fg
        val width = width.toDouble() * (progress.toDouble() / 100)
        paint.color = foregroundColor
        fgRect?.right = width.toFloat()
        canvas?.drawRect(fgRect, paint)

        if (progress == 100) animate()
                .alpha(0f)
                .setDuration(1000)
                .start()

        super.onDraw(canvas)
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }
}