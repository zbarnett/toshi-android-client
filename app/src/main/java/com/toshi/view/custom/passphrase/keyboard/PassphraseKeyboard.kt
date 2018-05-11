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
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.toshi.R
import com.toshi.extensions.getColorById
import com.toshi.extensions.getPxSize
import com.toshi.extensions.isVisible
import com.toshi.extensions.next
import com.toshi.view.custom.ForegroundImageView
import com.toshi.view.custom.passphrase.keyboard.keyboardLayouts.KeyboardLayout
import com.toshi.view.custom.passphrase.keyboard.keyboardLayouts.KeyboardLayout.Companion.FIRST_ROW
import com.toshi.view.custom.passphrase.keyboard.keyboardLayouts.KeyboardLayout.Companion.FOURTH_ROW
import com.toshi.view.custom.passphrase.keyboard.keyboardLayouts.KeyboardLayout.Companion.SECOND_ROW
import com.toshi.view.custom.passphrase.keyboard.keyboardLayouts.KeyboardLayout.Companion.THIRD_ROW

class PassphraseKeyboard : LinearLayout {

    companion object {
        const val KEYBOARD_LAYOUT = "keyboardLayout"
        const val SUPER_STATE = "superState"
    }

    private val builder by lazy { PassphraseKeyboardKeyBuilder() }
    private var keyboardLayout = KeyboardLayout.Layout.QWERTY

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
        orientation = VERTICAL
        setBackgroundColor(getColorById(R.color.sectioned_recyclerview_background))
        updateKeyboard()
    }

    private fun updateKeyboard(keyboardLayout: KeyboardLayout = KeyboardLayout.Qwerty()) {
        removeAllViews()
        addRows(keyboardLayout)
    }

    private fun addRows(keyboardLayout: KeyboardLayout) {
        keyboardLayout.getLayout().forEachIndexed { index, row ->
            when (index) {
                FIRST_ROW -> addFirstRow(keyboardLayout, row.list)
                SECOND_ROW -> addSecondRow(keyboardLayout, row.list)
                THIRD_ROW -> addThirdRow(keyboardLayout, row.list)
                FOURTH_ROW -> addFourthRow(keyboardLayout, row.list)
            }
        }
    }

    private fun addFirstRow(keyboardLayout: KeyboardLayout, keys: List<Any>) {
        val firstRow = createAndAddFlexBoxLayout(isFirstRow = true)
        keys.forEach {
            val view = addViewsToParent(firstRow, it)
            val isLastItemOnRow = keyboardLayout.isLastChatOnRow(it)
            builder.setLayoutParams(isLastItemOnRow, view ?: return, it)
        }
    }

    private fun addSecondRow(keyboardLayout: KeyboardLayout, keys: List<Any>) {
        val secondRow = createAndAddFlexBoxLayout(isSecondRow = true)
        keys.forEach {
            val view = addViewsToParent(secondRow, it)
            val isLastItemOnRow = keyboardLayout.isLastChatOnRow(it)
            builder.setLayoutParams(isLastItemOnRow, view ?: return, it)
        }
    }

    private fun addThirdRow(keyboardLayout: KeyboardLayout, keys: List<Any>) {
        val thirdRow = createAndAddFlexBoxLayout()
        keys.forEach {
            val view = addViewsToParent(thirdRow, it)
            val isLastItemOnRow = keyboardLayout.isLastChatOnRow(it)
            builder.setLayoutParams(isLastItemOnRow, view ?: return, it)
        }
    }

    private fun addFourthRow(keyboardLayout: KeyboardLayout, keys: List<Any>) {
        val fourthRow = createAndAddFlexBoxLayout(isLastRow = true)
        keys.forEach {
            val view = addViewsToParent(fourthRow, it)
            val isLastItemOnRow = keyboardLayout.isLastChatOnRow(it)
            builder.setLayoutParams(isLastItemOnRow, view ?: return, it)
        }
    }

    private fun createAndAddFlexBoxLayout(isFirstRow: Boolean = false,
                                          isSecondRow: Boolean = false,
                                          isLastRow: Boolean = false): FlexboxLayout {
        val view = FlexboxLayout(context).apply {
            clipToPadding = false
            gravity = Gravity.CENTER
            addPaddingToFlexboxLayout(this, isFirstRow, isSecondRow, isLastRow)
        }
        addView(view)
        return view
    }

    private fun addPaddingToFlexboxLayout(flexboxLayout: FlexboxLayout,
                                          isFirstRow: Boolean,
                                          isSecondRow: Boolean,
                                          isLastRow: Boolean) {
        val topPadding = if (isFirstRow) getPxSize(R.dimen.margin_three_quarters) else 0
        // Add extra padding on the second row because the second row has less keys than the first row
        val sidePadding =
                if (isSecondRow) getPxSize(R.dimen.margin_half) + (getPxSize(R.dimen.keyboard_key_width) / 2)
                else getPxSize(R.dimen.margin_half)
        val bottomPadding = if (isLastRow) getPxSize(R.dimen.margin_three_quarters) else getPxSize(R.dimen.margin_half)
        flexboxLayout.setPadding(sidePadding, topPadding, sidePadding, bottomPadding)
    }

    private fun addViewsToParent(parent: FlexboxLayout, key: Any): View? {
        val view = when (key) {
            is String -> createTextView(key)
            is KeyboardLayout.Action -> createImageButton(key)
            else -> null
        }
        parent.addView(view ?: return null)
        return view
    }

    private fun createTextView(key: String): TextView {
        return builder.createTextView(context, key).apply {
            setOnClickListener {
                val text = (it as TextView).text
                handleInput(text)
            }
        }
    }

    private fun createImageButton(action: KeyboardLayout.Action): View? {
        return when (action) {
            KeyboardLayout.Action.BACKSPACE -> createBackSpaceView()
            KeyboardLayout.Action.SHIFT -> createShiftView()
            KeyboardLayout.Action.LANGUAGE -> createLanguageView()
            KeyboardLayout.Action.SPACEBAR -> createSpaceBarView()
            KeyboardLayout.Action.RETURN -> createReturnView()
        }
    }

    private fun createBackSpaceView(): ForegroundImageView {
        return builder.createBackSpaceView(context).apply {
            setOnClickListener { handleInput(KeyboardLayout.Action.BACKSPACE) }
        }
    }

    private fun createShiftView() = builder.createShiftView(context)

    private fun createLanguageView(): ForegroundImageView {
        return builder.createLanguageView(context).apply {
            setOnClickListener { changeKeyboardLayout() }
        }
    }

    private fun changeKeyboardLayout() {
        keyboardLayout = keyboardLayout.next()
        updateKeyboard(KeyboardLayout.getLayout(keyboardLayout))
    }

    private fun createSpaceBarView(): TextView {
        return builder.createSpaceBarView(context).apply {
            setOnClickListener { handleInput(KeyboardLayout.Action.SPACEBAR) }
        }
    }

    private fun createReturnView() = builder.createReturnView(context)

    private fun handleInput(input: Any) {
        val focusCurrent = (context as AppCompatActivity).window.currentFocus
        if (focusCurrent == null || focusCurrent.javaClass is EditText) return
        val editText = focusCurrent as EditText
        when (input) {
            is CharSequence -> handleText(editText, input)
            is KeyboardLayout.Action -> handleAction(editText, input)
        }
    }

    private fun handleText(editText: EditText, charSequence: CharSequence) {
        val editable = editText.text
        val start = editText.selectionStart
        editable.insert(start, charSequence)
    }

    private fun handleAction(editText: EditText, action: KeyboardLayout.Action) {
        when (action) {
            KeyboardLayout.Action.BACKSPACE -> handleBackSpace(editText)
            KeyboardLayout.Action.SPACEBAR -> addWhiteSpace(editText)
            else -> { /*Don't do anything with these actions*/ }
        }
    }

    private fun handleBackSpace(editText: EditText) {
        val editable = editText.text
        val lastIndex = editable.length
        if (lastIndex <= 0) dispatchDeleteKeyEvent(editText)
        else editable.delete(lastIndex - 1, lastIndex)
    }

    private fun dispatchDeleteKeyEvent(editText: EditText) {
        val keyEvent = KeyEvent(0L, 0L, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0)
        editText.dispatchKeyEvent(keyEvent)
    }

    private fun addWhiteSpace(editText: EditText) {
        val editable = editText.text
        val start = editText.selectionStart
        editable.insert(start, " ")
    }

    fun showKeyboard() = isVisible(true)

    fun hideKeyboard() = isVisible(false)

    public override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val bundle = state as Bundle?
            keyboardLayout = bundle?.getSerializable(KEYBOARD_LAYOUT) as? KeyboardLayout.Layout
                    ?: KeyboardLayout.Layout.QWERTY
        }
        super.onRestoreInstanceState(state)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putSerializable(KEYBOARD_LAYOUT, keyboardLayout)
        bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState())
        return bundle
    }
}