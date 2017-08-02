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
import com.toshi.view.adapter.viewholder.BackupPhraseViewHolder;

import java.util.ArrayList;
import java.util.List;

public class BackupPhraseAdapter extends RecyclerView.Adapter<BackupPhraseViewHolder> {

    private List<String> backupPhrase;

    public BackupPhraseAdapter(final List<String> backupPhrase) {
        this.backupPhrase = new ArrayList<>(backupPhrase);
    }

    public void setBackupPhraseItems(final List<String> backupPhrase) {
        this.backupPhrase.clear();
        this.backupPhrase.addAll(backupPhrase);
        this.notifyDataSetChanged();
    }

    @Override
    public BackupPhraseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__backup_phrase, parent, false);
        return new BackupPhraseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BackupPhraseViewHolder holder, int position) {
        final String word = this.backupPhrase.get(position);
        holder.setText(word);
    }

    @Override
    public int getItemCount() {
        return this.backupPhrase.size();
    }
}
