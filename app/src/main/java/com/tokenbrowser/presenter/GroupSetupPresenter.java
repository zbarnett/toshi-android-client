package com.tokenbrowser.presenter;

import com.tokenbrowser.view.activity.GroupSetupActivity;

public class GroupSetupPresenter implements Presenter<GroupSetupActivity> {

    private GroupSetupActivity activity;

    @Override
    public void onViewAttached(GroupSetupActivity view) {
        this.activity = view;
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.activity = null;
    }
}
