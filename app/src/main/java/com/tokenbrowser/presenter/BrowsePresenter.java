/*
 * 	Copyright (c) 2017. Token Browser, Inc
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

package com.tokenbrowser.presenter;

import com.tokenbrowser.R;
import com.tokenbrowser.util.BrowseType;
import com.tokenbrowser.view.activity.BrowseActivity;

import rx.subscriptions.CompositeSubscription;

import static com.tokenbrowser.util.BrowseType.VIEW_TYPE_LATEST_APPS;
import static com.tokenbrowser.util.BrowseType.VIEW_TYPE_LATEST_PUBLIC_USERS;
import static com.tokenbrowser.util.BrowseType.VIEW_TYPE_TOP_RATED_APPS;
import static com.tokenbrowser.util.BrowseType.VIEW_TYPE_TOP_RATED_PUBLIC_USERS;

public class BrowsePresenter implements Presenter<BrowseActivity> {

    private BrowseActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(BrowseActivity view) {
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
        initClickListeners();
    }

    private void initToolbar() {
        this.activity.getBinding().title.setText(getTitle());
    }

    private String getTitle() {
        switch (getViewType()) {
            case VIEW_TYPE_TOP_RATED_APPS: {
                return this.activity.getString(R.string.top_rated_apps);
            }
            case VIEW_TYPE_LATEST_APPS: {
                return this.activity.getString(R.string.latest_apps);
            }
            case VIEW_TYPE_TOP_RATED_PUBLIC_USERS: {
                return this.activity.getString(R.string.top_rated_public_users);
            }
            case VIEW_TYPE_LATEST_PUBLIC_USERS:
            default: {
                return this.activity.getString(R.string.latest_public_users);
            }
        }
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
    }

    @SuppressWarnings("WrongConstant")
    private @BrowseType.Type int getViewType() {
        return this.activity.getIntent().getIntExtra(BrowseActivity.VIEW_TYPE, VIEW_TYPE_LATEST_PUBLIC_USERS);
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
