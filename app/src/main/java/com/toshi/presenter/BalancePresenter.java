/*
 * 	Copyright (c) 2017. Toshi Browser, Inc
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

import com.toshi.model.network.Balance;
import com.toshi.util.LogUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.BalanceActivity;
import com.toshi.view.activity.DepositActivity;
import com.toshi.view.activity.SendActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class BalancePresenter implements Presenter<BalanceActivity> {

    private BalanceActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;


    @Override
    public void onViewAttached(BalanceActivity view) {
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
        attachBalanceSubscriber();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().sendMoney.setOnClickListener(__ -> goToActivity(SendActivity.class));
        this.activity.getBinding().depositMoney.setOnClickListener(__ -> goToActivity(DepositActivity.class));
    }

    private void goToActivity(final Class clz) {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, clz);
        this.activity.startActivity(intent);
    }

    private void attachBalanceSubscriber() {
        final Subscription sub =
                BaseApplication
                        .get()
                        .getBalanceManager()
                        .getBalanceObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(balance -> balance != null)
                        .map(this::renderBalance)
                        .flatMap(balance -> balance.getFormattedLocalBalance().toObservable())
                        .subscribe(
                                this::renderFormattedBalance,
                                ex -> LogUtil.exception(getClass(), "Error during fetching balance", ex)
                        );

        this.subscriptions.add(sub);
    }

    private Balance renderBalance(final Balance balance) {
        if (this.activity != null) {
            this.activity.getBinding().ethBalance.setText(balance.getFormattedUnconfirmedBalance());
        }
        return balance;
    }

    private void renderFormattedBalance(final String formattedBalance) {
        if (this.activity != null) {
            this.activity.getBinding().localCurrencyBalance.setText(formattedBalance);
        }
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
