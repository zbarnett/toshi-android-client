package com.tokenbrowser.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tokenbrowser.BuildConfig;
import com.tokenbrowser.R;
import com.tokenbrowser.exception.PermissionException;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.local.Group;
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
import rx.Single;
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
    private Uri avatarUri;
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
        initNameListener();
        fetchUsers();
    }

    private void initClickListeners() {
        this.activity.getBinding().create.setOnClickListener(__ -> handleCreateClicked());
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().avatar.setOnClickListener(__ -> handleAvatarClicked());
    }

    private void handleCreateClicked() {
        final Subscription subscription =
                generateAvatar()
                .map(this::createGroup)
                .flatMap(
                    BaseApplication
                    .get()
                    .getSofaMessageManager()
                    ::createGroup
                ).subscribe(
                        __ -> LogUtil.i(getClass(), "Group created."),
                        ex -> LogUtil.e(getClass(), "Group creation failed: " + ex)
                );
        this.subscriptions.add(subscription);
    }

    private Group createGroup(final Bitmap avatar) {
        return new Group(this.getGroupParticipantAdapter().getUsers())
                        .setTitle(this.activity.getBinding().groupName.getText().toString())
                        .setAvatar(avatar);
    }

    private Single<Bitmap> generateAvatar() {
        return ImageUtil.loadAsBitmap(this.avatarUri, this.activity);
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
        final int numberOfParticipants = getParticipantList().size();
        final String participants = this.activity.getResources().getQuantityString(R.plurals.participants, numberOfParticipants, numberOfParticipants);
        this.activity.getBinding().numberOfParticipants.setText(participants);
    }

    private void initAvatarPlaceholder() {
        this.activity.getBinding().avatar.setImageResource(R.drawable.ic_camera_with_background);
    }

    private void initNameListener() {
        final Subscription sub =
                RxTextView.textChanges(this.activity.getBinding().groupName)
                .map(CharSequence::toString)
                .subscribe(
                        this::handleName,
                        throwable -> LogUtil.exception(getClass(), throwable)
                );

        this.subscriptions.add(sub);
    }

    private void handleName(final String groupName) {
        final TextView next =  this.activity.getBinding().create;
        next.setClickable(groupName.length() > 0);
        next.setAlpha(groupName.length() > 0 ? getEnabledAlphaValue() : getDisabledAlphaValue());
    }

    private float getEnabledAlphaValue() {
        final TypedValue typedValue = new TypedValue();
        this.activity.getResources().getValue(R.dimen.create_button_enabled, typedValue, true);
        return typedValue.getFloat();
    }

    private float getDisabledAlphaValue() {
        final TypedValue typedValue = new TypedValue();
        this.activity.getResources().getValue(R.dimen.create_button_disabled, typedValue, true);
        return typedValue.getFloat();
    }

    private void fetchUsers() {
        final Subscription sub =
                Observable.from(getParticipantList())
                .flatMap(this::fetchUser)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this.getGroupParticipantAdapter()::addUser,
                        throwable -> LogUtil.exception(getClass(), "Error during fetching group participants", throwable)
                );

        this.subscriptions.add(sub);
    }

    private List<String> getParticipantList() {
        return this.activity.getIntent().getStringArrayListExtra(GroupSetupActivity.PARTICIPANTS);
    }

    private Observable<User> fetchUser(final String tokenId) {
        return BaseApplication
                .get()
                .getRecipientManager()
                .getUserFromTokenId(tokenId)
                .toObservable();
    }

    private GroupParticipantAdapter getGroupParticipantAdapter() {
        return (GroupParticipantAdapter)this.activity.getBinding().participants.getAdapter();
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

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK
                || this.activity == null) {
            return false;
        }

        if (resultHolder.getRequestCode() == PICK_IMAGE) {
            this.avatarUri = resultHolder.getIntent().getData();
            ImageUtil.renderFileIntoTarget(this.avatarUri, this.activity.getBinding().avatar);
        } else if (resultHolder.getRequestCode() == CAPTURE_IMAGE) {
            final File file = new File(this.capturedImagePath);
            this.avatarUri = Uri.fromFile(file);
            ImageUtil.renderFileIntoTarget(this.avatarUri, this.activity.getBinding().avatar);
        }

        return true;
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
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.activity = null;
    }
}
