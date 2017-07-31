/*
 * 	Copyright (c) 2017. Toshi Inc
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

import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.toshi.R;
import com.toshi.crypto.HDWallet;
import com.toshi.manager.ToshiManager;
import com.toshi.util.SharedPrefsUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.MainActivity;
import com.toshi.view.activity.SignInActivity;
import com.toshi.view.activity.SignInInfoActivity;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SignInPresenter implements Presenter<SignInActivity> {

    private SignInActivity activity;
    private boolean firstTimeAttaching = true;
    private CompositeSubscription subscriptions;
    private boolean onGoingTask = false;

    @Override
    public void onViewAttached(SignInActivity view) {
        this.activity = view;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initMultilineWorkaround();
        initClickListeners();
    }

    private void initMultilineWorkaround() {
        this.activity.getBinding().passphrase.setHorizontallyScrolling(false);
        this.activity.getBinding().passphrase.setLines(3);
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().infoView.setOnClickListener(__ -> handleInfoViewClicked());
        this.activity.getBinding().signIn.setOnClickListener(v -> handleSignInClicked());
    }

    private void handleInfoViewClicked() {
        final Intent intent = new Intent(this.activity, SignInInfoActivity.class);
        this.activity.startActivity(intent);
    }

    private void handleSignInClicked() {
        final String passphraseInput = this.activity.getBinding().passphrase.getText().toString().toLowerCase().trim();
        final String[] passphraseArray = passphraseInput.split(" ");
        if (passphraseArray.length != 12) {
            Toast.makeText(
                    this.activity,
                    this.activity.getString(R.string.sign_in_length_error_message),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        tryCreateWallet(passphraseInput);
    }

    private void tryCreateWallet(final String masterSeed) {
        if (this.onGoingTask) return;
        startLoadingTask();

        final Subscription sub =
                new HDWallet()
                .createFromMasterSeed(masterSeed)
                .flatMap(this::initWallet)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(__ -> SharedPrefsUtil.setHasBackedUpPhrase())
                .subscribe(
                        __ -> handleWalletSuccess(),
                        __ -> handleWalletError()
                );

        this.subscriptions.add(sub);
    }

    private void startLoadingTask() {
        this.onGoingTask = true;
        this.activity.getBinding().loadingSpinner.setVisibility(View.VISIBLE);
    }

    private Single<ToshiManager> initWallet(final HDWallet wallet) {
        return BaseApplication
                .get()
                .getToshiManager()
                .init(wallet);
    }

    private void handleWalletSuccess() {
        stopLoadingTask();
        goToMainActivity();
    }

    private void goToMainActivity() {
        SharedPrefsUtil.setSignedIn();
        final Intent intent = new Intent(this.activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.activity.startActivity(intent);
        ActivityCompat.finishAffinity(this.activity);
    }

    private void handleWalletError() {
        showToast(R.string.unable_to_restore_wallet);
        stopLoadingTask();
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
