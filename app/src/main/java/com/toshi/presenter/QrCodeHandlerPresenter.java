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

import android.net.Uri;
import android.widget.Toast;

import com.toshi.R;
import com.toshi.util.QrCodeHandler;
import com.toshi.util.ScannerResultType;
import com.toshi.view.activity.QrCodeHandlerActivity;

import rx.subscriptions.CompositeSubscription;

public class QrCodeHandlerPresenter implements Presenter<QrCodeHandlerActivity> {

    private QrCodeHandlerActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;
    private QrCodeHandler qrCodeHandler;

    @Override
    public void onViewAttached(QrCodeHandlerActivity view) {
        this.activity = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        initQrCodeHandler();
        processIntentData();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void initQrCodeHandler() {
        this.qrCodeHandler = new QrCodeHandler(this.activity, ScannerResultType.NO_ACTION);
        this.qrCodeHandler.setOnQrCodeHandlerListener(this::handleInvalidQrCode);
    }

    private void handleInvalidQrCode() {
        Toast.makeText(
                this.activity,
                this.activity.getString(R.string.invalid_qr_code),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void processIntentData() {
        final Uri data = this.activity.getIntent().getData();
        this.qrCodeHandler.handleResult(data.toString());
    }

    @Override
    public void onViewDetached() {
        this.qrCodeHandler.clear();
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.qrCodeHandler = null;
        this.subscriptions = null;
        this.activity = null;
    }
}
