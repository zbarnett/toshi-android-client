/*
 * 	Copyright (c) 2017. Token Browser, Inc
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

package com.tokenbrowser.presenter.webview;


import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

/* package */ class SOFAHost {

    private final SofaHostListener listener;

    /* package */ SOFAHost(@NonNull final SofaHostListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public String getRcpUrl() {
        return this.listener.getRcpUrl();
    }

    @JavascriptInterface
    public String getAccounts() {
        return this.listener.getAccounts();
    }

    @JavascriptInterface
    public boolean approveTransaction(final String unsignedTransaction) {
        return this.listener.approveTransaction(unsignedTransaction);
    }

    @JavascriptInterface
    public void signTransaction(final String unsignedTransaction) {
        this.listener.signTransaction(unsignedTransaction);
    }
}
