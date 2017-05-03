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
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.tokenbrowser.R;
import com.tokenbrowser.presenter.Presenter;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.WebViewActivity;
import com.tokenbrowser.view.custom.listener.OnLoadListener;

public class WebViewPresenter implements Presenter<WebViewActivity> {

    private WebViewActivity activity;
    private SofaWebViewClient webClient;

    @Override
    public void onViewAttached(final WebViewActivity view) {
        this.activity = view;
        initWebClient();
        initView();
    }

    private void initWebClient() {
        if (this.webClient == null) {
            this.webClient = new SofaWebViewClient(this.loadedListener);
        }
    }

    private void initView() {
        initToolbar();
        initWebView();
        animateLoadingSpinner();
    }

    private void initToolbar() {
        final String address = this.activity.getIntent().getStringExtra(WebViewActivity.EXTRA__ADDRESS);
        this.activity.getBinding().title.setText(address);
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.onBackPressed());
    }

    private void initWebView() {
        loadWebApp();
        addWebClient();
    }

    private void loadWebApp() {
        final String address = this.activity.getIntent().getStringExtra(WebViewActivity.EXTRA__ADDRESS);
        this.activity.getBinding().webview.getSettings().setJavaScriptEnabled(true);
        this.activity.getBinding().webview.addJavascriptInterface(new SOFAHost(), "SOFAHost");
        this.activity.getBinding().webview.loadUrl(address);
    }

    private void addWebClient() {
        this.activity.getBinding().webview.setWebViewClient(this.webClient);
    }

    private void animateLoadingSpinner() {
        if (this.activity == null) return;
        final Animation rotateAnimation = AnimationUtils.loadAnimation(this.activity, R.anim.rotate);
        this.activity.getBinding().loadingView.startAnimation(rotateAnimation);
    }

    private final OnLoadListener loadedListener = new OnLoadListener() {
        @Override
        public void onLoaded() {
            if (activity == null) return;
            activity.getBinding().loadingView.clearAnimation();
            activity.getBinding().loadingView.setVisibility(View.GONE);
            activity.getBinding().webview.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(final Throwable t) {
            LogUtil.exception(getClass(), "Unable to load Dapp", t);
            if (activity == null) return;
            Toast.makeText(activity, R.string.error__dapp_loading, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    };

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.activity = null;
        this.webClient.destroy();
        this.webClient = null;
    }

    private class SOFAHost {

        @JavascriptInterface
        public String getRcpUrl() {
            return "https://propsten.infura.io";
        }

        @JavascriptInterface
        public String getAccounts() {
            return "[\"" +
                    BaseApplication
                    .get()
                    .getTokenManager()
                    .getWallet()
                    .toBlocking()
                    .value()
                    .getPaymentAddress()
                    + "\"]";
        }

        @JavascriptInterface
        public boolean approveTransaction(final String details) {
            return true;
        }

        @JavascriptInterface
        public String signTransaction(final String unsignedTransaction) {
            return BaseApplication
                    .get()
                    .getTokenManager()
                    .getWallet()
                    .toBlocking()
                    .value()
                    .signTransaction(unsignedTransaction);
        }
    }
}
