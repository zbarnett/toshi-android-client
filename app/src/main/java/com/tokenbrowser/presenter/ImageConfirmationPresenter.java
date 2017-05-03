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

package com.tokenbrowser.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.tokenbrowser.R;
import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.activity.ImageConfirmationActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ImageConfirmationPresenter implements Presenter<ImageConfirmationActivity> {

    private ImageConfirmationActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    private Uri imageUri;

    @Override
    public void onViewAttached(ImageConfirmationActivity view) {
        this.activity = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void initShortLivingObjects() {
        processIntentData();
        initToolbar();
        showImage();
        initClickListeners();
    }

    private void processIntentData() {
        this.imageUri = this.activity.getIntent().getParcelableExtra(ImageConfirmationActivity.FILE_URI);
    }

    private void initToolbar() {
        final String displayName = new FileUtil().getDisplayNameFromUri(this.imageUri);
        final String toolbarTitle = displayName != null
                ? displayName
                : this.activity.getString(R.string.confirm_image);

        final String title = toolbarTitle.length() > 15
                ? String.format("%.15s...", toolbarTitle)
                : toolbarTitle;

        this.activity.getBinding().title.setText(title);
    }

    private void showImage() {
        Glide
                .with(this.activity)
                .load(this.imageUri)
                .into(this.activity.getBinding().image);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> handleBackButtonClicked());
        this.activity.getBinding().approveButton.setOnClickListener(__ -> saveFileToLocalStorage());
    }

    public void handleBackButtonClicked() {
        final Intent intent = new Intent();
        this.activity.setResult(Activity.RESULT_CANCELED, intent);
        this.activity.finish();
    }

    private void saveFileToLocalStorage() {
        final Subscription sub =
                new FileUtil().saveFileFromUri(this.activity, this.imageUri)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        file -> finishWithResult(file.getAbsolutePath()),
                        this::handleFileError
                );

        this.subscriptions.add(sub);
    }

    private void finishWithResult(final String path) {
        final Intent intent = new Intent()
                .putExtra(ImageConfirmationActivity.FILE_PATH, path);
        this.activity.setResult(Activity.RESULT_OK, intent);
        this.activity.finish();
    }

    private void handleFileError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error during saving file to local storage", throwable);
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.activity = null;
    }
}
