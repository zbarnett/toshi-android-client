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

package com.toshi.presenter.webview;


/* package */ class SofaInjectResponse {

    private final String address;
    private final String data;
    private final String mimeType;
    private final String encoding;

    private SofaInjectResponse(final Builder builder) {
        this.address = builder.address;
        this.data = builder.data;
        this.mimeType = builder.mimeType;
        this.encoding = builder.encoding;
    }

    /* package */ String getAddress() {
        return address;
    }

    /* package */ String getData() {
        return data;
    }

    /* package */ String getMimeType() {
        return mimeType;
    }

    /* package */ String getEncoding() {
        return encoding;
    }

    /* package */ static class Builder {
        private String address;
        private String data;
        private String mimeType;
        private String encoding;

        /* package */ Builder setAddress(final String address) {
            this.address = address;
            return this;
        }

        /* package */ Builder setData(final String data) {
            this.data = data;
            return this;
        }

        /* package */ Builder setMimeType(final String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        /* package */ Builder setEncoding(final String encoding) {
            this.encoding = encoding;
            return this;
        }

        /* package */ SofaInjectResponse build() throws IllegalStateException {
            if (!isValid()) {
                throw new IllegalStateException("SofaInjectResponse could not be build due to missing information.");
            }

            return new SofaInjectResponse(this);
        }

        private boolean isValid() {
            return this.address != null
                    && this.data != null
                    && this.mimeType != null
                    && this.encoding != null;
        }

    }
}
