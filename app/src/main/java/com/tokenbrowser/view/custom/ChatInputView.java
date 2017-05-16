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

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tokenbrowser.R;
import com.tokenbrowser.util.LogUtil;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class ChatInputView extends LinearLayout {

    public interface OnSendClickedListener {
        void onClick(final String userInput);
    }

    public interface OnAttachmentClickedListener {
        void onClick();
    }

    private OnAttachmentClickedListener attachmentClickedListener;
    private OnSendClickedListener sendClickedListener;
    private CompositeSubscription subscriptions;

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
        initLayoutChangeAnimation();
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

    private void initLayoutChangeAnimation() {
        final Animator showAnimation = ObjectAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("scaleX", 1, 0));
        final Animator hideAnimation = ObjectAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("scaleX", 0, 1));
        final LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(50);
        layoutTransition.setAnimator(LayoutTransition.APPEARING, hideAnimation);
        layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, showAnimation);
        setLayoutTransition(layoutTransition);
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.subscriptions = new CompositeSubscription();
        initTextChangesListener();
    }

    private void initTextChangesListener() {
        final EditText userInput = (EditText) findViewById(R.id.user_input);
        final Subscription sub =
                RxTextView.textChanges(userInput)
                .subscribe(
                        this::showOrHideSendButton,
                        throwable -> LogUtil.e(getClass(), "Error when typing")
                );

        this.subscriptions.add(sub);
    }

    private void showOrHideSendButton(final CharSequence charSequence) {
        if (charSequence.length() > 0) {
            showSendButton();
        } else {
            hideSendButton();
        }
    }

    private void showSendButton() {
        final ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);
        sendButton.setVisibility(View.VISIBLE);
        ViewCompat.animate(sendButton)
                .alpha(1f)
                .setDuration(50);
    }

    private void hideSendButton() {
        final ImageButton sendButton = (ImageButton) findViewById(R.id.send_button);
        ViewCompat.animate(sendButton)
                .alpha(0f)
                .setDuration(50)
                .withEndAction(() -> sendButton.setVisibility(View.GONE));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.subscriptions == null) return;
        this.subscriptions.clear();
    }
}
