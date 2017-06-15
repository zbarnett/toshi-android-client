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
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.view.adapter.viewholder.GroupParticipantViewHolder;

import java.util.ArrayList;
import java.util.List;

public class GroupParticipantAdapter extends RecyclerView.Adapter<GroupParticipantViewHolder> {

    private List<User> users;

    public GroupParticipantAdapter() {
        this.users = new ArrayList<>();
    }

    public GroupParticipantAdapter addUser(final User user) {
        this.users.add(user);
        notifyDataSetChanged();
        return this;
    }

    @Override
    public GroupParticipantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__group_participant, parent, false);
        return new GroupParticipantViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GroupParticipantViewHolder holder, int position) {
        final User user = this.users.get(position);
        holder.setUser(user);
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }
}
