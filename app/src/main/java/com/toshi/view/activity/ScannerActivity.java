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

package com.toshi.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.toshi.R;
import com.toshi.databinding.ActivityScannerBinding;
import com.toshi.model.local.PermissionResultHolder;
import com.toshi.presenter.LoaderIds;
import com.toshi.presenter.ScannerPresenter;
import com.toshi.presenter.factory.PresenterFactory;
import com.toshi.presenter.factory.ScannerPresenterFactory;
import com.toshi.view.custom.OfflineViewRenderer;

public class ScannerActivity
        extends OfflineViewBasePresenterActivity<ScannerPresenter, ScannerActivity>
        implements OfflineViewRenderer {

    public static final String SCANNER_RESULT_TYPE = "scanner_result_type";

    private ScannerPresenter presenter;
    private ActivityScannerBinding binding;
    private PermissionResultHolder resultHolder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_scanner);
    }

    public ActivityScannerBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ScannerPresenter> getPresenterFactory() {
        return new ScannerPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ScannerPresenter presenter) {
        this.presenter = presenter;
        tryHandlePermissionResultHolder();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        this.resultHolder = new PermissionResultHolder(requestCode, permissions, grantResults);
        tryHandlePermissionResultHolder();
    }

    private void tryHandlePermissionResultHolder() {
        if (this.resultHolder == null || this.presenter == null) return;
        this.presenter.handlePermissionsResult(this.resultHolder);
        this.resultHolder = null;
    }

    @Override
    protected int loaderId() {
        return LoaderIds.get(this.getClass().getCanonicalName());
    }

    @Override
    public View getOfflineViewContainer() {
        return this.binding.getRoot();
    }
}