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
import android.support.annotation.Nullable;

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
    private String webViewSupportScript;

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
                loadInjections()
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

    private Completable loadInjections() {
        return Completable.merge(
                loadSofaScript(),
                loadWebViewSupportInjection()
        );
    }

    private Completable loadSofaScript() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getRcpUrlInjection());
        try {
            final String script = appendFromBuffer(sb, R.raw.sofa);
            this.sofaScript = "<script type=\"text/javascript\">" + script + "</script>\n";
        } catch (final IOException ex) {
            return Completable.error(ex);
        }
        return Completable.complete();
    }

    private Completable loadWebViewSupportInjection() {
        final StringBuilder sb = new StringBuilder();
        try {
            final String script = appendFromBuffer(sb, R.raw.webviewsupport);
            this.webViewSupportScript = "<script type=\"text/javascript\">" + script + "</script>\n";
        } catch (final IOException ex) {
            return Completable.error(ex);
        }
        return Completable.complete();
    }

    @NonNull
    private BufferedReader getBufferedReaderForResource(int resourceId) {
        final InputStream stream = BaseApplication.get().getResources().openRawResource(resourceId);
        return new BufferedReader(new InputStreamReader(stream));
    }

    @NonNull
    private String appendFromBuffer(StringBuilder sb, int resourceId) throws IOException {
        try (BufferedReader in = getBufferedReaderForResource(resourceId)) {
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String getRcpUrlInjection() {
        return String.format("window.SOFA = {config: {netVersion: \"%s\", accounts: [\"%s\"], rcpUrl: \"%s\"}};",
                BaseApplication.get().getResources().getString(R.string.net_version),
                this.wallet.getPaymentAddress(),
                BaseApplication.get().getResources().getString(R.string.rcp_url));
    }

    private String injectSofaScript(final String body) {
        final int position = getInjectionPosition(body);
        if (position == -1) return body;
        final String beforeTag = body.substring(0, position);
        final String afterTab = body.substring(position);
        String supportLib = this.webViewSupportScript != null ? this.webViewSupportScript : "";
        return beforeTag + supportLib + this.sofaScript + afterTab;
    }

    private int getInjectionPosition(final String body) {
        final String lowerCaseBody = body.toLowerCase();
        final int ieCheckIndex = lowerCaseBody.indexOf("<!--[if");
        int scriptTagIndex = lowerCaseBody.indexOf("<script");
        return ieCheckIndex < 0 ? scriptTagIndex : Math.min(scriptTagIndex, ieCheckIndex);
    }

    /* package */ void destroy() {
        this.subscriptions.clear();
    }
}
