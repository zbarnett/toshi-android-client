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


import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

public class SOFAHost {

    private final SofaHostListener listener;

    public SOFAHost(@NonNull final SofaHostListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void getAccounts(final String id) {
        this.listener.getAccounts(id);
    }

    @JavascriptInterface
    public void approveTransaction(final String id, final String unsignedTransaction) {
        this.listener.approveTransaction(id, unsignedTransaction);
    }

    @JavascriptInterface
    public void signTransaction(final String id, final String unsignedTransaction) {
        this.listener.signTransaction(id, unsignedTransaction);
    }

    @JavascriptInterface
    public void publishTransaction(final String id, final String signedTransaction) {
        this.listener.publishTransaction(id, signedTransaction);
    }

    @JavascriptInterface
    public void signPersonalMessage(final String id, final String msgParams) {
        this.listener.signPersonalMessage(id, msgParams);
    }

    @JavascriptInterface
    public void signMessage(final String id, final String from, final String data) {
        this.listener.signMessage(id, from, data);
    }
}
