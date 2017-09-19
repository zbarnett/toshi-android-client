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

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import com.toshi.BuildConfig;
import com.toshi.R;
import com.toshi.model.local.Network;
import com.toshi.model.local.Networks;
import com.toshi.util.BuildTypes;
import com.toshi.util.DialogUtil;
import com.toshi.util.LogUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.AdvancedSettingsActivity;
import com.toshi.view.activity.LicenseListActivity;
import com.toshi.view.fragment.DialogFragment.NetworkSwitcherDialog;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class AdvancedSettingsPresenter implements Presenter<AdvancedSettingsActivity> {

    private AdvancedSettingsActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    private Dialog infoDialog;
    private NetworkSwitcherDialog networkDialog;
    private boolean onGoingTask = false;

    @Override
    public void onViewAttached(AdvancedSettingsActivity view) {
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
        initCLickListeners();
        setVersionName();
        setNetworkSwitcherVisibility();
        setCurrentNetwork(Networks.getInstance().getCurrentNetwork());
    }

    private void initCLickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().currentNetworkWrapper.setOnClickListener(__ -> handleCurrentNetworkClicked());
        this.activity.getBinding().openSourceLicenses.setOnClickListener(this::handleOpenSourceLicencesClicked);
    }

    private void handleCurrentNetworkClicked() {
        this.infoDialog = DialogUtil.getBaseDialog(
                this.activity,
                R.string.network_dialog_title,
                R.string.network_dialog_message,
                R.string.continue_,
                (dialog, which) -> showNetworkSwitchDialog()
        ).show();
    }

    private void showNetworkSwitchDialog() {
        this.networkDialog = new NetworkSwitcherDialog()
                .setOnNetworkListener(this::changeNetwork);
        this.networkDialog.show(this.activity.getSupportFragmentManager(), NetworkSwitcherDialog.TAG);
    }

    private void setVersionName() {
        final String versionName = BuildConfig.VERSION_NAME;
        final String appVersion = this.activity.getString(R.string.app_version, versionName);
        this.activity.getBinding().version.setText(appVersion);
    }

    private void setNetworkSwitcherVisibility() {
        final int visibility = BuildConfig.BUILD_TYPE.equals(BuildTypes.DEBUG)
                ? View.VISIBLE
                : View.GONE;
        this.activity.getBinding().networkSwitcherWrapper.setVisibility(visibility);
    }

    private void setCurrentNetwork(final Network network) {
        this.activity.getBinding().currentNetwork.setText(network.getName());
    }

    private void changeNetwork(final Network network) {
        if (this.onGoingTask) return;
        startLoadingTask();

        final Subscription sub =
                BaseApplication
                .get()
                .getBalanceManager()
                .changeNetwork(network)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> handleNetworkChange(network),
                        this::handleNetworkChangeError
                );

        this.subscriptions.add(sub);
    }

    private void startLoadingTask() {
        if (this.activity == null) return;
        this.onGoingTask = true;
        this.activity.getBinding().loadingSpinner.setVisibility(View.VISIBLE);
    }

    private void handleNetworkChange(final Network network) {
        setCurrentNetwork(network);
        BaseApplication
                .get()
                .getBalanceManager()
                .refreshBalance();
        showToast(R.string.network_changed);
        stopLoadingTask();
    }

    private void handleNetworkChangeError(final Throwable throwable) {
        showToast(R.string.network_change_error);
        LogUtil.exception(getClass(), throwable);
        stopLoadingTask();
    }

    private void showToast(final @StringRes int stringRes) {
        if (this.activity == null) return;
        Toast.makeText(
                this.activity,
                stringRes,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void stopLoadingTask() {
        this.onGoingTask = false;
        if (this.activity == null) return;
        this.activity.getBinding().loadingSpinner.setVisibility(View.GONE);
    }

    private void handleOpenSourceLicencesClicked(final View v) {
        final Intent intent = new Intent(this.activity, LicenseListActivity.class);
        this.activity.startActivity(intent);
    }

    @Override
    public void onViewDetached() {
        closeDialogs();
        this.subscriptions.clear();
        this.activity = null;
    }

    private void closeDialogs() {
        if (this.infoDialog != null) {
            this.infoDialog.dismiss();
            this.infoDialog = null;
        }

        if (this.networkDialog != null) {
            this.networkDialog.dismissAllowingStateLoss();
            this.networkDialog = null;
        }
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.activity = null;
    }
}
