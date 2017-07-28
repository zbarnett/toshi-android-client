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

import com.toshi.R;
import com.toshi.crypto.util.TypeConverter;
import com.toshi.util.EthUtil;
import com.toshi.util.LogUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.ScannerActivity;
import com.toshi.view.activity.SendActivity;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class SendPresenter implements Presenter<SendActivity> {

    public static final String EXTRA__INTENT = "extraIntent";
    private static final int QR_REQUEST_CODE = 200;

    private SendActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;
    private String encodedEthAmount;


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
        initClickListeners();
        processIntentData();
    }

    private void initClickListeners() {
        this.activity.getBinding().scan.setOnClickListener(__ -> startScanQrActivity());
    }

    private void startScanQrActivity() {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, ScannerActivity.class);
        this.activity.startActivityForResult(intent, QR_REQUEST_CODE);
    }

    private void processIntentData() {
        final Intent amountIntent = this.activity.getIntent().getParcelableExtra(EXTRA__INTENT);
        this.encodedEthAmount = amountIntent.getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
        renderAmounts();
    }

    private void renderAmounts() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);

        final Subscription sub = BaseApplication
                .get()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount)
                .subscribe(
                        this::renderBalance,
                        throwable -> LogUtil.exception(getClass(), throwable)
                );

        this.subscriptions.add(sub);
    }

    private void renderBalance(final String amount) {
        if (this.activity == null) return;
        final String usdEth = this.activity.getString(R.string.local_dot_eth_amount, amount, getEthValue());
        this.activity.getBinding().amount.setText(usdEth);
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

    private String getEthValue() {
        final BigInteger eth = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        return EthUtil.weiAmountToUserVisibleString(eth);
    }
}
