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

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.tokenbrowser.R;
import com.tokenbrowser.presenter.Presenter;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.activity.WebViewActivity;
import com.tokenbrowser.view.custom.listener.OnLoadListener;

import java.net.URI;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WebViewPresenter implements Presenter<WebViewActivity> {

    private WebViewActivity activity;
    private SofaWebViewClient webClient;
    private SofaInjector sofaInjector;
    private SofaHostWrapper sofaHostWrapper;

    private boolean isLoaded = false;

    @Override
    public void onViewAttached(final WebViewActivity view) {
        this.activity = view;
        initWebClient();
        initView();
    }

    private void initWebClient() {
        if (this.webClient != null) {
            hideLoadingSpinner();
            return;
        }
        initInjectsAndEmbeds();
        initWebSettings();
        injectEverything();
    }

    private void initInjectsAndEmbeds() {
        this.webClient = new SofaWebViewClient(this.loadedListener);
        this.sofaInjector = new SofaInjector(this.loadedListener);
        this.sofaHostWrapper = new SofaHostWrapper(this.activity, this.activity.getBinding().webview);
    }

    private void initWebSettings() {
        final WebSettings webSettings = this.activity.getBinding().webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(false);
        webSettings.setDomStorageEnabled(true);
    }

    private void injectEverything() {
        this.activity.getBinding().webview.addJavascriptInterface(this.sofaHostWrapper.getSofaHost(), "SOFAHost");
        this.activity.getBinding().webview.setWebViewClient(this.webClient);
    }

    private void initView() {
        initToolbar();
        animateLoadingSpinner();
    }

    private void initToolbar() {
        final String address = getAddress();
        this.activity.getBinding().title.setText(address);
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.onBackPressed());
    }

    private void animateLoadingSpinner() {
        if (this.activity == null || this.isLoaded) return;
        final Animation rotateAnimation = AnimationUtils.loadAnimation(this.activity, R.anim.rotate);
        this.activity.getBinding().loadingView.startAnimation(rotateAnimation);
    }

    private final OnLoadListener loadedListener = new OnLoadListener() {
        @Override
        public void onReady() {
            if (activity == null) return;
            final String address = getAddress();

            sofaInjector.loadUrl(address)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::handleWebResourceResponse,
                            this::onError
                    );
        }

        private void handleWebResourceResponse(final SofaInjectResponse response) {
            if (activity == null) return;
            activity.getBinding()
                    .webview
                    .loadDataWithBaseURL(
                        response.getAddress(),
                        response.getData(),
                        response.getMimeType(),
                        response.getEncoding(),
                        null);
        }

        @Override
        public void onLoaded() {
            if (activity == null) return;
            hideLoadingSpinner();
            isLoaded = true;
        }

        @Override
        public void onError(final Throwable t) {
            LogUtil.exception(getClass(), "Unable to load Dapp", t);
            if (activity == null) return;
            Toast.makeText(activity, R.string.error__dapp_loading, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    };

    private void hideLoadingSpinner() {
        activity.getBinding().loadingView.clearAnimation();
        activity.getBinding().loadingView.setVisibility(View.GONE);
        activity.getBinding().webview.setVisibility(View.VISIBLE);
    }

    private String getAddress() {
        final String url = this.activity.getIntent().getStringExtra(WebViewActivity.EXTRA__ADDRESS).trim();
        final URI uri = URI.create(url);
        return uri.getScheme() == null
                ? "http://" + uri.toASCIIString()
                : uri.toASCIIString();
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
        // WebView doesn't handle orientation change so
        // we can destroy it. This seems to be an Android issue.
        // this kinda sucks.
        destroy();
    }

    @Override
    public void onDestroyed() {
        this.activity = null;
    }

    private void destroy() {
        this.sofaInjector.destroy();
        this.sofaInjector = null;
        this.webClient = null;
        this.isLoaded = false;
        this.sofaHostWrapper.destroy();
        this.sofaHostWrapper = null;
    }

}
