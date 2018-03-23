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
import android.text.method.KeyListener
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.LinearLayout
import com.toshi.R
import com.toshi.util.KeyboardUtil
import com.toshi.view.adapter.listeners.TextChangedListener
import kotlinx.android.synthetic.main.view_address_bar_input.view.userInput
import kotlinx.android.synthetic.main.view_address_bar_input.view.backButton
import kotlinx.android.synthetic.main.view_address_bar_input.view.forwardButton
import kotlinx.android.synthetic.main.view_address_bar_input.view.closeButton

class AddressBarInputView : LinearLayout {

    var onBackClickedListener: (() -> Unit)? = null
    var onForwardClickedListener: (() -> Unit)? = null
    var onGoClickedListener: ((String) -> Unit)? = null
    var onExitClickedListener: (() -> Unit)? = null
    var onFocusChangedListener: ((Boolean) -> Unit)? = null
    var onTextChangedListener: ((String) -> Unit)? = null

    private var keyListener: KeyListener? = null
    private var skipFirst = true

    var text: String
        get() {
            return userInput.text.toString()
        }
        set(value) {
            skipFirst = true
            userInput.setText(value)
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.view_address_bar_input, this)
        initClickListener()
        initListeners()
    }

    private fun initClickListener() {
        backButton.setOnClickListener { onBackClickedListener?.invoke() }
        forwardButton.setOnClickListener { onForwardClickedListener?.invoke() }
        closeButton.setOnClickListener { onExitClickedListener?.invoke() }
    }

    private fun initListeners() {
        // Store keyListener to toggle editable state which makes ellipsize work
        keyListener = userInput.keyListener
        userInput.setOnFocusChangeListener { _, hasFocus ->
            userInput.keyListener = if (hasFocus) keyListener else null
            onFocusChangedListener?.invoke(hasFocus)
        }
        userInput.setOnKeyListener { _, _, event ->
            val isEnter = event.keyCode == KeyEvent.KEYCODE_ENTER
            val isActionUp = event.action == KeyEvent.ACTION_UP
            if (event != null && isEnter && isActionUp) {
                clearFocus()
                handleGoClicked()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        userInput.addTextChangedListener(object : TextChangedListener() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val skip = skipFirst && s?.isEmpty() == true
                if (!skip) onTextChangedListener?.invoke(s.toString())
                skipFirst = false
            }
        })
    }

    private fun handleGoClicked() {
        val url = userInput.text.toString()
        if (url.trim().isEmpty()) return
        onGoClickedListener?.invoke(url)
    }

    override fun clearFocus() {
        super.clearFocus()
        userInput.clearFocus()
        KeyboardUtil.hideKeyboard(userInput)
    }
}
