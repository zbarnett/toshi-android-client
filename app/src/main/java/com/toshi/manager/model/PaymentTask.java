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

package com.toshi.manager.model;

import android.support.annotation.IntDef;

import com.toshi.model.local.GasPrice;
import com.toshi.model.local.User;
import com.toshi.model.network.SentTransaction;
import com.toshi.model.network.UnsignedTransaction;
import com.toshi.model.sofa.Payment;
import com.toshi.model.sofa.SofaMessage;

public class PaymentTask {


    @IntDef({INCOMING, OUTGOING, OUTGOING_EXTERNAL, OUTGOING_RESEND})
    public @interface Action {}
    public static final int INCOMING = 0;
    public static final int OUTGOING = 1;
    public static final int OUTGOING_EXTERNAL = 2;
    public static final int OUTGOING_RESEND = 3;

    private User user;
    private Payment payment;
    private @Action int action;
    private UnsignedTransaction unsignedTransaction;
    private SofaMessage sofaMessage;
    private SentTransaction sentTransaction;
    private GasPrice gasPrice;

    public PaymentTask(final Payment payment,
                       final UnsignedTransaction unsignedTransaction,
                       final GasPrice gasPrice) {
        this.payment = payment;
        this.unsignedTransaction = unsignedTransaction;
        this.gasPrice = gasPrice;
    }

    public PaymentTask(final UnsignedTransaction unsignedTransaction,
                       final GasPrice gasPrice) {
        this.unsignedTransaction = unsignedTransaction;
        this.gasPrice = gasPrice;
    }

    public PaymentTask(
            final User user,
            final Payment payment,
            final @Action int action) {
        this.user = user;
        this.payment = payment;
        this.action = action;
    }

    public User getUser() {
        return this.user;
    }

    public PaymentTask setUser(final User user) {
        this.user = user;
        return this;
    }

    public Payment getPayment() {
        return this.payment;
    }

    public PaymentTask setPayment(final Payment payment) {
        this.payment = payment;
        return this;
    }

    public int getAction() {
        return this.action;
    }

    public PaymentTask setAction(final @Action int action) {
        this.action = action;
        return this;
    }

    public UnsignedTransaction getUnsignedTransaction() {
        return unsignedTransaction;
    }

    public SofaMessage getSofaMessage() {
        return sofaMessage;
    }

    public PaymentTask setSofaMessage(final SofaMessage sofaMessage) {
        this.sofaMessage = sofaMessage;
        return this;
    }

    public SentTransaction getSentTransaction() {
        return sentTransaction;
    }

    public PaymentTask setSentTransaction(final SentTransaction sentTransaction) {
        this.sentTransaction = sentTransaction;
        return this;
    }

    public GasPrice getGasPrice() {
        return this.gasPrice;
    }
}
