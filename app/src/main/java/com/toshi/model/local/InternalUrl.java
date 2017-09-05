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

package com.toshi.model.local;


import android.net.Uri;

import com.toshi.R;
import com.toshi.util.QrCodeType;
import com.toshi.view.BaseApplication;

import static com.toshi.util.QrCodeParameterName.MEMO;
import static com.toshi.util.QrCodeParameterName.VALUE;

public class InternalUrl {

    private final String baseUrl;
    private final Uri uri;

    public InternalUrl(final String url) {
        this.baseUrl = BaseApplication.get().getString(R.string.qr_code_base_url);
        this.uri = parseUrl(url);
    }

    private Uri parseUrl(final String url) {
        if (!isValid(url)) return null;
        return Uri.parse(url);
    }

    private boolean isValid(final String url) {
        return url != null && url.startsWith(this.baseUrl);
    }

    public String getUsername() {
        final String username = this.uri.getLastPathSegment();
        if (username.startsWith("@")) return username.substring(1);
        return username;
    }

    public String getAmount() {
        return this.uri.getQueryParameter(VALUE);
    }

    public String getMemo() {
        return this.uri.getQueryParameter(MEMO);
    }

    public boolean isValid() {
        return this.uri != null
                && getUsername() != null
                && this.uri.getPathSegments().size() >= 2;
    }

    public @QrCodeType.Type int getType() {
        try {
            final String typePath = this.uri.getPathSegments().get(0);
            if (typePath.equals("add")) return QrCodeType.ADD;
            if (typePath.equals("pay") && getAmount() != null) return QrCodeType.PAY;
            return QrCodeType.INVALID;
        } catch (final NullPointerException ex) {
            return QrCodeType.INVALID;
        }
    }
}
