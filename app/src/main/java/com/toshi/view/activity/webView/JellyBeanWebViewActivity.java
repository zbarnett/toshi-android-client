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

package com.toshi.view.activity.webView;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.toshi.R;
import com.toshi.databinding.ActivityWebViewBinding;
import com.toshi.model.local.ActivityResultHolder;
import com.toshi.model.local.PermissionResultHolder;
import com.toshi.presenter.LoaderIds;
import com.toshi.presenter.factory.PresenterFactory;
import com.toshi.presenter.factory.WebViewPresenterFactory;
import com.toshi.presenter.webview.WebViewPresenter;
import com.toshi.view.activity.BasePresenterActivity;

public class JellyBeanWebViewActivity extends BasePresenterActivity<WebViewPresenter, JellyBeanWebViewActivity> {

    public static final String EXTRA__ADDRESS = "address";
    private ActivityWebViewBinding binding;
    private WebViewPresenter presenter;

    private ActivityResultHolder resultHolder;
    private PermissionResultHolder permissionResultHolder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view);
    }

    public final ActivityWebViewBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<WebViewPresenter> getPresenterFactory() {
        return new WebViewPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull WebViewPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int loaderId() {
        return LoaderIds.get(this.getClass().getCanonicalName());
    }

    @Override
    public void onResume() {
        super.onResume();
        tryProcessResultHolder();
        tryProcessPermissionResultHolder();
        resumeWebView();
    }

    private void resumeWebView() {
        getBinding().webview.onResume();
        getBinding().webview.resumeTimers();
    }

    @Override
    public void onPause() {
        pauseWebView();
        super.onPause();
    }

    private void pauseWebView() {
        getBinding().webview.onPause();
        getBinding().webview.pauseTimers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.resultHolder = new ActivityResultHolder(requestCode, resultCode, data);
        tryProcessResultHolder();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String permissions[],
                                           @NonNull final int[] grantResults) {
        this.permissionResultHolder = new PermissionResultHolder(requestCode, permissions, grantResults);
        tryProcessPermissionResultHolder();
    }

    private void tryProcessResultHolder() {
        if (this.presenter == null || this.resultHolder == null) return;

        if (this.presenter.handleActivityResult(this.resultHolder)) {
            this.resultHolder = null;
        }
    }

    private void tryProcessPermissionResultHolder() {
        if (this.presenter == null || this.permissionResultHolder == null) return;
        final boolean isPermissionHandled = this.presenter.tryHandlePermissionResult(this.permissionResultHolder);
        if (isPermissionHandled) {
            this.permissionResultHolder = null;
        }
    }

    @Override
    protected void onPresenterDestroyed() {
        super.onPresenterDestroyed();
        this.presenter = null;
    }

    public void onDestroy() {
        getBinding().webview.destroy();
        super.onDestroy();
    }
}
