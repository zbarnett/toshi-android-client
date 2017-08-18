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

    private PasteInterceptEditText.OnPasteListener pasteListener;

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

    public SuggestionInputView setOnPasteListener(final PasteInterceptEditText.OnPasteListener listener) {
        this.pasteListener = listener;
        addPasteListener();
        return this;
    }

    private void addPasteListener() {
        final PasteInterceptEditText wordView = findViewById(R.id.word);
        wordView.setOnPasteListener(pastedString -> {
            clear();
            this.pasteListener.onPaste(pastedString);
        });
    }

    public void setWord(final String word) {
        final EditText wordView = findViewById(R.id.word);
        wordView.setText(word);
    }

    public void setSuggestion(final String suggestion) {
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
    }

    public EditText getWordView() {
        return findViewById(R.id.word);
    }

    public EditText getSuggestioniew() {
        return findViewById(R.id.suggestion);
    }
}
