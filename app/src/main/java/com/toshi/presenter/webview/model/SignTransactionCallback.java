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

package com.toshi.presenter.webview.model;


import com.toshi.crypto.util.TypeConverter;

public class SignTransactionCallback {

    private String signature;
    private String skeleton;

    public SignTransactionCallback setSkeleton(final String skeleton) {
        this.skeleton = skeleton;
        return this;
    }

    public SignTransactionCallback setSignature(final String signature) {
        this.signature = signature;
        return this;
    }

    public String toJsonEncodedString() throws Exception {
        return String.format(
                "{\\\"result\\\":\\\"%s\\\"}",
                TypeConverter.skeletonAndSignatureToRLPEncodedHex(skeleton, signature)
        );
    }
}
