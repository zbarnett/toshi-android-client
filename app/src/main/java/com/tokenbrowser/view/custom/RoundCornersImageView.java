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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;

import java.io.File;

public class RoundCornersImageView extends FrameLayout {

    public RoundCornersImageView(Context context) {
        super(context);
        init();
    }

    public RoundCornersImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundCornersImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        inflate(getContext(), R.layout.view_round_corners, this);
    }

    public void setImage(final File file) {
        final ImageView image = (ImageView) findViewById(R.id.rciv__image);

        Glide.with(getContext())
                .load(file)
                .asBitmap()
                .into(new ImageViewTarget<Bitmap>(image) {
                    @Override
                    protected void setResource(final Bitmap resource) {
                        // Render a "round corners" frame on top of the image.
                        final Drawable background = new BitmapDrawable(BaseApplication.get().getResources(), resource);
                        final Drawable frame = BaseApplication.get().getResources().getDrawable(R.drawable.frame);
                        final Drawable[] layers = {background, frame};
                        final LayerDrawable ld = new LayerDrawable(layers);

                        // The background image needs a small margin to ensure
                        // this it doesn't render on the outside edges of the frame
                        ld.setLayerInset(0, 2, 2, 2, 2);
                        image.setImageDrawable(ld);
                    }
                });
    }
}