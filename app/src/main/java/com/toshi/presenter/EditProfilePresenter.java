/*
 * 	Copyright (c) 2017. Toshi Browser, Inc
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


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Toast;

import com.toshi.BuildConfig;
import com.toshi.R;
import com.toshi.exception.PermissionException;
import com.toshi.model.local.ActivityResultHolder;
import com.toshi.model.local.PermissionResultHolder;
import com.toshi.model.local.User;
import com.toshi.model.network.UserDetails;
import com.toshi.util.FileUtil;
import com.toshi.util.ImageUtil;
import com.toshi.util.LogUtil;
import com.toshi.util.OnSingleClickListener;
import com.toshi.util.PermissionUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.EditProfileActivity;
import com.toshi.view.activity.ImageCropActivity;
import com.toshi.view.fragment.DialogFragment.ChooserDialog;

import java.io.File;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EditProfilePresenter implements Presenter<EditProfileActivity> {

    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final String INTENT_TYPE = "image/*";
    private static final String CAPTURED_IMAGE_PATH = "capturedImagePath";

    private EditProfileActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    private String capturedImagePath;
    private ChooserDialog chooserDialog;

    private String displayNameFieldContents;
    private String userNameFieldContents;
    private String aboutFieldContents;
    private String locationFieldContents;
    private String avatarUrl;
    private Boolean isProfilePublic;

    @Override
    public void onViewAttached(final EditProfileActivity view) {
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
        initToolbar();
        updateView();
        initClickListeners();
        getUser();
    }

    private void initToolbar() {
        this.activity.getBinding().title.setText(R.string.edit_profile);
        this.activity.getBinding().saveButton.setOnClickListener(this.handleSaveClicked);
        this.activity.getBinding().avatar.setOnClickListener(this::handleAvatarClicked);
        this.activity.getBinding().editProfilePhoto.setOnClickListener(this::handleAvatarClicked);
    }

    private void updateView() {
        populateFields();
        loadAvatar();
    }

    private void populateFields() {
        this.activity.getBinding().inputName.setText(this.displayNameFieldContents);
        this.activity.getBinding().inputUsername.setText(this.userNameFieldContents);
        this.activity.getBinding().inputAbout.setText(this.aboutFieldContents);
        this.activity.getBinding().inputLocation.setText(this.locationFieldContents);
        this.activity.getBinding().publicSwitch.setChecked(this.isProfilePublic == null ? false : this.isProfilePublic);
    }

    private void loadAvatar() {
        if (this.avatarUrl == null) {
            this.activity.getBinding().avatar.setImageDrawable(null);
            this.activity.getBinding().avatar.setImageResource(R.color.textColorHint);
            return;
        }
        this.activity.getBinding().avatar.setImageResource(0);
        ImageUtil.loadFromNetwork(this.avatarUrl, this.activity.getBinding().avatar);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
    }

    private void getUser() {
        final Subscription sub =
                BaseApplication
                .get()
                .getUserManager()
                .getUserObservable()
                .filter(user -> user != null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleUserLoaded,
                        this::handleUserLoadedError
                );

        this.subscriptions.add(sub);
    }

    private void handleUserLoaded(final User user) {
        setFieldsFromUser(user);
        updateView();
    }

    private void handleUserLoadedError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error while fetching loaded user", throwable);
    }

    private final OnSingleClickListener handleSaveClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            if (!validate()) return;

            final UserDetails userDetails =
                    new UserDetails()
                    .setDisplayName(activity.getBinding().inputName.getText().toString().trim())
                    .setUsername(activity.getBinding().inputUsername.getText().toString().trim())
                    .setAbout(activity.getBinding().inputAbout.getText().toString().trim())
                    .setLocation(activity.getBinding().inputLocation.getText().toString().trim())
                    .setIsPublic(activity.getBinding().publicSwitch.isChecked());

            final Subscription sub =
                    BaseApplication
                    .get()
                    .getUserManager()
                    .updateUser(userDetails)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            __ -> handleUserUpdated(),
                            throwable -> handleUserUpdateFailed(throwable)
                    );

            subscriptions.add(sub);
        }

        private boolean validate() {
            final String displayName = activity.getBinding().inputName.getText().toString().trim();
            final String username = activity.getBinding().inputUsername.getText().toString().trim();

            if (displayName.trim().length() == 0) {
                activity.getBinding().inputName.setError(activity.getResources().getString(R.string.error__required));
                activity.getBinding().inputName.requestFocus();
                return false;
            }

            if (username.trim().length() == 0) {
                activity.getBinding().inputUsername.setError(activity.getResources().getString(R.string.error__required));
                activity.getBinding().inputUsername.requestFocus();
                return false;
            }

            if (username.contains(" ")) {
                activity.getBinding().inputUsername.setError(activity.getResources().getString(R.string.error__invalid_characters));
                activity.getBinding().inputUsername.requestFocus();
                return false;
            }
            return true;
        }
    };

    private void handleAvatarClicked(final View v) {
        this.chooserDialog = ChooserDialog.newInstance();
        this.chooserDialog.setOnChooserClickListener(new ChooserDialog.OnChooserClickListener() {
            @Override
            public void captureImageClicked() {
                checkCameraPermission();
            }

            @Override
            public void importImageFromGalleryClicked() {
                checkExternalStoragePermission();
            }
        });
        this.chooserDialog.show(this.activity.getSupportFragmentManager(), ChooserDialog.TAG);
    }

    private void checkExternalStoragePermission() {
        PermissionUtil.hasPermission(
                this.activity,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION,
                this::startGalleryActivity
        );
    }

    private void checkCameraPermission() {
        PermissionUtil.hasPermission(
                this.activity,
                Manifest.permission.CAMERA,
                PermissionUtil.CAMERA_PERMISSION,
                this::startCameraActivity
        );
    }

    private void startCameraActivity() {
        final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.activity.getPackageManager()) != null) {
            final File photoFile = new FileUtil().createImageFileWithRandomName();
            this.capturedImagePath = photoFile.getAbsolutePath();
            final Uri photoURI = FileProvider.getUriForFile(
                    BaseApplication.get(),
                    BuildConfig.APPLICATION_ID + FileUtil.FILE_PROVIDER_NAME,
                    photoFile);
            PermissionUtil.grantUriPermission(this.activity, cameraIntent, photoURI);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            this.activity.startActivityForResult(cameraIntent, CAPTURE_IMAGE);
        }
    }

    private void startGalleryActivity() {
        final Intent pickPictureIntent = new Intent()
                .setType(INTENT_TYPE)
                .setAction(Intent.ACTION_GET_CONTENT);

        if (pickPictureIntent.resolveActivity(this.activity.getPackageManager()) != null) {
            final Intent chooser = Intent.createChooser(
                    pickPictureIntent,
                    BaseApplication.get().getString(R.string.select_picture));
            this.activity.startActivityForResult(chooser, PICK_IMAGE);
        }
    }

    private void setFieldsFromUser(final User user) {
        if (this.displayNameFieldContents == null) this.displayNameFieldContents = user.getDisplayName();
        if (this.userNameFieldContents == null) this.userNameFieldContents = user.getUsernameForEditing();
        if (this.aboutFieldContents == null) this.aboutFieldContents = user.getAbout();
        if (this.locationFieldContents == null) this.locationFieldContents = user.getLocation();
        if (this.isProfilePublic == null) this.isProfilePublic = user.isPublic();
        this.avatarUrl = user.getAvatar();
    }

    private void handleUserUpdated() {
        showToast("Saved successfully!");
        if (this.activity != null) {
            this.activity.onBackPressed();
        }
    }

    private void handleUserUpdateFailed(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error while updating user", throwable);
        showToast("Error updating profile. Try a different username");
    }

    private void showToast(final String message) {
        Toast.makeText(this.activity, message, Toast.LENGTH_LONG).show();
    }

    private void saveFields() {
        this.displayNameFieldContents = this.activity.getBinding().inputName.getText().toString();
        this.userNameFieldContents = this.activity.getBinding().inputUsername.getText().toString();
        this.aboutFieldContents = this.activity.getBinding().inputAbout.getText().toString();
        this.locationFieldContents = this.activity.getBinding().inputLocation.getText().toString();
        this.isProfilePublic = this.activity.getBinding().publicSwitch.isChecked();
    }

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK
                || this.activity == null) {
            return false;
        }

        if (resultHolder.getRequestCode() == PICK_IMAGE) {
            final Uri imageUri = resultHolder.getIntent().getData();
            goToImageCropActivity(imageUri);
        } else if (resultHolder.getRequestCode() == CAPTURE_IMAGE) {
            final File file = new File(this.capturedImagePath);
            final Uri imageUri = Uri.fromFile(file);
            goToImageCropActivity(imageUri);
        }

        return true;
    }

    private void goToImageCropActivity(final Uri imageUri) {
        final Intent intent = new Intent(this.activity, ImageCropActivity.class)
                .putExtra(ImageCropActivity.IMAGE_URI, imageUri);
        this.activity.startActivity(intent);
    }

    /**
     *
     * @param permissionResultHolder Object containing info about the permission action
     * @return a boolean that tells if the method has handled the permission result
     * @throws PermissionException
     */
    public boolean tryHandlePermissionResult(final PermissionResultHolder permissionResultHolder) throws PermissionException {
        if (permissionResultHolder == null || this.activity == null) return false;
        final int[] grantResults = permissionResultHolder.getGrantResults();

        // Return true so the calling class knows the permission is handled
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) return true;

        if (permissionResultHolder.getRequestCode() == PermissionUtil.CAMERA_PERMISSION) {
            startCameraActivity();
            return true;
        } else if (permissionResultHolder.getRequestCode() == PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION) {
            startGalleryActivity();
            return true;
        } else {
            throw new PermissionException("This permission doesn't belong in this context");
        }
    }

    public void onSaveInstanceState(final Bundle outState) {
        outState.putString(CAPTURED_IMAGE_PATH, this.capturedImagePath);
    }

    public void onRestoreInstanceState(final Bundle inState) {
        this.capturedImagePath = inState.getString(CAPTURED_IMAGE_PATH);
    }

    @Override
    public void onViewDetached() {
        saveFields();
        closeDialogs();
        this.subscriptions.clear();
        this.activity = null;
    }

    private void closeDialogs() {
        if (this.chooserDialog != null) {
            this.chooserDialog.dismissAllowingStateLoss();
            this.chooserDialog = null;
        }
    }

    @Override
    public void onDestroyed() {
        this.activity = null;
    }
}
