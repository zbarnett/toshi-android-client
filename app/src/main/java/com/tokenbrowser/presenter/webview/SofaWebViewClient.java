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

import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.custom.listener.OnLoadListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import rx.Completable;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SofaWebViewClient extends WebViewClient {

    private final OnLoadListener listener;
    private final CompositeSubscription subscriptions;
    private String sofaScript;

    /* package */ SofaWebViewClient(@NonNull final OnLoadListener listener) {
        this.listener = listener;
        this.subscriptions = new CompositeSubscription();
        asyncLoadSofaScript();
    }

    private void asyncLoadSofaScript() {
        final Subscription sub =
                loadSofaScript()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        () -> {},
                        this.listener::onError
                );
        this.subscriptions.add(sub);
    }

    private Completable loadSofaScript() {
        final StringBuilder sb = new StringBuilder();
        final InputStream stream = BaseApplication.get().getResources().openRawResource(R.raw.sofa);
        final BufferedReader in = new BufferedReader(new InputStreamReader(stream));

        try {
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
        } catch (final IOException ex) {
            return Completable.error(ex);
        } finally {
            try {
                in.close();
            } catch (final IOException ex) {
                // Return in a finally statement. We failed anyway.
                return Completable.error(ex);
            }
        }

        this.sofaScript = sb.toString();

        return Completable.complete();
    }

    @Override
    public void onPageFinished(final WebView webView, final String url) {
        final Subscription sub =
                getSofaScript()
                .subscribe(
                        sofaScript -> this.handlePageFinished(sofaScript, webView),
                        this.listener::onError
                );
        this.subscriptions.add(sub);
    }

    private void handlePageFinished(final String sofaScript, final WebView webView) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(sofaScript, null);
            webView.evaluateJavascript("SOFA.initialize();", null);
        } else {
            webView.loadUrl("javascript:" + sofaScript, null);
            webView.loadUrl("javascript:SOFA.initialize();");
        }

        this.listener.onLoaded();
    }

    private Single<String> getSofaScript() {
        return
                Single.fromCallable(() -> {
                    while (sofaScript == null) {
                        Thread.sleep(100);
                    }
                    return sofaScript;
                })
                .timeout(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void destroy() {
        this.subscriptions.clear();
    }
}
