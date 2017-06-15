package com.tokenbrowser.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityGroupSetupBinding;
import com.tokenbrowser.exception.PermissionException;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.local.PermissionResultHolder;
import com.tokenbrowser.presenter.GroupSetupPresenter;
import com.tokenbrowser.presenter.LoaderIds;
import com.tokenbrowser.presenter.factory.GroupSetupPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.util.LogUtil;

public class GroupSetupActivity extends BasePresenterActivity<GroupSetupPresenter, GroupSetupActivity> {

    public static final String PARTICIPANTS = "PARTICIPANTS";

    private ActivityGroupSetupBinding binding;
    private PermissionResultHolder permissionResultHolder;
    private ActivityResultHolder resultHolder;
    private GroupSetupPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_group_setup);
    }

    public ActivityGroupSetupBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<GroupSetupPresenter> getPresenterFactory() {
        return new GroupSetupPresenterFactory();
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

    private void tryProcessPermissionResultHolder() {
        if (this.presenter == null || this.permissionResultHolder == null) return;

        try {
            final boolean isPermissionHandled = this.presenter.tryHandlePermissionResult(this.permissionResultHolder);
            if (isPermissionHandled) {
                this.permissionResultHolder = null;
            }
        } catch (PermissionException e) {
            LogUtil.e(getClass(), "Error during permission request");
        }
    }

    private void tryProcessResultHolder() {
        if (this.presenter == null || this.resultHolder == null) return;

        if (this.presenter.handleActivityResult(this.resultHolder)) {
            this.resultHolder = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        tryProcessResultHolder();
        tryProcessPermissionResultHolder();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        this.presenter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(final Bundle inState) {
        super.onSaveInstanceState(inState);
        this.presenter.onRestoreInstanceState(inState);
    }

    @Override
    protected void onPresenterDestroyed() {
        super.onPresenterDestroyed();
        this.presenter = null;
    }

    @Override
    protected void onPresenterPrepared(@NonNull GroupSetupPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int loaderId() {
        return LoaderIds.get(this.getClass().getCanonicalName());
    }
}
