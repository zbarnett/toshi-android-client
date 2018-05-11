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
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import com.toshi.R
import com.toshi.extensions.getDrawableById
import com.toshi.extensions.getPxSize

class ForegroundImageView : ImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int): super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private var foregroundResource: Drawable? = null

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ForegroundImageView)
        val foreground = a.getDrawable(R.styleable.ForegroundImageView_android_foreground)
        if (foreground != null) setForeground(foreground)
        a.recycle()
    }

    /**
     * Supply a drawable resource that is to be rendered on top of all of the child
     * views.
     *
     * @param drawableResId The drawable resource to be drawn on top of the children.
     */
    fun setForegroundResource(drawableResId: Int) {
        foreground = getDrawableById(drawableResId)
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views in the frame layout.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    override fun setForeground(drawable: Drawable?) {
        if (foregroundResource === drawable) return

        if (foregroundResource != null) {
            foregroundResource?.callback = null
            unscheduleDrawable(foregroundResource)
        }

        foregroundResource = drawable

        if (drawable != null) {
            drawable.callback = this
            if (drawable.isStateful) drawable.state = drawableState
        }
        requestLayout()
        invalidate()
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === foregroundResource
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        foregroundResource?.jumpToCurrentState()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (foregroundResource?.isStateful == true) {
            foregroundResource?.state = drawableState
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setBounds(measuredWidth, measuredHeight)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setBounds(w, h)
        invalidate()
    }

    private fun setBounds(width: Int, height: Int) {
        val backspaceWidth = getPxSize(R.dimen.icon_default)
        foregroundResource?.setBounds(0 + backspaceWidth, 0 + backspaceWidth, width / 2, height)

        val intrinsicWidth = foregroundResource?.intrinsicWidth ?: 0
        val intrinsicHeight = foregroundResource?.intrinsicHeight ?: 0

        val drawableWidthCenter = (width / 2) - (intrinsicWidth / 2)
        val drawableHeightCenter = (height / 2) - (intrinsicHeight / 2)

        foregroundResource?.setBounds(
                drawableWidthCenter,
                drawableHeightCenter,
                drawableWidthCenter + intrinsicWidth,
                drawableHeightCenter + intrinsicHeight
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        foregroundResource?.draw(canvas)
    }
}