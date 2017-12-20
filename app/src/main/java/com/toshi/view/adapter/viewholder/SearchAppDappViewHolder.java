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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.toshi.R;
import com.toshi.model.local.DappLink;
import com.toshi.util.LocaleUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.adapter.listeners.OnItemClickListener;

public class SearchAppDappViewHolder extends RecyclerView.ViewHolder {

    private Button launchDappButton;
    private DappLink dapp;

    public SearchAppDappViewHolder(View itemView) {
        super(itemView);
        this.launchDappButton = (Button) itemView.findViewById(R.id.dapp_launch_button);
    }

    public SearchAppDappViewHolder setDapp(final DappLink dapp) {
        this.dapp = dapp;
        final String textResource = BaseApplication.get().getResources().getString(R.string.launch_dapp);
        final String buttonText = String.format(LocaleUtil.getLocale(), textResource,  this.dapp.getAddress());
        this.launchDappButton.setText(buttonText);
        return this;
    }

    public SearchAppDappViewHolder setListener(final OnItemClickListener<DappLink> listener) {
        this.launchDappButton.setOnClickListener(view -> listener.onItemClick(this.dapp));
        return this;
    }
}
