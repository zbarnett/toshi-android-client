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
import com.toshi.extensions.isVisible
import com.toshi.util.KeyboardUtil
import com.toshi.view.adapter.listeners.TextChangedListener
import kotlinx.android.synthetic.main.view_address_bar_input.view.userInput
import kotlinx.android.synthetic.main.view_address_bar_input.view.backButton
import kotlinx.android.synthetic.main.view_address_bar_input.view.forwardButton
import kotlinx.android.synthetic.main.view_address_bar_input.view.closeButton
import kotlinx.android.synthetic.main.view_address_bar_input.view.addressInput
import kotlinx.android.synthetic.main.view_address_bar_input.view.addressPrefix
import kotlinx.android.synthetic.main.view_address_bar_input.view.addressField
import kotlinx.android.synthetic.main.view_address_bar_input.view.address
import kotlinx.android.synthetic.main.view_address_bar_input.view.backArrowButton

class AddressBarInputView : LinearLayout {

    var onBackClickedListener: (() -> Unit)? = null
    var onForwardClickedListener: (() -> Unit)? = null
    var onGoClickedListener: ((String) -> Unit)? = null
    var onExitClickedListener: (() -> Unit)? = null
    var onFocusChangedListener: ((Boolean) -> Unit)? = null
    var onTextChangedListener: ((String) -> Unit)? = null

    private var keyListener: KeyListener? = null
    private var skipFirst = true
    private var editTextVisible = false

    var url: String
        get() {
            return "${addressPrefix.text}${address.text}"
        }
        set(value) {
            skipFirst = true
            setStaticUrl(value)
            if (isAddressInputVisible()) copyAddressBarValueToEditText()
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
        addressField.setOnClickListener { showAddressInput() }
        addressField.setOnLongClickListener { showAddressInput() }
        closeButton.setOnClickListener { onExitClickedListener?.invoke() }
        backArrowButton.setOnClickListener { clearFocus() }
    }

    private fun showAddressInput(): Boolean {
        addressInput.isVisible(true)
        editTextVisible = true
        copyAddressBarValueToEditText()
        return true
    }

    private fun copyAddressBarValueToEditText() {
        userInput.setText(url)
        selectAll()
    }

    private fun selectAll() {
        userInput.requestFocus()
        KeyboardUtil.showKeyboard(userInput)
        userInput.selectAll()
    }

    private fun hideAddressInput() {
        addressInput.isVisible(false)
        editTextVisible = false
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

    override fun dispatchKeyEventPreIme(event: KeyEvent?): Boolean {
        val actionUp = event?.action == KeyEvent.ACTION_UP
        val keyCodeBack = event?.keyCode == KeyEvent.KEYCODE_BACK
        // Hide EditText when keyboard is being closed
        if (isAddressInputVisible() && actionUp && keyCodeBack) hideAddressInput()
        return super.dispatchKeyEventPreIme(event)
    }

    private fun handleGoClicked() {
        url = userInput.text.toString()
        if (url.trim().isEmpty()) return
        onGoClickedListener?.invoke(url)
        hideAddressInput()
    }

    private fun setStaticUrl(url: String) {
        val regex = "^(https?://)?(.+)".toRegex()
        val matchResult = regex.find(url)
        val matchGroups = matchResult?.groups ?: return
        if (matchGroups.size < 3) return
        addressPrefix.text = matchGroups[1]?.value.orEmpty()
        address.text = matchGroups[2]?.value.orEmpty()
    }

    override fun clearFocus() {
        super.clearFocus()
        userInput.clearFocus()
        KeyboardUtil.hideKeyboard(userInput)
        hideAddressInput()
    }

    fun isAddressInputVisible() = editTextVisible
}
