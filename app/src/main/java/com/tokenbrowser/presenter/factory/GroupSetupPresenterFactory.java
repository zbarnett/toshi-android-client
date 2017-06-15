package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.GroupSetupPresenter;

public class GroupSetupPresenterFactory implements PresenterFactory<GroupSetupPresenter> {
    @Override
    public GroupSetupPresenter create() {
        return new GroupSetupPresenter();
    }
}
