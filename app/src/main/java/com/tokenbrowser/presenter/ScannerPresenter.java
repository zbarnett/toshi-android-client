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

package com.tokenbrowser.presenter;

import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.tokenbrowser.R;
import com.tokenbrowser.model.local.PermissionResultHolder;
import com.tokenbrowser.model.local.ScanResult;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.QrCodeHandler;
import com.tokenbrowser.view.activity.ScannerActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Completable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public final class ScannerPresenter implements Presenter<ScannerActivity> {

    private CaptureManager capture;
    private ScannerActivity activity;
    private CompositeSubscription subscriptions;

    private QrCodeHandler qrCodeHandler;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final ScannerActivity activity) {
        this.activity = activity;

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
        initCloseButton();
        initQrCodeHandler();
        initScanner();
    }

    private void initCloseButton() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> activity.finish());
    }

    private void initScanner() {
        if (this.capture == null) {
            this.capture = new CaptureManager(this.activity, this.activity.getBinding().scanner);
        }

        decodeQrCode();
        this.capture.onResume();
    }

    private void initQrCodeHandler() {
        this.qrCodeHandler = new QrCodeHandler(this.activity);
        this.qrCodeHandler.setOnQrCodeHandlerListener(this::handleInvalidQrCode);
    }

    private void handleInvalidQrCode() {
        decodeQrCodeDelayed();
        Toast.makeText(
                this.activity,
                this.activity.getString(R.string.invalid_qr_code),
                Toast.LENGTH_SHORT
        ).show();
    }

    private final BarcodeCallback onScanSuccess = new BarcodeCallback() {
        @Override
        public void barcodeResult(final BarcodeResult result) {
            final ScanResult scanResult = new ScanResult(result);
            qrCodeHandler.handleResult(scanResult.getText());
        }

        @Override
        public void possibleResultPoints(final List<ResultPoint> resultPoints) {}
    };

    private void decodeQrCodeDelayed() {
        final Subscription sub =
                Completable
                .timer(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::decodeQrCode);

        this.subscriptions.add(sub);
    }

    private void decodeQrCode() {
        if (this.activity == null) return;
        this.activity.getBinding().scanner.decodeSingle(this.onScanSuccess);
    }

    public void handlePermissionsResult(final PermissionResultHolder prh) {
        this.capture.onRequestPermissionsResult(prh.getRequestCode(), prh.getPermissions(), prh.getGrantResults());
    }

    @Override
    public void onViewDetached() {
        try {
            if (this.capture != null) {
                this.capture.onPause();
            }
        } catch (final IllegalArgumentException ex) {
            LogUtil.exception(getClass(), ex);
        }

        this.qrCodeHandler.clear();
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        try {
            if (this.capture != null) {
                this.capture.onDestroy();
            }
        } catch (final IllegalArgumentException ex) {
            LogUtil.exception(getClass(), ex);
        }
        
        this.qrCodeHandler = null;
        this.activity = null;
    }
}
