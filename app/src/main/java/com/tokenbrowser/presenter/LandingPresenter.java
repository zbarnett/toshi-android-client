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

import android.content.Intent;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import com.tokenbrowser.R;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.LandingActivity;
import com.tokenbrowser.view.activity.MainActivity;
import com.tokenbrowser.view.activity.SignInActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class LandingPresenter implements Presenter<LandingActivity> {

    private LandingActivity activity;
    private boolean firstTimeAttaching = true;
    private CompositeSubscription subscriptions;
    private boolean onGoingTask = false;

    @Override
    public void onViewAttached(LandingActivity view) {
        this.activity = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        initClickListeners();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void initClickListeners() {
        this.activity.getBinding().signIn.setOnClickListener(__ -> goToSignInActivity());
        this.activity.getBinding().createNewAccount.setOnClickListener(__ -> handleCreateNewAccountClicked());
    }

    private void goToSignInActivity() {
        final Intent intent = new Intent(this.activity, SignInActivity.class);
        this.activity.startActivity(intent);
    }

    private void handleCreateNewAccountClicked() {
        if (this.onGoingTask) return;
        startLoadingTask();

        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .init()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        __ -> handleWalletSuccess(),
                        this::handleWalletError
                );

        this.subscriptions.add(sub);
    }

    private void startLoadingTask() {
        this.onGoingTask = true;
        this.activity.getBinding().loadingSpinner.setVisibility(View.VISIBLE);
    }

    private void handleWalletSuccess() {
        stopLoadingTask();
        goToMainActivity();
    }

    private void goToMainActivity() {
        SharedPrefsUtil.setSignedIn();
        final Intent intent = new Intent(this.activity, MainActivity.class);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    private void handleWalletError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error while creating new wallet", throwable);
        stopLoadingTask();
        showToast(R.string.unable_to_create_wallet);
    }

    private void stopLoadingTask() {
        this.onGoingTask = false;
        this.activity.getBinding().loadingSpinner.setVisibility(View.GONE);
    }

    private void showToast(final @StringRes int stringId) {
        Toast.makeText(
                this.activity,
                this.activity.getString(stringId),
                Toast.LENGTH_SHORT
        ).show();
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
