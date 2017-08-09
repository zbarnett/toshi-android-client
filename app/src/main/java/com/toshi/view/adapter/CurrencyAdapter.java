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
import com.toshi.model.network.Currency;
import com.toshi.view.adapter.listeners.OnItemClickListener;
import com.toshi.view.adapter.viewholder.CurrencyViewHolder;

import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyViewHolder> {

    private List<Currency> currencies;
    private OnItemClickListener<Currency> listener;

    public CurrencyAdapter(final List<Currency> currencies) {
        this.currencies = currencies;
    }

    public CurrencyAdapter setOnClickListener(final OnItemClickListener<Currency> listener) {
        this.listener = listener;
        return this;
    }

    public void addItems(final List<Currency> currencies) {
        this.currencies.clear();
        this.currencies.addAll(currencies);
        notifyDataSetChanged();
    }

    @Override
    public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__currency, parent, false);
        return new CurrencyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CurrencyViewHolder holder, int position) {
        final Currency currency = this.currencies.get(position);
        holder
                .setCurrency(currency)
                .setOnClickListener(this.listener, currency);
    }

    @Override
    public int getItemCount() {
        return this.currencies.size();
    }
}
