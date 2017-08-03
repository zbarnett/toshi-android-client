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
import com.toshi.view.adapter.listeners.OnItemClickListener;
import com.toshi.view.adapter.viewholder.SignInPassphraseViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SignInPassphraseAdapter extends RecyclerView.Adapter<SignInPassphraseViewHolder> {

    private List<String> passphrase;
    private OnItemClickListener<Integer> onItemClickListener;
    private boolean hideWords = false;

    public SignInPassphraseAdapter(final List<String> passphrase) {
        this.passphrase = new ArrayList<>(passphrase);
    }

    public SignInPassphraseAdapter setOnItemClickListener(final OnItemClickListener<Integer> listener) {
        this.onItemClickListener = listener;
        return this;
    }

    public void hideWords() {
        this.hideWords = true;
        notifyDataSetChanged();
    }

    public void showWords() {
        this.hideWords = false;
        notifyDataSetChanged();
    }

    public void setPassphrase(final List<String> passphrase) {
        this.passphrase.clear();
        this.passphrase.addAll(passphrase);
        this.notifyDataSetChanged();
    }

    @Override
    public SignInPassphraseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__sign_in_backup_phrase, parent, false);
        return new SignInPassphraseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SignInPassphraseViewHolder holder, int position) {
        final String word = this.passphrase.get(position);
        holder.setText(word)
                .setOnClickListener(this.onItemClickListener, position)
                .hideWord(this.hideWords);
    }

    @Override
    public int getItemCount() {
        return this.passphrase.size();
    }
}
