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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Toast;

import com.toshi.R;
import com.toshi.model.local.Attachment;
import com.toshi.util.FileUtil;
import com.toshi.util.ImageUtil;
import com.toshi.util.LogUtil;
import com.toshi.view.activity.AttachmentConfirmationActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class AttachmentConfirmationPresenter implements Presenter<AttachmentConfirmationActivity> {

    private AttachmentConfirmationActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(AttachmentConfirmationActivity view) {
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
        getAttachmentAndRender();
        initClickListeners();
    }

    private void getAttachmentAndRender() {
        final Attachment attachment = getAttachment();
        if (attachment == null || attachment.getFilename() == null) {
            showToast(R.string.invalid_resource);
            return;
        }
        initToolbar(attachment.getFilename());
        renderContent(attachment);
    }

    private void initToolbar(final String filename) {
        final String toolbarTitle = filename != null
                ? filename
                : this.activity.getString(R.string.confirm_image);

        final String title = toolbarTitle.length() > 20
                ? String.format("%.20s...", toolbarTitle)
                : toolbarTitle;

        this.activity.getBinding().title.setText(title);
    }

    private void renderContent(final Attachment attachment) {
        final String mimeType = getMimeType(attachment.getFilename());
        if (mimeType == null) {
            showToast(R.string.unsupported_file_type);
            this.activity.finish();
            return;
        }

        if (isImage(mimeType)) {
            showImage();
        } else {
            showAttachmentInfo(attachment);
        }
    }

    private void showToast(final @StringRes int res) {
        if (this.activity == null) return;
        Toast.makeText(this.activity, this.activity.getString(res), Toast.LENGTH_SHORT).show();
    }

    private void showImage() {
        if (this.activity == null) return;
        this.activity.getBinding().image.setVisibility(View.VISIBLE);
        ImageUtil.renderFileIntoTarget(getAttachmentUri(), this.activity.getBinding().image);
    }

    private void showAttachmentInfo(final Attachment attachment) {
        if (this.activity == null) return;
        this.activity.getBinding().fileWrapper.setVisibility(View.VISIBLE);
        this.activity.getBinding().displayName.setText(attachment.getFilename());
        final String fileSizeText = Formatter.formatFileSize(this.activity, attachment.getSize());
        this.activity.getBinding().fileSize.setText(fileSizeText);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> handleBackButtonClicked());
        this.activity.getBinding().approveButton.setOnClickListener(__ -> saveFileToLocalStorage());
    }

    public void handleBackButtonClicked() {
        if (this.activity == null) return;
        this.activity.setResult(Activity.RESULT_CANCELED, new Intent());
        this.activity.finish();
    }

    private void saveFileToLocalStorage() {
        final Attachment attachment = getAttachment();
        if (attachment == null || attachment.getFilename() == null) {
            showToast(R.string.invalid_resource);
            return;
        }
        final String mimeType = getMimeType(attachment.getFilename());
        if (isImage(mimeType)) {
            saveAndCompressImageToFile();
        } else {
            saveAttachmentToFile();
        }
    }

    private Attachment getAttachment() {
        return FileUtil.getNameAndSizeFromUri(getAttachmentUri());
    }

    private String getMimeType(final String filename) {
        return FileUtil.getMimeTypeFromFilename(filename);
    }

    private boolean isImage(final String mimeType) {
        return mimeType != null && mimeType.startsWith("image");
    }

    private void saveAndCompressImageToFile() {
        final Subscription sub =
                FileUtil.saveFileFromUri(this.activity, getAttachmentUri())
                .flatMap(file -> FileUtil.compressImage(FileUtil.MAX_SIZE, file))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        compressedFile -> finishWithResult(compressedFile.getAbsolutePath()),
                        this::handleFileError
                );

        this.subscriptions.add(sub);
    }

    private void saveAttachmentToFile() {
        final Subscription sub =
                FileUtil.saveFileFromUri(this.activity, getAttachmentUri())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        file -> finishWithResult(file.getAbsolutePath()),
                        this::handleFileError
                );

        this.subscriptions.add(sub);
    }

    private Uri getAttachmentUri() {
        return this.activity.getIntent().getParcelableExtra(AttachmentConfirmationActivity.ATTACHMENT_URI);
    }

    private void finishWithResult(final String path) {
        if (this.activity == null) return;
        final Intent intent = new Intent()
                .putExtra(AttachmentConfirmationActivity.ATTACHMENT_PATH, path);
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
