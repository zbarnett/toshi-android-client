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
import com.toshi.view.adapter.viewholder.SelectGroupParticipantViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SelectGroupParticipantAdapter extends RecyclerView.Adapter<SelectGroupParticipantViewHolder> {

    private List<User> users;
    private List<User> selectedUsers;
    private OnItemClickListener<User> onItemClickListener;

    public SelectGroupParticipantAdapter() {
        this.users = new ArrayList<>();
        this.selectedUsers = new ArrayList<>();
    }

    public SelectGroupParticipantAdapter setUsers(final List<User> users) {
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
        return this;
    }
    public SelectGroupParticipantAdapter setOnItemClickListener(final OnItemClickListener<User> listener) {
        this.onItemClickListener = listener;
        return this;
    }

    public SelectGroupParticipantAdapter setSelectedUsers(final List<User> selectedUsers) {
        this.selectedUsers.addAll(selectedUsers);
        return this;
    }

    public SelectGroupParticipantAdapter addOrRemoveUser(final User user) {
        if (this.selectedUsers.contains(user)) this.selectedUsers.remove(user);
        else this.selectedUsers.add(user);
        return this;
    }

    @Override
    public SelectGroupParticipantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__select_group_participant, parent, false);
        return new SelectGroupParticipantViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SelectGroupParticipantViewHolder holder, int position) {
        final User user = this.users.get(position);
        final boolean isSelected = this.selectedUsers.contains(user);
        holder.setUser(user)
                .setIsSelected(isSelected)
                .setOnClickListener(this.onItemClickListener, user);
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    public void clear() {
        this.users.clear();
        notifyDataSetChanged();
    }
}
