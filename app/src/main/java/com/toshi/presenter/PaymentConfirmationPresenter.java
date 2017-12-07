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

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.toshi.R;
import com.toshi.crypto.HDWallet;
import com.toshi.crypto.util.TypeConverter;
import com.toshi.manager.model.PaymentTask;
import com.toshi.model.local.GasPrice;
import com.toshi.model.local.UnsignedW3Transaction;
import com.toshi.model.local.User;
import com.toshi.model.sofa.SofaAdapters;
import com.toshi.util.EthUtil;
import com.toshi.util.LogUtil;
import com.toshi.util.OnSingleClickListener;
import com.toshi.util.PaymentType;
import com.toshi.view.BaseApplication;
import com.toshi.view.fragment.DialogFragment.PaymentConfirmationDialog;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PaymentConfirmationPresenter implements Presenter<PaymentConfirmationDialog> {

    private PaymentConfirmationDialog view;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttached = true;

    private Bundle bundle;
    private String encodedEthAmount;
    private String memo;
    private String paymentAddress;
    private String toshiId;
    private @PaymentType.Type int paymentType;
    private PaymentTask paymentTask;

    @Override
    public void onViewAttached(PaymentConfirmationDialog view) {
        this.view = view;

        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            initLongLivingObjects();
        }

        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void initShortLivingObjects() {
        initClickListeners();
        processBundleData();
        setTitle();
        setMemo();
        tryLoadUserAndLocalAmount(this.toshiId, this.encodedEthAmount);
        getGasPrice();
    }

    private void initClickListeners() {
        this.view.getBinding().cancel.setOnClickListener(__ -> handleCanceledClicked());
        this.view.getBinding().pay.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                handleApprovedClicked();
            }
        });
    }

    @SuppressWarnings("WrongConstant")
    private void processBundleData() {
        this.bundle = this.view.getArguments();
        this.toshiId = this.bundle.getString(PaymentConfirmationDialog.TOSHI_ID);
        this.paymentAddress = this.bundle.getString(PaymentConfirmationDialog.PAYMENT_ADDRESS);
        this.encodedEthAmount = this.bundle.getString(PaymentConfirmationDialog.ETH_AMOUNT);
        this.memo = this.bundle.getString(PaymentConfirmationDialog.MEMO);
        this.paymentType = this.bundle.getInt(PaymentConfirmationDialog.PAYMENT_TYPE);
    }

    private void getGasPrice() {
        if (this.toshiId != null) getPaymentTaskWithToshiId(this.toshiId, this.encodedEthAmount);
        else getPaymentTaskWithPaymentAddress(this.paymentAddress, this.encodedEthAmount);
    }

    private void handleApprovedClicked() {
        if (this.view.getPaymentConfirmationApprovedListener() != null) {
            this.view.getPaymentConfirmationApprovedListener().onPaymentApproved(this.bundle, this.paymentTask);
        }
        this.view.dismiss();
    }

    private void handleCanceledClicked() {
        if (this.view.getPaymentConfirmationCanceledListener() != null) {
            this.view.getPaymentConfirmationCanceledListener().onPaymentCanceled(this.bundle);
        }
        this.view.dismiss();
    }

    private void setTitle() {
        final String title = this.paymentType == PaymentType.TYPE_SEND
                ? this.view.getString(R.string.payment_confirmation_title)
                : this.view.getString(R.string.payment_request_confirmation_title);
        this.view.getBinding().title.setText(title);
    }

    private void setMemo() {
        if (this.memo == null) return;
        this.view.getBinding().memo.setVisibility(View.VISIBLE);
        this.view.getBinding().memo.setText(this.memo);
    }

    private void tryLoadUserAndLocalAmount(final String toshiId, final String encodedEthAmount) {
        if (toshiId != null) {
            getUserAndLocalAmount(toshiId, encodedEthAmount);
        } else {
            getLocalAmount(encodedEthAmount);
        }
    }

    private void getUserAndLocalAmount(final String toshiId, final String encodedEthAmount) {
        final Subscription sub =
                Single.zip(
                        getUserFromId(toshiId),
                        getLocalCurrency(encodedEthAmount),
                        Pair::new
                )
                .doOnSuccess(pair -> addPaymentAddressToBundle(pair.first))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        pair -> renderPaymentMessage(pair.first, pair.second),
                        this::handleUserError
                );

        this.subscriptions.add(sub);
    }

    private void addPaymentAddressToBundle(final User user) {
        if (user == null) return;
        this.bundle.putString(PaymentConfirmationDialog.PAYMENT_ADDRESS, user.getPaymentAddress());
    }

    private Single<User> getUserFromId(final String toshiId) {
        return BaseApplication
                .get()
                .getRecipientManager()
                .getUserFromToshiId(toshiId);
    }

    private void handleUserError(final Throwable throwable) {
        if (this.view == null) return;
        Toast.makeText(
                this.view.getContext(),
                this.view.getString(R.string.invalid_payment),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void getLocalAmount(final String encodedEthAmount) {
        final Subscription sub =
                getLocalCurrency(encodedEthAmount)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        localAmount -> renderPaymentMessage(null, localAmount),
                        this::handleUserError
                );

        this.subscriptions.add(sub);
    }

    private Single<String> getLocalCurrency(final String encodedEthAmount) {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(encodedEthAmount);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);

        return BaseApplication
                .get()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount);
    }

    private void renderPaymentMessage(final User user, final String localAmount) {
        final String displayNameOrPaymentAddress = user != null ? user.getDisplayName() : this.paymentAddress;
        final String ethAmount = this.view.getContext().getString(R.string.eth_amount, getEthAmount(this.encodedEthAmount));
        final @StringRes int messageRes = this.paymentType == PaymentType.TYPE_SEND
                ? R.string.payment_confirmation_body
                : R.string.payment_request_confirmation_body;
        final String messageBody = BaseApplication.get().getString(messageRes, localAmount, ethAmount, displayNameOrPaymentAddress);
        this.view.getBinding().message.setText(messageBody);
    }

    private String getEthAmount(final String encodedEthAmount) {
        final BigInteger eth = TypeConverter.StringHexToBigInteger(encodedEthAmount);
        return EthUtil.weiAmountToUserVisibleString(eth);
    }

    private void getPaymentTaskWithToshiId(final String toshiId, final String ethAmount) {
        final Subscription sub =
                Single.zip(
                        getWallet(),
                        getUserFromId(toshiId),
                        Pair::new
                )
                .flatMap(pair -> getPaymentTask(pair.first.getPaymentAddress(), pair.second.getPaymentAddress(), ethAmount))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this::showGasPriceLoadingState)
                .doOnSuccess(__ -> showGasPriceSuccessState())
                .doOnError(__ -> showGasPriceErrorState())
                .subscribe(
                        this::handlePaymentTask,
                        this::handlePaymentTaskError
                );

        this.subscriptions.add(sub);
    }

    private void getPaymentTaskWithPaymentAddress(final String toPaymentAddress, final String ethAmount) {
        final Subscription sub =
                getWallet()
                .flatMap(wallet -> getPaymentTask(wallet.getPaymentAddress(), toPaymentAddress, ethAmount))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this::showGasPriceLoadingState)
                .doOnSuccess(__ -> showGasPriceSuccessState())
                .doOnError(__ -> showGasPriceErrorState())
                .subscribe(
                        this::handlePaymentTask,
                        this::handlePaymentTaskError
                );

        this.subscriptions.add(sub);
    }

    private Single<HDWallet> getWallet() {
        return BaseApplication
                .get()
                .getToshiManager()
                .getWallet();
    }

    private Single<PaymentTask> getPaymentTask(final String fromPaymentAddress, final String toPaymentAddress, final String ethAmount) {
        return BaseApplication.get()
                .getTransactionManager()
                .buildPaymentTask(fromPaymentAddress, toPaymentAddress, ethAmount)
                .doOnSuccess(paymentTask -> this.paymentTask = paymentTask);
    }

    private void showGasPriceLoadingState() {
        disablePayButton();
        this.view.getBinding().pay.setVisibility(View.INVISIBLE);
        this.view.getBinding().progressBar.setVisibility(View.VISIBLE);
    }

    private void showGasPriceSuccessState() {
        enablePayButton();
        this.view.getBinding().pay.setVisibility(View.VISIBLE);
        this.view.getBinding().progressBar.setVisibility(View.GONE);
    }

    private void showGasPriceErrorState() {
        disablePayButton();
        this.view.getBinding().pay.setVisibility(View.VISIBLE);
        this.view.getBinding().progressBar.setVisibility(View.GONE);
    }

    private void handlePaymentTask(final PaymentTask paymentTask) {
        final GasPrice gasPrice = paymentTask.getGasPrice();
        if (gasPrice.getEthAmount().compareTo(BigDecimal.ZERO) == 0) {
            this.view.getBinding().gasInfo.setVisibility(View.GONE);
            return;
        }
        final String ethAmount = EthUtil.ethAmountToUserVisibleString(gasPrice.getEthAmount());
        final String messageBody = BaseApplication.get().getString(R.string.gas_price_message, gasPrice.getLocalAmount(), ethAmount);
        final @ColorInt int errorColor = ContextCompat.getColor(this.view.getContext(), R.color.textColorSecondary);
        final TextView gasView = this.view.getBinding().gasInfo;
        gasView.setTextColor(errorColor);
        gasView.setVisibility(View.VISIBLE);
        gasView.setText(messageBody);
    }

    private void enablePayButton() {
        final TextView pay = this.view.getBinding().pay;
        pay.setVisibility(View.VISIBLE);
        pay.setTextColor(ContextCompat.getColor(this.view.getContext(), R.color.colorPrimary));
        pay.setClickable(true);
    }

    private void disablePayButton() {
        final TextView pay = this.view.getBinding().pay;
        pay.setVisibility(View.INVISIBLE);
        pay.setTextColor(ContextCompat.getColor(this.view.getContext(), R.color.colorPrimarySemiTransparent));
        pay.setClickable(false);
    }

    private void handlePaymentTaskError(final Throwable throwable) {
        LogUtil.e(getClass(), "Error " + throwable);
        final String errorMessage = this.view.getString(R.string.gas_price_error);
        final @ColorInt int errorColor = ContextCompat.getColor(this.view.getContext(), R.color.error_color);
        final TextView gasView = this.view.getBinding().gasInfo;
        gasView.setTextColor(errorColor);
        gasView.setVisibility(View.VISIBLE);
        gasView.setText(errorMessage);
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.view = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.view = null;
    }
}
