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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.toshi.BuildConfig;
import com.toshi.R;
import com.toshi.model.local.ActivityResultHolder;
import com.toshi.model.local.PermissionResultHolder;
import com.toshi.presenter.Presenter;
import com.toshi.util.FileUtil;
import com.toshi.util.LogUtil;
import com.toshi.util.PermissionUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.webView.JellyBeanWebViewActivity;
import com.toshi.view.custom.listener.OnLoadListener;
import com.toshi.view.fragment.DialogFragment.ChooserDialog;

import java.io.File;
import java.net.URI;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class WebViewPresenter implements Presenter<JellyBeanWebViewActivity> {

    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final String IMAGE_TYPE = "image/*";

    private JellyBeanWebViewActivity activity;
    private SofaWebViewClient webClient;
    private ToshiChromeWebViewClient chromeWebViewClient;
    private SofaInjector sofaInjector;
    private SofaHostWrapper sofaHostWrapper;
    private CompositeSubscription subscriptions;
    private String capturedImagePath;
    private ValueCallback<Uri[]> filePathCallback;

    private boolean firstTimeAttaching = true;
    private boolean isLoaded = false;

    @Override
    public void onViewAttached(final JellyBeanWebViewActivity view) {
        this.activity = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initWebClient();
        initView();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
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

    private String tryGetAddress() {
        try {
            return getAddress();
        } catch (IllegalArgumentException e) {
            return BaseApplication.get().getString(R.string.unknown_address);
        }
    }

    private void initInjectsAndEmbeds() {
        final String address = tryGetAddress();
        this.webClient = new SofaWebViewClient(this.loadedListener);
        this.chromeWebViewClient = new ToshiChromeWebViewClient(this::handleFileChooserCallback);
        this.sofaHostWrapper = new SofaHostWrapper(this.activity, this.activity.getBinding().webview, address);

        final Subscription sub = BaseApplication.get()
            .getToshiManager()
            .getWallet()
            .subscribeOn(Schedulers.io())
            .subscribe(
                wallet -> this.sofaInjector = new SofaInjector(this.loadedListener, wallet),
                ex -> LogUtil.exception(getClass(), ex)
            );
        subscriptions.add(sub);
    }

    private boolean handleFileChooserCallback(final ValueCallback<Uri[]> valueCallback, final WebChromeClient.FileChooserParams fileChooserParams) {
        this.filePathCallback = valueCallback;
        showImageChooserDialog();
        return true;
    }

    private void initWebSettings() {
        final WebSettings webSettings = this.activity.getBinding().webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.WEB_DEBUG_ENABLED);
        }
    }

    private void injectEverything() {
        this.activity.getBinding().webview.addJavascriptInterface(this.sofaHostWrapper.getSofaHost(), "SOFAHost");
        this.activity.getBinding().webview.setWebViewClient(this.webClient);
        this.activity.getBinding().webview.setWebChromeClient(this.chromeWebViewClient);
    }

    private void initView() {
        initToolbar();
        animateLoadingSpinner();
    }

    private void initToolbar() {
        try {
            final String address = getAddress();
            this.activity.getBinding().address.setText(address);
        } catch (final IllegalArgumentException ex) {
            this.activity.getBinding().address.setText(BaseApplication.get().getString(R.string.unknown_address));
        }
        this.activity.getBinding().closeButton.setOnClickListener(__ -> handleBackButtonClicked());
    }

    private void handleBackButtonClicked() {
        if (this.activity == null) return;
        final WebView webView = this.activity.getBinding().webview;
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            this.activity.onBackPressed();
        }
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
            handleOnReady();
        }

        private void handleOnReady() {
            try {
                final String address = getAddress();
                loadUrlFromAddress(address);
            } catch (IllegalArgumentException e) {
                onError(e);
            }
        }

        private void loadUrlFromAddress(final String address) {
            if (sofaInjector == null) {
                onError(new Throwable("SofaInjector is null"));
                return;
            }
            final Subscription sub = sofaInjector.loadUrl(address)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::handleWebResourceResponse,
                            this::onError
                    );

            subscriptions.add(sub);
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
                         null
                    );
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
            showToast(R.string.error__dapp_loading);
        }

        @Override
        public void newPageLoad(final String address) {
            try {
                loadUrlFromAddress(address);
            } catch (IllegalArgumentException e) {
                showToast(R.string.unsupported_format);
            }
        }

        @Override
        public void updateUrl(final String url) {
            if (activity == null) return;
            activity.getBinding().address.setText(url);
            sofaHostWrapper.updateUrl(url);
        }

        @Override
        public void updateTitle(final String title) {
            if (activity == null) return;
            activity.getBinding().toolbarTitle.setText(title);
        }
    };

    private void hideLoadingSpinner() {
        activity.getBinding().loadingView.clearAnimation();
        activity.getBinding().loadingView.setVisibility(View.GONE);
        activity.getBinding().webview.setVisibility(View.VISIBLE);
    }

    private String getAddress() throws IllegalArgumentException {
        final String url = this.activity.getIntent().getStringExtra(JellyBeanWebViewActivity.EXTRA__ADDRESS).trim();
        final URI uri = URI.create(url);
        return uri.getScheme() == null
                ? "http://" + uri.toASCIIString()
                : uri.toASCIIString();
    }

    private void showToast(final @StringRes int stringRes) {
        Toast.makeText(
                this.activity,
                stringRes,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.sofaHostWrapper.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.activity = null;
        destroy();
    }

    private void destroy() {
        this.sofaInjector.destroy();
        this.sofaInjector = null;
        this.webClient = null;
        this.isLoaded = false;
        this.sofaHostWrapper = null;
    }

    private void showImageChooserDialog() {
        final ChooserDialog chooserDialog = ChooserDialog.newInstance();
        chooserDialog.setOnChooserClickListener(new ChooserDialog.OnChooserClickListener() {
            @Override
            public void captureImageClicked() {
                checkCameraPermission();
            }

            @Override
            public void importImageFromGalleryClicked() {
                checkExternalStoragePermission();
            }
        });
        chooserDialog.show(this.activity.getSupportFragmentManager(), ChooserDialog.TAG);
    }

    private void checkExternalStoragePermission() {
        PermissionUtil.hasPermission(
                this.activity,
                READ_EXTERNAL_STORAGE,
                PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION,
                this::startGalleryActivity
        );
    }

    private void checkCameraPermission() {
        PermissionUtil.hasPermission(
                this.activity,
                CAMERA,
                PermissionUtil.CAMERA_PERMISSION,
                this::startCameraActivity
        );
    }

    public boolean tryHandlePermissionResult(final PermissionResultHolder resultHolder) {
        final int[] grantResults = resultHolder.getGrantResults();
        final int requestCode = resultHolder.getRequestCode();
        if (!PermissionUtil.isPermissionGranted(grantResults)) return false;
        if (requestCode == PermissionUtil.CAMERA_PERMISSION) {
            startCameraActivity();
            return true;
        } else if (requestCode == PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION) {
            startGalleryActivity();
            return true;
        }
        return false;
    }

    private void startCameraActivity() {
        final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.activity.getPackageManager()) == null) return;
        final File photoFile = FileUtil.createImageFileWithRandomName();
        this.capturedImagePath = photoFile.getAbsolutePath();
        final Uri photoURI = FileProvider.getUriForFile(
                this.activity,
                BuildConfig.APPLICATION_ID + FileUtil.FILE_PROVIDER_NAME,
                photoFile
        );
        PermissionUtil.grantUriPermission(this.activity, cameraIntent, photoURI);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        this.activity.startActivityForResult(cameraIntent, CAPTURE_IMAGE);
    }

    private void startGalleryActivity() {
        final Intent pickPictureIntent = new Intent()
                .setType(IMAGE_TYPE)
                .setAction(Intent.ACTION_GET_CONTENT);
        if (pickPictureIntent.resolveActivity(this.activity.getPackageManager()) == null) return;
        final Intent chooserIntent = Intent.createChooser(
                pickPictureIntent,
                this.activity.getString(R.string.select_picture)
        );
        this.activity.startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        final int resultCode = resultHolder.getResultCode();
        final int requestCode = resultHolder.getRequestCode();
        if (resultCode != Activity.RESULT_OK) return false;
        if (requestCode == PICK_IMAGE) {
            final Uri uri = resultHolder.getIntent().getData();
            postFileCallback(uri);
            return true;
        } else if (requestCode == CAPTURE_IMAGE) {
            handleCapturesImage(this.capturedImagePath);
            return true;
        }
        return false;
    }

    private void handleCapturesImage(final String path) {
        if (this.capturedImagePath == null) return;
        final Uri uri = Uri.fromFile(new File(path));
        postFileCallback(uri);
        this.capturedImagePath = null;
    }

    private void postFileCallback(final Uri uri) {
        final Uri[] uriArray = new Uri[] {uri};
        filePathCallback.onReceiveValue(uriArray);
        filePathCallback = null;
    }
}
