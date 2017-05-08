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
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tokenbrowser.view.custom.listener.OnLoadListener;

/* package */ class SofaWebViewClient extends WebViewClient {

    private final OnLoadListener listener;

    /* package */ SofaWebViewClient(@NonNull final OnLoadListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPageFinished(WebView webView, final String url) {
        this.listener.onLoaded();
        super.onPageFinished(webView, url);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        this.listener.onLoaded();
    }
}
