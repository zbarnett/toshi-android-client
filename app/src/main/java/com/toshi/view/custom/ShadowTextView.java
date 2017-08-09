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

package com.toshi.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import com.toshi.R;

public class ShadowTextView extends CardView {

    private boolean shadowEnabled;
    private String text;
    private int cornerRadius;

    public ShadowTextView(@NonNull Context context) {
        super(context);
        init();
    }

    public ShadowTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttributeSet(context, attrs);
        init();
    }

    private void parseAttributeSet(final Context context, final @Nullable AttributeSet attrs) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShadowTextView, 0, 0);
        this.shadowEnabled = a.getBoolean(R.styleable.ShadowTextView_shadow, true);
        this.text = a.getString(R.styleable.ShadowTextView_text);
        this.cornerRadius = a.getDimensionPixelSize(R.styleable.ShadowTextView_cornerRadius, 0);
        a.recycle();
    }

    private void init() {
        inflate(getContext(), R.layout.view_shadow_text_view, this);
        initView();
    }

    private void initView() {
        setText(this.text);
        setMaxCardElevation(this.cornerRadius);
        setRadius(this.cornerRadius);
        setShadowEnabled(this.shadowEnabled);
    }

    public ShadowTextView setShadowEnabled(final boolean shadowEnabled) {
        this.shadowEnabled = shadowEnabled;
        if (this.shadowEnabled) enableShadow();
        else disableShadow();
        return this;
    }

    public ShadowTextView setCornerRadius(final float radius) {
        setRadius(radius);
        return this;
    }

    public void enableShadow() {
        this.setCardElevation(4f);
    }

    public void disableShadow() {
        this.setCardElevation(0f);
    }

    public void setText(final String s) {
        final TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setText(s);
    }

    public String getText() {
        final TextView textView = (TextView) findViewById(R.id.text_view);
        return textView.getText().toString();
    }

    public void hideText() {
        final TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    public void showText() {
        final TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setTransformationMethod(null);
    }
}
