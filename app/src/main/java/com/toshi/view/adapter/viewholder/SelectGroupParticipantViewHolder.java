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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.toshi.R;
import com.toshi.model.local.User;
import com.toshi.util.ImageUtil;
import com.toshi.view.adapter.listeners.OnItemClickListener;

public class SelectGroupParticipantViewHolder extends RecyclerView.ViewHolder {

    private @NonNull ImageView avatar;
    private @NonNull TextView name;
    private @NonNull TextView username;
    private @NonNull CheckBox checkBox;

    public SelectGroupParticipantViewHolder(final View view) {
        super(view);
        this.name = (TextView) view.findViewById(R.id.name);
        this.username = (TextView) view.findViewById(R.id.username);
        this.avatar = (ImageView) view.findViewById(R.id.avatar);
        this.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
    }

    public SelectGroupParticipantViewHolder setUser(final User user) {
        this.name.setText(user.getDisplayName());
        this.username.setText(user.getUsername());
        ImageUtil.load(user.getAvatar(), this.avatar);
        return this;
    }

    public SelectGroupParticipantViewHolder setOnClickListener(final OnItemClickListener<User> listener,
                                                               final User user) {
        this.itemView.setOnClickListener(__ -> handleUserClicked(listener, user));
        return this;
    }

    private void handleUserClicked(final OnItemClickListener<User> listener,
                                   final User user) {
        this.checkBox.performClick();
        listener.onItemClick(user);
    }

    public SelectGroupParticipantViewHolder setIsSelected(final boolean isSelected) {
        this.checkBox.setChecked(isSelected);
        return this;
    }
}
