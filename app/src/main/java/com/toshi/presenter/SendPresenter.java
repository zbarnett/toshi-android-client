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
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.toshi.R;
import com.toshi.crypto.util.TypeConverter;
import com.toshi.model.local.Network;
import com.toshi.model.local.Networks;
import com.toshi.util.EthUtil;
import com.toshi.util.LogUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.ScannerActivity;
import com.toshi.view.activity.SendActivity;
import com.toshi.view.fragment.DialogFragment.PaymentConfirmationDialog;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class SendPresenter implements Presenter<SendActivity>,PaymentConfirmationDialog.OnPaymentConfirmationListener {

    public static final String EXTRA__INTENT = "extraIntent";
    private static final int QR_REQUEST_CODE = 200;

    private SendActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;
    private String encodedEthAmount;
    private PaymentConfirmationDialog paymentConfirmationDialog;


    @Override
    public void onViewAttached(final SendActivity view) {
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
        initNetworkView();
        initUiListeners();
        processIntentData();
    }

    private void initNetworkView() {
        final Network network = Networks.getInstance().getCurrentNetwork();
        this.activity.getBinding().network.setText(network.getName());
    }

    private void initUiListeners() {
        this.activity.getBinding().closeButton.setOnClickListener( __ -> this.activity.finish());
        this.activity.getBinding().scan.setOnClickListener(__ -> startScanQrActivity());
        this.activity.getBinding().send.setOnClickListener(__ -> showPaymentConfirmationDialog());
        RxTextView
                .textChanges(this.activity.getBinding().recipientAddress)
                .subscribe(
                        this::handleRecipientAddressChanged,
                        t -> LogUtil.e(getClass(), t.toString())
                );
    }

    private void handleRecipientAddressChanged(final CharSequence charSequence) {
        this.activity.getBinding().send.setEnabled(charSequence.length() > 16);
    }

    private void startScanQrActivity() {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, ScannerActivity.class);
        this.activity.startActivityForResult(intent, QR_REQUEST_CODE);
    }

    private void showPaymentConfirmationDialog() {
        this.paymentConfirmationDialog = PaymentConfirmationDialog.newInstanceExternalPayment(
                getRecipientAddress(),
                this.encodedEthAmount,
                null
        )
                .setOnPaymentConfirmationListener(this);
        this.paymentConfirmationDialog.show(this.activity.getSupportFragmentManager(), PaymentConfirmationDialog.TAG);
    }

    @Override
    public void onPaymentRejected(final Bundle bundle) {}

    @Override
    public void onPaymentApproved(final Bundle bundle) {
        BaseApplication
                .get()
                .getTransactionManager()
                .sendExternalPayment(
                        getRecipientAddress(),
                        this.encodedEthAmount
                );
        this.activity.finish();
    }

    private void processIntentData() {
        final Intent amountIntent = this.activity.getIntent().getParcelableExtra(EXTRA__INTENT);
        this.encodedEthAmount = amountIntent.getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
        generateAmount(this.encodedEthAmount);
    }

    private void generateAmount(final String amountAsEncodedEth) {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(amountAsEncodedEth);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        final String ethAmountString = EthUtil.weiAmountToUserVisibleString(weiAmount);

        final Subscription sub = BaseApplication
                .get()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount)
                .subscribe(
                        localAmount -> renderAmount(localAmount, ethAmountString),
                        throwable -> LogUtil.exception(getClass(), throwable)
                );

        this.subscriptions.add(sub);
    }

    private void renderAmount(final String localAmount, final String ethAmount) {
        if (this.activity == null) return;
        final String usdEth = this.activity.getString(R.string.local_dot_eth_amount, localAmount, ethAmount);
        this.activity.getBinding().amount.setText(usdEth);
    }

    @NonNull
    private String getRecipientAddress() {
        final String userInput = this.activity.getBinding().recipientAddress.getText().toString();
        return userInput.contains(":") ? userInput.split(":")[1] : userInput;
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        if (this.paymentConfirmationDialog != null) {
            this.paymentConfirmationDialog.dismissAllowingStateLoss();
            this.paymentConfirmationDialog = null;
        }
        this.subscriptions = null;
        this.activity = null;
    }
}
