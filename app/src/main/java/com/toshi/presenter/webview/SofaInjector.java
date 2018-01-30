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

import com.toshi.R;
import com.toshi.crypto.HDWallet;
import com.toshi.manager.network.interceptor.DeviceInfoUserAgentInterceptor;
import com.toshi.util.webView.WebViewCookieJar;
import com.toshi.view.BaseApplication;
import com.toshi.view.custom.listener.OnLoadListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Completable;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/* package */ class SofaInjector {

    private final OkHttpClient client;
    private final CompositeSubscription subscriptions;
    private final OnLoadListener listener;
    private final HDWallet wallet;

    private String sofaScript;

    /**
     * Injects SOFA script into valid pages
     *
     * If there is no <script> tag then nothing will be injected
     */
    /* package */ SofaInjector(@NonNull final OnLoadListener listener, final HDWallet wallet) {
        this.listener = listener;
        this.wallet = wallet;
        this.client = buildHttpClient();
        this.subscriptions = new CompositeSubscription();
        asyncLoadSofaScript();
    }

    @NonNull
    private OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new DeviceInfoUserAgentInterceptor())
                .cookieJar(new WebViewCookieJar())
                .build();
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
                .setAddress(response.request().url().toString())
                .setData(injectedBody)
                .setMimeType(response.header("Content-Type", "text/html; charset=utf-8"))
                .setEncoding(response.header("Content-Encoding", "UTF-8"))
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

        this.sofaScript = "<script type=\"text/javascript\">" + sb.toString() + "</script>\n";

        return Completable.complete();
    }

    private String getRcpUrlInjection() {
        return String.format("window.SOFA = {config: {accounts: [\"%s\"], rcpUrl: \"%s\"}};",
                this.wallet.getPaymentAddress(),
                BaseApplication.get().getResources().getString(R.string.rcp_url));
    }

    private String injectSofaScript(final String body) {
        final int position = getInjectionPosition(body);
        if (position == -1) return body;
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
