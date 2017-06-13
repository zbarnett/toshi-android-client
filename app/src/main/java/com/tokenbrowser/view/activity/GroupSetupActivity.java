package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityGroupSetupBinding;
import com.tokenbrowser.presenter.GroupSetupPresenter;
import com.tokenbrowser.presenter.LoaderIds;
import com.tokenbrowser.presenter.factory.GroupSetupPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public class GroupSetupActivity extends BasePresenterActivity<GroupSetupPresenter, GroupSetupActivity> {

    private ActivityGroupSetupBinding binding;

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
    protected void onPresenterPrepared(@NonNull GroupSetupPresenter presenter) {}

    @Override
    protected int loaderId() {
        return LoaderIds.get(this.getClass().getCanonicalName());
    }
}
