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


import android.support.v7.app.AppCompatActivity;

import com.tokenbrowser.R;
import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.model.local.UnsignedW3Transaction;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.fragment.DialogFragment.PaymentConfirmationDialog;
import com.tokenbrowser.view.fragment.DialogFragment.WebPaymentConfirmationListener;

import java.io.IOException;

/* package */ class SofaHostWrapper implements SofaHostListener {

    private final AppCompatActivity activity;
    private final SOFAHost sofaHost;
    private final HDWallet wallet;
    private PaymentConfirmationDialog paymentConfirmationDialog;

    /* package */ SofaHostWrapper(final AppCompatActivity activity) {
        this.activity = activity;
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

    public String getRcpUrl() {
        return BaseApplication.get().getResources().getString(R.string.rcp_url);
    }

    public String getAccounts() {
        return "[\"" + this.wallet.getPaymentAddress() + "\"]";
    }

    public boolean approveTransaction(final String unsignedTransaction) {
        final UnsignedW3Transaction transaction;
        try {
            transaction = SofaAdapters.get().unsignedW3TransactionFrom(unsignedTransaction);
        } catch (final IOException e) {
            LogUtil.exception(getClass(), "Unable to parse unsigned transaction. ", e);
            return false;
        }

        return transaction.getFrom().equals(this.wallet.getPaymentAddress());
    }

    public void signTransaction(final String unsignedTransaction) {
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
        public void onWebPaymentApproved(final String unsignedTransaction) {
            final String signedTransaction = wallet.signTransaction(unsignedTransaction);
            // To Do -- pass the signed transaction back to webview
        }
    };
}
