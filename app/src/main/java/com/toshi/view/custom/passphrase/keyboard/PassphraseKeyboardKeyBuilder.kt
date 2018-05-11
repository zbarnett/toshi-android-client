/*
 * Copyright (c) 2017. Toshi Inc
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

package com.toshi.view.custom.passphrase.keyboard

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.toshi.R
import com.toshi.extensions.getColorById
import com.toshi.extensions.getDrawableById
import com.toshi.extensions.getPxSize
import com.toshi.extensions.getString
import com.toshi.view.custom.ForegroundImageView
import com.toshi.view.custom.passphrase.keyboard.keyboardLayouts.KeyboardLayout

class PassphraseKeyboardKeyBuilder {

    fun createBackSpaceView(context: Context): ForegroundImageView {
        return ForegroundImageView(context).apply {
            background = getDrawableById(R.drawable.keyboard_action_key_background)
            setForegroundResource(R.drawable.ic_backspace)
            elevation = getPxSize(R.dimen.elevation_default).toFloat()
        }
    }

    fun createShiftView(context: Context): ForegroundImageView {
        return ForegroundImageView(context).apply {
            setForegroundResource(R.drawable.ic_shift)
            background = getDrawableById(R.drawable.keyboard_action_key_background)
            elevation = getPxSize(R.dimen.elevation_default).toFloat()
        }
    }

    fun createLanguageView(context: Context): ForegroundImageView {
        return ForegroundImageView(context).apply {
            setForegroundResource(R.drawable.ic_language)
            background = getDrawableById(R.drawable.keyboard_action_key_background)
            elevation = getPxSize(R.dimen.elevation_default).toFloat()
        }
    }

    fun createSpaceBarView(context: Context): TextView {
        return TextView(context).apply {
            background = getDrawableById(R.drawable.keyboard_char_key_background)
            gravity = Gravity.CENTER
            text = getString(R.string.next).toLowerCase()
            elevation = getPxSize(R.dimen.elevation_default).toFloat()
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getPxSize(R.dimen.text_size_subtitle).toFloat())
            setTextColor(getColorById(R.color.textColorPrimary))
        }
    }

    fun createReturnView(context: Context): TextView {
        return TextView(context).apply {
            background = getDrawableById(R.drawable.keyboard_action_key_background)
            gravity = Gravity.CENTER
            text = getString(R.string.return_key).toLowerCase()
            elevation = getPxSize(R.dimen.elevation_default).toFloat()
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getPxSize(R.dimen.text_size_subtitle).toFloat())
            setTextColor(getColorById(R.color.keyboard_tex_color_disabled))
        }
    }

    fun createTextView(context: Context, value: String): TextView {
        return TextView(context).apply {
            background = getDrawableById(R.drawable.keyboard_char_key_background)
            gravity = Gravity.CENTER
            text = value
            elevation = getPxSize(R.dimen.elevation_default).toFloat()
            setTextColor(getColorById(R.color.textColorPrimary))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getPxSize(R.dimen.text_size_keyboard).toFloat())
        }
    }

    fun setLayoutParams(isLastItemOnRow: Boolean, view: View, key: Any) {
        when {
            key == KeyboardLayout.Action.BACKSPACE -> setSmallActionViewLayoutParams(view, isLastItemOnRow)
            key == KeyboardLayout.Action.SHIFT -> setSmallActionViewLayoutParams(view, isLastItemOnRow)
            key == KeyboardLayout.Action.LANGUAGE -> setLargeActionViewLayoutParams(view, isLastItemOnRow)
            key == KeyboardLayout.Action.SPACEBAR -> setSpacebarLayoutParams(view, isLastItemOnRow)
            key == KeyboardLayout.Action.RETURN -> setLargeActionViewLayoutParams(view, isLastItemOnRow)
            view is TextView -> setTextViewLayoutParams(view, isLastItemOnRow)
        }
    }

    private fun setSmallActionViewLayoutParams(view: View, isLastItemOnRow: Boolean) {
        val layoutParams = view.layoutParams as FlexboxLayout.LayoutParams
        layoutParams.apply {
            width = view.getPxSize(R.dimen.keyboard_backspace_width)
            height = view.getPxSize(R.dimen.keyboard_key_height)
            flexGrow = 1f
            setMargin(view, layoutParams, isLastItemOnRow)
        }
    }

    private fun setLargeActionViewLayoutParams(view: View, isLastItemOnRow: Boolean) {
        val layoutParams = view.layoutParams as FlexboxLayout.LayoutParams
        layoutParams.apply {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = view.getPxSize(R.dimen.keyboard_key_height)
            flexGrow = 1f
            flexBasisPercent = 0.25f
            setMargin(view, layoutParams, isLastItemOnRow)
        }
    }

    private fun setSpacebarLayoutParams(view: View, isLastItemOnRow: Boolean) {
        val layoutParams = view.layoutParams as FlexboxLayout.LayoutParams
        layoutParams.apply {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = view.getPxSize(R.dimen.keyboard_key_height)
            flexBasisPercent = 0.5f
            setMargin(view, layoutParams, isLastItemOnRow)
        }
    }

    private fun setTextViewLayoutParams(textView: TextView, isLastItemOnRow: Boolean) {
        val layoutParams = textView.layoutParams as FlexboxLayout.LayoutParams
        layoutParams.apply {
            width = textView.getPxSize(R.dimen.keyboard_key_width)
            height = textView.getPxSize(R.dimen.keyboard_key_height)
            flexGrow = 1f
            setMargin(textView, layoutParams, isLastItemOnRow)
        }
    }

    private fun setMargin(view: View, lp: FlexboxLayout.LayoutParams, isLastItemOnRow: Boolean) {
        if (!isLastItemOnRow) lp.marginEnd = view.getPxSize(R.dimen.margin_half)
    }
}