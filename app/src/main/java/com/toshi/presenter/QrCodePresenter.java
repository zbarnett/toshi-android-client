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

import android.graphics.Bitmap;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.toshi.R;
import com.toshi.model.local.User;
import com.toshi.util.LogUtil;
import com.toshi.util.QrCode;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.QrCodeActivity;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class QrCodePresenter implements Presenter<QrCodeActivity> {

    private QrCodeActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(QrCodeActivity view) {
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
        fetchUser();
        initClickListeners();
    }

    private void fetchUser() {
        final Subscription sub =
                getCurrentUser()
                .toObservable()
                .filter(user -> user != null)
                .toSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showQrCode,
                        this::handleUserError
                );

        this.subscriptions.add(sub);
    }

    private Single<User> getCurrentUser() {
        return BaseApplication
                .get()
                .getUserManager()
                .getCurrentUser();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(v -> this.activity.finish());
    }

    private void handleUserError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error while fetching user", throwable);
        showToast(R.string.qr_code_error);
    }

    private void showQrCode(final User user) {
        final Subscription sub =
                QrCode.generateAddQrCode(user.getUsername())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::renderQrCode,
                        this::handleQrCodeError
                );

        this.subscriptions.add(sub);
    }

    private void renderQrCode(final Bitmap bitmap) {
        if (this.activity == null) return;
        this.activity.getBinding().qrCode.setAlpha(0.0f);
        this.activity.getBinding().qrCode.setImageBitmap(bitmap);
        this.activity.getBinding().qrCode.animate().alpha(1f).setDuration(200).start();
    }

    private void handleQrCodeError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error while generating qr code", throwable);
        showToast(R.string.qr_code_error);
    }

    private void showToast(@StringRes final int messageId) {
        Toast.makeText(this.activity, messageId, Toast.LENGTH_SHORT).show();
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
