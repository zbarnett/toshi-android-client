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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DraggableShadowTextView extends ShadowTextView {

    private float touchDownX;
    private float touchDownY;
    private boolean isClicked;
    private ClickAndDragListener listener;


    public interface ClickAndDragListener {
        void onClick(DraggableShadowTextView v);
        void onDrag(DraggableShadowTextView v);
    }

    public DraggableShadowTextView(@NonNull Context context) {
        super(context);
    }

    public DraggableShadowTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(final ClickAndDragListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                this.touchDownX = ev.getX();
                this.touchDownY = ev.getY();
                this.isClicked = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isClicked) {
                    if (this.listener != null) this.listener.onClick(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float SCROLL_THRESHOLD = 10;
                if (this.isClicked && (Math.abs(touchDownX - ev.getX()) > SCROLL_THRESHOLD || Math.abs(touchDownY - ev.getY()) > SCROLL_THRESHOLD)) {
                    if (this.listener != null) this.listener.onDrag(this);
                    isClicked = false;
                }
                break;
            default:
                break;
        }
        return true;
    }
}
