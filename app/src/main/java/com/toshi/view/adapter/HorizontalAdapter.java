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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.toshi.R;
import com.toshi.model.local.ToshiEntity;
import com.toshi.view.adapter.listeners.OnItemClickListener;
import com.toshi.view.adapter.viewholder.HorizontalViewHolder;

import java.util.ArrayList;
import java.util.List;

public class HorizontalAdapter<T extends ToshiEntity> extends RecyclerView.Adapter<HorizontalViewHolder> {

    private final boolean showRating;
    private List<T> elements;
    private OnItemClickListener<T> listener;

    public HorizontalAdapter() {
        this(0);
    }

    public HorizontalAdapter(final int numberOfPlaceholders) {
        this(numberOfPlaceholders, true);
    }

    public HorizontalAdapter(final int numberOfPlaceholders, final boolean showRating) {
        this.showRating = showRating;
        this.elements = new ArrayList<>();
        for (int i = 0; i < numberOfPlaceholders; i++) {
            this.elements.add(null);
        }
    }

    public HorizontalAdapter setOnItemClickListener(final OnItemClickListener<T> listener) {
        this.listener = listener;
        return this;
    }

    public void setItems(final List<T> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
        this.notifyDataSetChanged();
    }

    @Override
    public HorizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__horizontal, parent, false);
        if (!showRating) {
            View ratingView = v.findViewById(R.id.rating_view);
            if (ratingView != null) {
                ratingView.setVisibility(View.GONE);
            }
        }
        return new HorizontalViewHolder<T>(v);
    }

    @Override
    public void onBindViewHolder(HorizontalViewHolder holder, int position) {
        final T elem = this.elements.get(position);

        holder.setElement(elem);
        if (elem != null) holder.setOnClickListener(this.listener, elem);
    }

    @Override
    public int getItemCount() {
        return this.elements.size();
    }
}
