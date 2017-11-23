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

package com.toshi.presenter;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import com.steelkiwi.cropiwa.config.CropIwaSaveConfig;
import com.toshi.R;
import com.toshi.util.FileUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.ImageCropActivity;

import java.io.File;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ImageCropPresenter implements Presenter<ImageCropActivity> {

    private static final float MIN_SCALE_FACTOR = 0.2f;
    private static final float MAX_SCALE_FACTOR = 10f;
    private static final int IMAGE_QUALITY = 100;
    private static final int MAX_IMAGE_DIAMATER = 800;

    private ImageCropActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;
    private boolean ongoingTask;
    private Uri imageUri;

    @Override
    public void onViewAttached(ImageCropActivity view) {
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
        initCropScale();
        showImage();
        initClickListeners();
    }

    private void processIntentData() {
        this.imageUri = this.activity.getIntent().getParcelableExtra(ImageCropActivity.IMAGE_URI);
    }

    private void initCropScale() {
        this.activity.getBinding()
                .cropImageView
                .configureImage()
                .setMinScale(MIN_SCALE_FACTOR)
                .setMaxScale(MAX_SCALE_FACTOR)
                .apply();
    }

    private void showImage() {
        this.activity.getBinding().cropImageView.setAlpha(0.0f);
        this.activity.getBinding().cropImageView.setImageUri(this.imageUri);
        this.activity.getBinding().cropImageView.animate().alpha(1f).setDuration(200).start();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().approve.setOnClickListener(__ -> cropAndUploadAvatar());
    }

    private void cropAndUploadAvatar() {
        if (this.ongoingTask) return;
        startLoadingState();
        initCropListener();
        cropImage();
    }

    private void startLoadingState() {
        this.ongoingTask = true;
        this.activity.getBinding().loadingSpinner.setVisibility(View.VISIBLE);
    }

    private void initCropListener() {
        this.activity.getBinding().cropImageView.setCropSaveCompleteListener(this::handleCroppedImage);
        this.activity.getBinding().cropImageView.setErrorListener(__ -> uploadUncroppedImage());
    }

    private void cropImage() {
        final File destFile = FileUtil.createImageFileWithRandomName();
        final Uri imageUri = Uri.fromFile(destFile);
        final CropIwaSaveConfig cropConfig =
                new CropIwaSaveConfig
                .Builder(imageUri)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setQuality(IMAGE_QUALITY)
                .setWidth(MAX_IMAGE_DIAMATER)
                .setHeight(MAX_IMAGE_DIAMATER)
                .build();

        this.activity.getBinding()
                .cropImageView.crop(cropConfig);
    }

    private void handleCroppedImage(final Uri bitmapUri) {
        if (bitmapUri == null) {
            stopLoadingState();
            showToast(R.string.crop_error);
            return;
        }

        uploadAvatar(new File(bitmapUri.getPath()));
    }

    //If the image is from the gallery, no file will exist. So we need to create one
    private void uploadUncroppedImage() {
        final File file = new File(this.imageUri.getPath());
        if (file.exists()) {
            uploadAvatar(file);
        } else {
            createNewFileAndUploadAvatar(this.imageUri);
        }
    }

    private void createNewFileAndUploadAvatar(final Uri uri) {
        final Subscription sub =
                FileUtil.saveFileFromUri(this.activity, uri)
                .flatMap(file -> FileUtil.compressImage(FileUtil.MAX_SIZE, file))
                .subscribe(
                        this::uploadAvatar,
                        __ -> handleUploadError()
                );

        this.subscriptions.add(sub);
    }

    private void uploadAvatar(final File file) {
        final Subscription sub =
                BaseApplication
                .get()
                .getUserManager()
                .uploadAvatar(file)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(__ -> tryDeleteCachedFile(file))
                .doOnError(__ -> tryDeleteCachedFile(file))
                .subscribe(
                        __ -> handleUploadSuccess(),
                        __ -> handleUploadError()
                );

        this.subscriptions.add(sub);
    }

    private void tryDeleteCachedFile(final File file) {
        if (file.exists()) file.delete();
    }

    private void handleUploadError() {
        stopLoadingState();
        showToast(R.string.profile_image_error);
    }

    private void handleUploadSuccess() {
        stopLoadingState();
        showToast(R.string.profile_image_success);
        this.activity.finish();
    }

    private void stopLoadingState() {
        this.ongoingTask = false;
        if (this.activity == null) return;
        this.activity.getBinding().loadingSpinner.setVisibility(View.GONE);
    }

    private void showToast(final @StringRes int stringRes) {
        if (this.activity == null) return;
        Toast.makeText(this.activity, this.activity.getString(stringRes), Toast.LENGTH_SHORT).show();
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
