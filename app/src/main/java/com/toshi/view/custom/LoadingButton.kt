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
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.toshi.R
import kotlinx.android.synthetic.main.view_loading_button.view.*

class LoadingButton : FrameLayout {

    private var textColor: Int = 0
    private var enabledBackground: Int = 0
    private var disabledBackground: Int = 0
    private var text: String = ""

    constructor(context: Context): super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        parseAttributeSet(context, attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int): super(context, attrs, defStyle) {
        parseAttributeSet(context, attrs)
        init()
    }

    private fun parseAttributeSet(context: Context, attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0)
        textColor = attributes.getColor(R.styleable.LoadingButton_textColor, 0)
        enabledBackground = attributes.getResourceId(R.styleable.LoadingButton_enabledBackground, 0)
        disabledBackground = attributes.getResourceId(R.styleable.LoadingButton_disabledBackground, 0)
        text = attributes.getString(R.styleable.LoadingButton_buttonText)
        attributes.recycle()
    }

    private fun init() {
        inflate(context, R.layout.view_loading_button, this)
        setTextColor()
        setText()
        enablePayButton()
    }

    private fun setTextColor(textColor: Int = this.textColor) = button.setTextColor(textColor)
    private fun setText(text: String = this.text) = button.setText(text)

    fun disablePayButton() {
        button.isClickable = false
        button.setBackgroundResource(disabledBackground)
    }

    fun enablePayButton() {
        button.isClickable = true
        button.setBackgroundResource(enabledBackground)
    }

    fun startLoading() {
        button.isClickable = false
        progressBar.visibility = View.VISIBLE
        button.text = null
        button.setBackgroundResource(enabledBackground)
    }

    fun stopLoading() {
        progressBar.visibility = View.GONE
        button.text = text
    }

    fun setOnButtonClickListener(listener: () -> Unit) = button.setOnClickListener { listener() }
}