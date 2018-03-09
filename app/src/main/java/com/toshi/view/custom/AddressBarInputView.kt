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
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import com.toshi.R
import com.toshi.util.KeyboardUtil
import kotlinx.android.synthetic.main.view_address_bar_input.view.*

class AddressBarInputView : LinearLayout {

    var backClickedListener: (() -> Unit)? = null
    var forwardClickedListener: (() -> Unit)? = null
    var goClickedListener: ((String) -> Unit)? = null
    var exitClickedListener: (() -> Unit)? = null

    var text: String
        get() {
            return userInput.text.toString()
        }
        set(value) {
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
        backButton.setOnClickListener { backClickedListener?.invoke() }
        forwardButton.setOnClickListener { forwardClickedListener?.invoke() }
        closeButton.setOnClickListener { exitClickedListener?.invoke() }
    }

    private fun initListeners() {
        userInput.setOnEditorActionListener { view, actionId, event ->
            onEditorAction(event, actionId, view)
        }
    }

    private fun onEditorAction(event: KeyEvent?, actionId: Int, view: TextView): Boolean {
        return when {
            event != null && event.action != KeyEvent.ACTION_DOWN -> false
            actionId == EditorInfo.IME_ACTION_GO -> {
                view.clearFocus()
                KeyboardUtil.hideKeyboard(view)
                handleGoClicked()
                true
            }
            else -> false
        }
    }

    private fun handleGoClicked() {
        val userInput = userInput.text.toString()
        if (userInput.trim().isEmpty()) return
        goClickedListener?.invoke(userInput)
    }
}
