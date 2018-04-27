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

import android.app.Activity;
import android.content.Intent;
import android.util.Pair;

import com.toshi.model.local.ActivityResultHolder;
import com.toshi.model.network.Balance;
import com.toshi.util.PaymentType;
import com.toshi.util.logging.LogUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.AmountActivity;
import com.toshi.view.activity.BalanceActivity;
import com.toshi.view.activity.DepositActivity;
import com.toshi.view.activity.SendActivity;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class BalancePresenter implements Presenter<BalanceActivity> {

    private static final int SEND_REQUEST_CODE = 100;

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
        initRefreshListener();
        attachBalanceSubscriber();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().sendMoney.setOnClickListener(__ -> startPaymentActivityForResult());
        this.activity.getBinding().depositMoney.setOnClickListener(__ -> goToActivity(DepositActivity.class));
    }

    private void initRefreshListener() {
        this.activity.getBinding().refresh.setOnRefreshListener(this::refreshBalance);
    }

    private void refreshBalance() {
        final Subscription sub =
                BaseApplication
                .get()
                .getBalanceManager()
                .refreshBalanceCompletable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::stopRefreshing,
                        __ -> stopRefreshing()
                );

        this.subscriptions.add(sub);
    }

    private void stopRefreshing() {
        if (this.activity == null) return;
        this.activity.getBinding().refresh.setRefreshing(false);
    }

    private void startPaymentActivityForResult() {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, AmountActivity.class)
                .putExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_SEND);
        this.activity.startActivityForResult(intent, SEND_REQUEST_CODE);
    }

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK
                || this.activity == null
                || resultHolder.getRequestCode() != SEND_REQUEST_CODE) {
            return false;
        }

        final String ethAmount = resultHolder.getIntent().getStringExtra(AmountActivity.INTENT_EXTRA__ETH_AMOUNT);
        startSendActivityWithAmountIntent(ethAmount);
        return true;
    }

    private void startSendActivityWithAmountIntent(final String ethAmount) {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, SendActivity.class)
                .putExtra(SendActivity.INTENT_EXTRA__ETH_AMOUNT, ethAmount);
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
                .flatMap(this::mapBalance)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        balancePair -> renderFormattedBalance(balancePair.first, balancePair.second),
                        ex -> LogUtil.exception("Error during fetching balance", ex)
                );

        this.subscriptions.add(sub);
    }

    private Observable<Pair<Balance, String>> mapBalance(final Balance balance) {
        return balance
                .getFormattedLocalBalance()
                .toObservable()
                .map(formattedBalance -> new Pair<>(balance, formattedBalance));
    }

    private void renderFormattedBalance(final Balance balance, final String formattedBalance) {
        if (this.activity == null) return;
        this.activity.getBinding().ethBalance.setText(balance.getFormattedUnconfirmedBalance());
        this.activity.getBinding().localCurrencyBalance.setText(formattedBalance);
    }

    private void goToActivity(final Class clz) {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, clz);
        this.activity.startActivity(intent);
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
