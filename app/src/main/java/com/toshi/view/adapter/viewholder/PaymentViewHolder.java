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

package com.toshi.view.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.toshi.R;
import com.toshi.model.local.SendState;
import com.toshi.model.sofa.Payment;
import com.toshi.util.EthUtil;
import com.toshi.util.ImageUtil;
import com.toshi.view.BaseApplication;

public final class PaymentViewHolder extends RecyclerView.ViewHolder {

    private @Nullable ImageView avatar;
    private @NonNull TextView title;
    private @NonNull TextView ethereumAmount;
    private @NonNull TextView body;
    private @Nullable ImageView sendStatus;

    private Payment payment;
    private @SendState.State int sendState;
    private String avatarUri;

    public PaymentViewHolder(final View v) {
        super(v);
        this.avatar = (ImageView) v.findViewById(R.id.avatar);
        this.title = (TextView) v.findViewById(R.id.title);
        this.ethereumAmount = (TextView) v.findViewById(R.id.eth_amount);
        this.body = (TextView) v.findViewById(R.id.body);
        this.sendStatus = (ImageView) v.findViewById(R.id.sent_status);
    }

    public PaymentViewHolder setPayment(final Payment payment) {
        this.payment = payment;
        return this;
    }

    public PaymentViewHolder setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    public PaymentViewHolder setAvatarUri(final String avatarUri) {
        this.avatarUri = avatarUri;
        return this;
    }

    public void draw() {
        renderAmounts();
        renderAvatar();
        setSendState();
    }

    private void renderAmounts() {
        final String requestTitle = BaseApplication.get().getString(R.string.payment_for_value, this.payment.getLocalPrice());
        this.title.setText(requestTitle);
        final String ethAmount = String.format(
                BaseApplication.get().getResources().getString(R.string.eth_amount),
                EthUtil.hexAmountToUserVisibleString(this.payment.getValue()));
        this.ethereumAmount.setText(ethAmount);
    }

    private void renderAvatar() {
        if (this.avatar == null) return;
        ImageUtil.load(this.avatarUri, this.avatar);
    }

    private void setSendState() {
        if (this.sendStatus == null) return;
        final int visibility = this.sendState == SendState.STATE_FAILED || this.sendState == SendState.STATE_PENDING
                ? View.VISIBLE
                : View.GONE;
        this.sendStatus.setVisibility(visibility);
    }
}
