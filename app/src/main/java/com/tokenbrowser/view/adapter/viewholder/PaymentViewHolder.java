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

package com.tokenbrowser.view.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.view.BaseApplication;

public final class PaymentViewHolder extends RecyclerView.ViewHolder {

    private @NonNull LinearLayout wrapper;
    private @NonNull TextView requestedAmount;
    private @NonNull TextView sender;
    private @NonNull TextView receiver;
    private @NonNull TextView failedMessge;

    private Payment payment;
    private User remoteUser;
    private @SendState.State int sendState;

    public PaymentViewHolder(final View v) {
        super(v);
        this.wrapper = (LinearLayout) v.findViewById(R.id.payment_wrapper);
        this.requestedAmount = (TextView) v.findViewById(R.id.requested_amount);
        this.sender = (TextView) v.findViewById(R.id.sender);
        this.receiver = (TextView) v.findViewById(R.id.receiver);
        this.failedMessge = (TextView) v.findViewById(R.id.failed_message);
    }

    public PaymentViewHolder setPayment(final Payment payment) {
        this.payment = payment;
        return this;
    }

    public PaymentViewHolder setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    public PaymentViewHolder setRemoteUser(final User remoteUser) {
        this.remoteUser = remoteUser;
        return this;
    }

    public void draw() {
        renderAmounts();
        renderPaymentStatus();
        renderReceiverAndSender();

        this.payment = null;
    }

    private void renderAmounts() {
        this.requestedAmount.setText(this.payment.getLocalPrice());
    }

    private void renderReceiverAndSender() {
        if (this.remoteUser == null) return;
        final String remoteName = this.remoteUser.getDisplayName();
        final boolean isSentByRemote = this.remoteUser.getPaymentAddress().equals(this.payment.getFromAddress());
        final String sender = isSentByRemote ? remoteName : BaseApplication.get().getString(R.string.you_uppercase);
        final String receiver = isSentByRemote ? BaseApplication.get().getString(R.string.you_lowercase) : remoteName;

        this.sender.setText(sender);
        this.receiver.setText(receiver);
    }

    private void renderPaymentStatus() {
        switch (this.sendState) {
            case SendState.STATE_FAILED:
                showFailedMessage();
                break;
            case SendState.STATE_SENDING:
            case SendState.STATE_SENT:
                showSuccessMessage();
                break;
            case SendState.STATE_PENDING:
            case SendState.STATE_RECEIVED:
            case SendState.STATE_LOCAL_ONLY:
            default:
                showSuccessMessage();
                break;
        }
    }

    private void showFailedMessage() {
        this.wrapper.setVisibility(View.GONE);
        this.failedMessge.setVisibility(View.VISIBLE);
    }

    private void showSuccessMessage() {
        this.wrapper.setVisibility(View.VISIBLE);
        this.failedMessge.setVisibility(View.GONE);
    }
}
