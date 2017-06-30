package com.toshi.presenter.factory;

import com.toshi.presenter.GroupSetupPresenter;

public class GroupSetupPresenterFactory implements PresenterFactory<GroupSetupPresenter> {
    @Override
    public GroupSetupPresenter create() {
        return new GroupSetupPresenter();
    }
}
