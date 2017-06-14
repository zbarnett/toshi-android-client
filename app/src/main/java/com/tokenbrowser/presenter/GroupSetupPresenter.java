package com.tokenbrowser.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tokenbrowser.BuildConfig;
import com.tokenbrowser.R;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.local.PermissionResultHolder;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.PermissionUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.GroupSetupActivity;
import com.tokenbrowser.view.adapter.GroupParticipantAdapter;
import com.tokenbrowser.view.custom.HorizontalLineDivider;
import com.tokenbrowser.view.fragment.DialogFragment.ChooserDialog;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class GroupSetupPresenter implements Presenter<GroupSetupActivity> {

    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final String INTENT_TYPE = "image/*";
    private static final String CAPTURED_IMAGE_PATH = "capturedImagePath";

    private GroupSetupActivity activity;
    private CompositeSubscription subscriptions;

    private boolean firstTimeAttaching = true;
    private String capturedImagePath;
    private ChooserDialog chooserDialog;

    @Override
    public void onViewAttached(GroupSetupActivity view) {
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
        initClickListeners();
        initRecyclerView();
        initNumberOfParticipantsView();
        initAvatarPlaceholder();
        fetchUsers();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().avatar.setOnClickListener(__ -> handleAvatarClicked());
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.activity.getBinding().participants;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new GroupParticipantAdapter());
        recyclerView.setNestedScrollingEnabled(false);

        final int dividerLeftPadding = this.activity.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + this.activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.activity, R.color.divider))
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void initNumberOfParticipantsView() {
        final String participants = this.activity.getString(R.string.number_of_participants, String.format("%d", getParticipantList().size()));
        this.activity.getBinding().numberOfParticipants.setText(participants);
    }

    private void initAvatarPlaceholder() {
        ImageUtil.renderResourceIntoTarget(
                R.drawable.ic_camera_with_background,
                this.activity.getBinding().avatar
        );
    }

    private void fetchUsers() {
        final Subscription sub =
                Observable.from(getParticipantList())
                .flatMap(this::fetchUser)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> getGroupParticipantAdapter().addUser(user),
                        this::HandleError
                );

        this.subscriptions.add(sub);
    }

    private List<String> getParticipantList() {
        return this.activity.getIntent().getStringArrayListExtra(GroupSetupActivity.PARTICIPANTS);
    }

    private Observable<User> fetchUser(final String tokenId) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserFromAddress(tokenId)
                .toObservable();
    }

    private GroupParticipantAdapter getGroupParticipantAdapter() {
        return (GroupParticipantAdapter)this.activity.getBinding().participants.getAdapter();
    }

    private void HandleError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error during fetching group participants", throwable);
    }

    private void handleAvatarClicked() {
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
        final boolean hasPermission = PermissionUtil.hasPermission(this.activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasPermission) {
            startGalleryActivity();
        } else {
            PermissionUtil.requestPermission(
                    this.activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION
            );
        }
    }

    private void checkCameraPermission() {
        final boolean hasPermission = PermissionUtil.hasPermission(this.activity, Manifest.permission.CAMERA);
        if (hasPermission) {
            startCameraActivity();
        } else {
            PermissionUtil.requestPermission(
                    this.activity,
                    Manifest.permission.CAMERA,
                    PermissionUtil.CAMERA_PERMISSION
            );
        }
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

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK
                || this.activity == null) {
            return false;
        }

        if (resultHolder.getRequestCode() == PICK_IMAGE) {
            final Uri imageUri = resultHolder.getIntent().getData();
            ImageUtil.renderFileIntoTarget(imageUri, this.activity.getBinding().avatar);
        } else if (resultHolder.getRequestCode() == CAPTURE_IMAGE) {
            final File file = new File(this.capturedImagePath);
            final Uri imageUri = Uri.fromFile(file);
            ImageUtil.renderFileIntoTarget(imageUri, this.activity.getBinding().avatar);
        }

        return true;
    }

    public boolean handlePermissionResult(final PermissionResultHolder permissionResultHolder) {
        if (permissionResultHolder == null || this.activity == null) return false;
        final int[] grantResults = permissionResultHolder.getGrantResults();
        if (grantResults.length == 0) return true;
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) return true;

        if (permissionResultHolder.getRequestCode() == PermissionUtil.CAMERA_PERMISSION) {
            startCameraActivity();
            return true;
        } else if (permissionResultHolder.getRequestCode() == PermissionUtil.READ_EXTERNAL_STORAGE_PERMISSION) {
            startGalleryActivity();
            return true;
        }

        return false;
    }

    public void onSaveInstanceState(final Bundle outState) {
        outState.putString(CAPTURED_IMAGE_PATH, this.capturedImagePath);
    }

    public void onRestoreInstanceState(final Bundle inState) {
        this.capturedImagePath = inState.getString(CAPTURED_IMAGE_PATH);
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
