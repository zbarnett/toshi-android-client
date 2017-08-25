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
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.toshi.R;

public class SuggestionInputView extends FrameLayout {

    private String word;

    public SuggestionInputView(@NonNull Context context) {
        super(context);
        init();
    }

    public SuggestionInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuggestionInputView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_suggestion_input, this);
    }

    public void setWordSuggestion(final String suggestion) {
        final EditText suggestionView = findViewById(R.id.suggestion);
        suggestionView.setText(suggestion);
    }

    public void clear() {
        final EditText wordView = findViewById(R.id.word);
        final EditText suggestionView = findViewById(R.id.suggestion);
        wordView.setText("");
        suggestionView.setText("");
    }

    public void clearSuggestion() {
        final EditText suggestionView = findViewById(R.id.suggestion);
        suggestionView.setText("");
        this.word = null;
    }

    public EditText getWordView() {
        return findViewById(R.id.word);
    }

    public EditText getSuggestionView() {
        return findViewById(R.id.suggestion);
    }

    public ShadowTextView getTagView() {
        return findViewById(R.id.tag);
    }

    public void setSuggestionAsWord() {
        final String suggestion = getSuggestionView().getText().toString();
        if (suggestion.length() == 0) return;
        getWordView().setText(suggestion);
        this.word = suggestion;
    }

    public void showTagView(final String word) {
        getWordView().setVisibility(GONE);
        getSuggestionView().setVisibility(GONE);
        getTagView().setVisibility(VISIBLE);
        getTagView().setText(word);
        this.word = word;
    }

    public void showInputView(final String word) {
        getWordView().setVisibility(VISIBLE);
        getSuggestionView().setVisibility(VISIBLE);
        getTagView().setVisibility(GONE);
        getWordView().setText(word);
        this.word = word;
    }

    public String getWord() {
        return this.word;
    }
}
