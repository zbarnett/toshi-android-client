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

package com.toshi.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.toshi.R;
import com.toshi.model.local.ToshiEntity;
import com.toshi.util.ImageUtil;
import com.toshi.util.ScreenUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.adapter.listeners.OnItemClickListener;
import com.toshi.view.custom.StarRatingView;

import de.hdodenhof.circleimageview.CircleImageView;

public class HorizontalViewHolder<T extends ToshiEntity> extends RecyclerView.ViewHolder {
    private static final double MINIMAL_SCREEN_RATIO = 1.4;

    private TextView appLabel;
    private StarRatingView ratingView;
    private CircleImageView appImage;
    private View container;

    public HorizontalViewHolder(View itemView) {
        super(itemView);

        this.appLabel = itemView.findViewById(R.id.app_label);
        this.ratingView = itemView.findViewById(R.id.rating_view);
        this.appImage = itemView.findViewById(R.id.app_image);
        this.container = itemView.findViewById(R.id.container);
    }

    public HorizontalViewHolder setElement(final T elem) {
        setContainerSize();
        if (elem != null) {
            renderName(elem);
            loadImage(elem);
        }
        return this;
    }

    private void renderName(final T elem) {
        this.appLabel.setText(elem.getDisplayName());
        this.ratingView.setStars(elem.getAverageRating());
    }

    private void setContainerSize() {
        final int avatarSize = getContainerSize();
        this.container.getLayoutParams().width = avatarSize;
        this.appImage.getLayoutParams().width = avatarSize;
        this.appImage.getLayoutParams().height = avatarSize;
        this.container.requestLayout();
    }

    private int getContainerSize() {
        final int screenWidth = ScreenUtil.getWidthOfScreen();
        final int horizontalMargin = this.appImage.getContext().getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final float numberOfVisibleApps = getNumberOfVisibleApps();
        return (int)(screenWidth / numberOfVisibleApps) - horizontalMargin;
    }

    private float getNumberOfVisibleApps() {
        final int resourceId = ScreenUtil.isPortrait() || ScreenUtil.getScreenRatio() < MINIMAL_SCREEN_RATIO
                ? R.dimen.num_horizontal_items__portrait
                : R.dimen.num_horizontal_items__landscape;
        final TypedValue actualValue = new TypedValue();
        BaseApplication.get().getResources().getValue(resourceId, actualValue, true);
        return actualValue.getFloat();
    }

    private void loadImage(final T elem) {
        ImageUtil.load(elem.getAvatar(), this.appImage);
    }

    public HorizontalViewHolder setOnClickListener(final OnItemClickListener<ToshiEntity> listener,
                                                   final ToshiEntity elem) {
        this.itemView.setOnClickListener(__ -> listener.onItemClick(elem));
        return this;
    }
}
