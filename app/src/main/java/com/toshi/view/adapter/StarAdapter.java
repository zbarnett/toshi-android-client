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

package com.toshi.view.adapter;

import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toshi.R;
import com.toshi.view.adapter.listeners.OnItemClickListener;
import com.toshi.view.adapter.viewholder.StarViewHolder;

public class StarAdapter extends RecyclerView.Adapter<StarViewHolder> {

    @IntDef({
            READ_ONLY,
            CLICKABLE
    })
    private @interface MODE {}
    public static final int READ_ONLY = 0;
    public static final int CLICKABLE = 1;

    private static final int MAX_STARS = 5;
    private static final int MIN_STARS = 0;
    private static final int MIN_SELECTABLE_STARS = 1;

    private double rating;
    private @MODE int mode;
    private OnItemClickListener<Integer> listener;

    public void setOnItemClickListener(final OnItemClickListener<Integer> listener) {
        this.listener = listener;
    }

    public StarAdapter(final @MODE int mode) {
        this.mode = mode;
    }

    @Override
    public StarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = mode == CLICKABLE
                ? LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__star_big, parent, false)
                : LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__star_small, parent, false);
        return new StarViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StarViewHolder holder, int position) {
        final double rest = this.rating - position;

        if (rest < 1 && rest > 0) {
            holder.setHalfStar();
        } else if (position < this.rating){
            holder.setWholeStar();
        } else {
            holder.setWholeGreyStar();
        }

        if (this.mode == CLICKABLE) {
            holder.setOnItemClickListener(__ -> updateSelectedStars(position + 1));
        }
    }

    private void updateSelectedStars(final @IntRange(from = 1, to = 5) int starsSelected) {
        if (starsSelected > MAX_STARS) this.rating = MAX_STARS;
        else if (starsSelected < MIN_SELECTABLE_STARS) this.rating = MIN_SELECTABLE_STARS;
        else this.rating = starsSelected;

        this.notifyDataSetChanged();
        this.listener.onItemClick(starsSelected);
    }

    public void setStars(final @FloatRange(from = 0, to = 5) double rating) {
        if (rating > MAX_STARS) this.rating = MAX_STARS;
        else if (rating < MIN_STARS) this.rating = MIN_STARS;
        else this.rating = rating;

        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return MAX_STARS;
    }
}