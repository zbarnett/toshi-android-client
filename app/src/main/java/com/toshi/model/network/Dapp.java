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

package com.toshi.model.network;

import com.squareup.moshi.Json;
import com.toshi.model.local.ToshiEntity;
import com.toshi.view.adapter.ToshiEntityAdapter;

public class Dapp implements ToshiEntity {
    @Json(name = "description")
    private String about;
    private String name;
    private String avatar;
    @Json(name = "url")
    private String address;

    @Override
    public int getReviewCount() {
        return 0;
    }

    @Override
    public Double getReputationScore() {
        return 0.0;
    }

    @Override
    public Double getAverageRating() {
        return 0.0;
    }

    @Override
    public String getToshiId() {
        return null;
    }

    public String getPaymentAddress() {
        return null;
    }

    public boolean isApp() {
        return true;
    }

    @Override
    public String getAbout() {
        return about;
    }

    // Defaults to the username if no name is set.
    @Override
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public String getUsername() {
        return null;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public @ToshiEntityAdapter.ViewType int getViewType() {
        return ToshiEntityAdapter.ITEM;
    }
}
