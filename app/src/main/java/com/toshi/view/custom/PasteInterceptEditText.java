package com.toshi.view.custom;

import android.content.Context;
import android.util.AttributeSet;

public class PasteInterceptEditText extends android.support.v7.widget.AppCompatEditText {

    public interface OnPasteListener {
        void onPaste(final String pastedString);
    }

    private OnPasteListener pasteListener;

    public PasteInterceptEditText(Context context) {
        super(context);
    }

    public PasteInterceptEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PasteInterceptEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PasteInterceptEditText setOnPasteListener(final OnPasteListener listener) {
        this.pasteListener = listener;
        return this;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);

        switch (id){
            case android.R.id.paste:
                onTextPasted();
                break;
        }
        return consumed;
    }

    private void onTextPasted() {
        this.pasteListener.onPaste(getText().toString());
    }
}
