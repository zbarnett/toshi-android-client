/*
 * 	Copyright (c) 2017. Token Browser, Inc
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

package com.tokenbrowser.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tokenbrowser.R;

public class ChatInputView extends LinearLayout {

    public interface OnSendClickedListener {
        void onClick(final String userInput);
    }

    public interface OnAttachmentClickedListener {
        void onClick();
    }

    private OnAttachmentClickedListener attachmentClickedListener;
    private OnSendClickedListener sendClickedListener;

    public ChatInputView(Context context) {
        super(context);
        init();
    }

    public ChatInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ChatInputView setOnSendMessageClicked(final OnSendClickedListener listener) {
        this.sendClickedListener = listener;
        return this;
    }

    public ChatInputView setOnAttachmentClicked(final OnAttachmentClickedListener listener) {
        this.attachmentClickedListener = listener;
        return this;
    }

    private void init() {
        inflate(getContext(), R.layout.view_chat_input, this);
        initClickListener();
        initEditorActionListener();
    }

    private void initClickListener() {
        findViewById(R.id.send_button).setOnClickListener(__ -> handleSendClicked());
        findViewById(R.id.add_attachments_button).setOnClickListener(__ -> this.attachmentClickedListener.onClick());
    }

    private void initEditorActionListener() {
        getInputView().setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleSendClicked();
                return true;
            }
            return false;
        });
    }

    private void handleSendClicked() {
        final String userInput = getText();
        if (userInput.trim().length() == 0) return;
        setText(null);
        this.sendClickedListener.onClick(userInput);
    }

    public EditText getInputView() {
        return (EditText) findViewById(R.id.user_input);
    }

    public String getText() {
        final EditText userInput = (EditText) findViewById(R.id.user_input);
        return userInput.getText().toString();
    }

    public void setText(final String value) {
        final EditText userInput = (EditText) findViewById(R.id.user_input);
        userInput.setText(value);
    }
}
