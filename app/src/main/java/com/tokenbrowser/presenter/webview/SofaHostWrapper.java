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

package com.tokenbrowser.presenter.webview;


import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.model.local.UnsignedW3Transaction;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.presenter.webview.model.ApproveTransactionCallback;
import com.tokenbrowser.presenter.webview.model.GetAccountsCallback;
import com.tokenbrowser.presenter.webview.model.SignTransactionCallback;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.fragment.DialogFragment.PaymentConfirmationDialog;
import com.tokenbrowser.view.fragment.DialogFragment.WebPaymentConfirmationListener;

import java.io.IOException;

/* package */ class SofaHostWrapper implements SofaHostListener {

    private final AppCompatActivity activity;
    private final WebView webView;
    private final SOFAHost sofaHost;
    private final HDWallet wallet;
    private PaymentConfirmationDialog paymentConfirmationDialog;

    /* package */ SofaHostWrapper(final AppCompatActivity activity, final WebView webView) {
        this.activity = activity;
        this.webView = webView;
        this.sofaHost = new SOFAHost(this);
        this.wallet = BaseApplication
                        .get()
                        .getTokenManager()
                        .getWallet()
                        .toBlocking()
                        .value();
    }

    /* package */ SOFAHost getSofaHost() {
        return this.sofaHost;
    }

    public void getAccounts(final String id) {
        final GetAccountsCallback callback =
                new GetAccountsCallback().setResult(this.wallet.getPaymentAddress());

        doCallBack(id, callback.toJsonEncodedString());
    }

    public void approveTransaction(final String id, final String unsignedTransaction) {
        final boolean shouldApprove = shouldApproveTransaction(unsignedTransaction);
        final ApproveTransactionCallback callback =
                new ApproveTransactionCallback()
                        .setResult(shouldApprove);
        doCallBack(id, callback.toJsonEncodedString());
    }

    private boolean shouldApproveTransaction(final String unsignedTransaction) {
        final UnsignedW3Transaction transaction;
        try {
            transaction = SofaAdapters.get().unsignedW3TransactionFrom(unsignedTransaction);
        } catch (final IOException e) {
            LogUtil.exception(getClass(), "Unable to parse unsigned transaction. ", e);
            return false;
        }

        return transaction.getFrom().equals(this.wallet.getPaymentAddress());
    }

    public void signTransaction(final String id, final String unsignedTransaction) {
        final UnsignedW3Transaction transaction;
        try {
            transaction = SofaAdapters.get().unsignedW3TransactionFrom(unsignedTransaction);
        } catch (final IOException e) {
            LogUtil.exception(getClass(), "Unable to parse unsigned transaction. ", e);
            return;
        }
        if (this.activity == null) return;
        this.paymentConfirmationDialog =
                PaymentConfirmationDialog
                        .newInstanceWebPayment(
                                unsignedTransaction,
                                transaction.getTo(),
                                transaction.getValue(),
                                id,
                                null
                        );
        this.paymentConfirmationDialog.show(this.activity.getSupportFragmentManager(), PaymentConfirmationDialog.TAG);
        this.paymentConfirmationDialog.setOnPaymentConfirmationListener(this.confirmationListener);
    }

    /* package */ void destroy() {
        if (this.paymentConfirmationDialog != null) {
            this.paymentConfirmationDialog.dismiss();
            this.paymentConfirmationDialog = null;
        }
    }

    private final WebPaymentConfirmationListener confirmationListener = new WebPaymentConfirmationListener() {
        @Override
        public void onWebPaymentApproved(final String callbackId, final String unsignedTransaction) {
            final String signedTransaction = wallet.signTransaction(unsignedTransaction);
            final SignTransactionCallback callback =
                    new SignTransactionCallback()
                            .setResult(signedTransaction);
            doCallBack(callbackId, callback.toJsonEncodedString());
        }
    };

    private void doCallBack(final String id, final String encodedCallback) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (activity == null) return;
            final String methodCall = String.format("SOFA.callback(\"%s\",\"%s\")", id, encodedCallback);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(methodCall, null);
            } else {
                webView.loadUrl("javascript:" + methodCall);
            }
        });

    }
}
