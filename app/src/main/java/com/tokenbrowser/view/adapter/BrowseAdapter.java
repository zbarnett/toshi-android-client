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

package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.TokenEntity;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.adapter.viewholder.TokenEntityViewHolder;

import java.util.ArrayList;
import java.util.List;

public class BrowseAdapter<T extends TokenEntity> extends RecyclerView.Adapter<TokenEntityViewHolder> {

    private final List<T> elems;
    private OnItemClickListener<T> listener;

    public BrowseAdapter() {
        this.elems = new ArrayList<>();
    }

    public BrowseAdapter setOnItemClickListener(final OnItemClickListener<T> listener) {
        this.listener = listener;
        return this;
    }

    public void setItems(final List<T> elems) {
        this.elems.clear();
        this.elems.addAll(elems);
        notifyDataSetChanged();
    }

    @Override
    public TokenEntityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__token_entity, parent, false);
        return new TokenEntityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TokenEntityViewHolder holder, int position) {
        final TokenEntity elem = this.elems.get(position);
        holder.setTokenEntity(elem)
                .setOnClickListener(this.listener, elem);
    }

    @Override
    public int getItemCount() {
        return this.elems.size();
    }
}