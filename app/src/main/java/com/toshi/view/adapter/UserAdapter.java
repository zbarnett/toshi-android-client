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
import com.toshi.model.local.User;
import com.toshi.view.adapter.listeners.OnItemClickListener;
import com.toshi.view.adapter.viewholder.ClickableViewHolder;
import com.toshi.view.adapter.viewholder.UserViewHolder;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> implements ClickableViewHolder.OnClickListener {

    private List<User> users;
    public OnItemClickListener<User> onItemClickListener;

    public UserAdapter() {
        this.users = new ArrayList<>(0);
    }

    public UserAdapter setUsers(final List<User> users) {
        this.users = users;
        notifyDataSetChanged();
        return this;
    }

    @Override
    public UserViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__user, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {
        final User user = this.users.get(position);
        holder.setUser(user);
        holder.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    @Override
    public void onClick(final int position) {
        if (this.onItemClickListener == null && position > 0) return;
        final User clickedUser = this.users.get(position);
        this.onItemClickListener.onItemClick(clickedUser);
    }

    public UserAdapter setOnItemClickListener(final OnItemClickListener<User> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public void clear() {
        this.users.clear();
        notifyDataSetChanged();
    }
}
