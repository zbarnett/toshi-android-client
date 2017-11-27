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


import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class HorizontalLineDivider extends RecyclerView.ItemDecoration {

    private final Paint paint;
    private int leftPadding = 0;
    private int rightPadding = 0;
    private int dividerHeight = 6;
    private int startPosition = 0;

    public HorizontalLineDivider(final int color) {
        this.paint = new Paint();
        this.paint.setColor(color);
    }

    public HorizontalLineDivider setLeftPadding(final int leftPadding) {
        this.leftPadding = leftPadding;
        return this;
    }

    public HorizontalLineDivider setRightPadding(final int rightPadding) {
        this.rightPadding = rightPadding;
        return this;
    }

    public HorizontalLineDivider setDividerHeight(final int dividerHeight) {
        this.dividerHeight = dividerHeight;
        return this;
    }

    public HorizontalLineDivider setStartPosition(final int startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    @Override
    public void onDraw(final Canvas canvas, final RecyclerView parent, final RecyclerView.State state) {
        final int dividerLeft = parent.getPaddingLeft() + this.leftPadding;
        final int dividerRight = parent.getWidth() - parent.getPaddingRight() - this.rightPadding;

        for (int i = 0; i < parent.getChildCount() - 1; i++) {
            final View child = parent.getChildAt(i);
            final int childPosition = parent.getChildAdapterPosition(child);
            if (childPosition >= this.startPosition) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int dividerTop = child.getBottom() + params.bottomMargin - (this.dividerHeight / 2);
                final int dividerBottom = dividerTop + (this.dividerHeight / 2);
                canvas.drawRect(dividerLeft, dividerTop, dividerRight, dividerBottom, paint);
            }
        }
    }
}