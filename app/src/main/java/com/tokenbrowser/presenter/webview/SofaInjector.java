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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.custom.listener.OnLoadListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import rx.Completable;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/* package */ class SofaInjector {

    private final OkHttpClient client;
    private final CompositeSubscription subscriptions;
    private final OnLoadListener listener;

    private String sofaScript;

    /* package */ SofaInjector(@NonNull final OnLoadListener listener) {
        this.listener = listener;
        this.client = new OkHttpClient();
        this.subscriptions = new CompositeSubscription();
        asyncLoadSofaScript();
    }

    private void asyncLoadSofaScript() {
        final Subscription sub =
                loadSofaScript()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this.listener::onReady,
                                this.listener::onError
                        );
        this.subscriptions.add(sub);
    }

    /* package */ Single<SofaInjectResponse> loadUrl(final String url) {
        return Single.fromCallable(() -> loadAndInjectSofa(url));
    }

    private SofaInjectResponse loadAndInjectSofa(final String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .build();

        final Response response = this.client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        final String body = response.body().string();
        final String injectedBody = injectSofaScript(body);

        return new SofaInjectResponse.Builder()
                .setAddress(url)
                .setData(injectedBody)
                .setMimeType(response.header("content-type", "text/plain"))
                .setEncoding(response.header("content-encoding", "utf-8"))
                .build();
    }

    private Completable loadSofaScript() {
        final StringBuilder sb = new StringBuilder();
        final InputStream stream = BaseApplication.get().getResources().openRawResource(R.raw.sofa);
        final BufferedReader in = new BufferedReader(new InputStreamReader(stream));

        sb.append(getRcpUrlInjection());

        try {
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
                sb.append("\n");
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

        this.sofaScript = "<script>" + sb.toString() + "</script>\n";

        return Completable.complete();
    }

    private String getRcpUrlInjection() {
        return String.format("window.SOFA = {config: {rcpUrl: \"%s\"}};", BaseApplication.get().getResources().getString(R.string.rcp_url));
    }

    private String injectSofaScript(final String body) {
        final int position = getInjectionPosition(body);
        final String beforeTag = body.substring(0, position);
        final String afterTab = body.substring(position);
        return beforeTag + this.sofaScript + afterTab;
    }

    private int getInjectionPosition(final String body) {
        final String htmlTag = "<script";
        return body.toLowerCase().indexOf(htmlTag);
    }

    /* package */ void destroy() {
        this.subscriptions.clear();
    }
}
