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

package com.toshi.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.toshi.R;
import com.toshi.view.BaseApplication;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class UserBlockingHandler {

    public interface OnBlockingListener {
        void onUserBlocked();
        void onUserUnblocked();
    }

    private CompositeSubscription subscriptions;
    private Context context;
    private OnBlockingListener listener;
    private String userAddress;

    private AlertDialog blockedUserDialog;
    private AlertDialog confirmationDialog;

    public UserBlockingHandler(final Context context) {
        this.context = context;
        this.subscriptions = new CompositeSubscription();
    }

    public UserBlockingHandler setUserAddress(final String userAddress) {
        this.userAddress = userAddress;
        return this;
    }

    public UserBlockingHandler setOnBlockingListener(final OnBlockingListener listener) {
        this.listener = listener;
        return this;
    }

    public void blockOrUnblockUser() {
        final Subscription sub =
                isUserBlocked(this.userAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showDialog,
                        this::handleError
                );

        this.subscriptions.add(sub);
    }

    private Single<Boolean> isUserBlocked(final String ownerAddress) {
        return BaseApplication
                .get()
                .getRecipientManager()
                .isUserBlocked(ownerAddress)
                .subscribeOn(Schedulers.io());
    }

    private void showDialog(final boolean isBlocked) {
        if (isBlocked) {
            showUnblockedDialog();
        } else {
            showBlockedDialog();
        }
    }

    private void showUnblockedDialog() {
        showBlockingDialog(
                R.string.unblock_user_dialog_title,
                R.string.unblock_user_dialog_message,
                R.string.unblock,
                R.string.cancel,
                (dialog, which) -> {
                    unblockUser();
                    dialog.dismiss();
                });
    }

    private void showBlockedDialog() {
        showBlockingDialog(
                R.string.block_user_dialog_title,
                R.string.block_user_dialog_message,
                R.string.block,
                R.string.cancel,
                (dialog, which) -> {
                    blockUser();
                    dialog.dismiss();
                });
    }

    private void showBlockingDialog(final @StringRes int title,
                                    final @StringRes int message,
                                    final @StringRes int positiveButtonText,
                                    final @StringRes int negativeButtonText,
                                    final DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder builder =
                DialogUtil.getBaseDialog(
                        this.context,
                        title,
                        message,
                        positiveButtonText,
                        negativeButtonText,
                        listener
                );

        this.blockedUserDialog = builder.create();
        this.blockedUserDialog.show();
    }

    private void unblockUser() {
        final Subscription sub =
                BaseApplication
                .get()
                .getRecipientManager()
                .unblockUser(this.userAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleUnblockUser,
                        this::handleError
                );

        this.subscriptions.add(sub);
    }

    private void handleUnblockUser() {
        showConfirmationDialog(
                R.string.unblock_user_confirmation_dialog_title,
                R.string.unblock_user_confirmation_dialog_message,
                R.string.dismiss
        );
        this.listener.onUserUnblocked();
    }

    private void blockUser() {
        final Subscription sub =
                BaseApplication
                .get()
                .getRecipientManager()
                .blockUser(this.userAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleBlockerUser,
                        this::handleError
                );

        this.subscriptions.add(sub);
    }

    private void handleBlockerUser() {
        showConfirmationDialog(
                R.string.block_user_confirmation_dialog_title,
                R.string.block_user_confirmation_dialog_message,
                R.string.dismiss
        );
        this.listener.onUserBlocked();
    }

    private void showConfirmationDialog(final @StringRes int title,
                                        final @StringRes int message,
                                        final @StringRes int negativeButtonText) {
        final AlertDialog.Builder baseDialog =
                DialogUtil.getBaseDialog(
                        this.context,
                        title,
                        message,
                        negativeButtonText
                );

        this.confirmationDialog = baseDialog.create();
        this.confirmationDialog.show();
    }

    private void handleError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error during blocking", throwable);
    }

    public void clear() {
        closeDialogs();
        this.subscriptions.clear();
        this.subscriptions = null;
        this.context = null;
    }

    private void closeDialogs() {
        if (this.blockedUserDialog != null) {
            this.blockedUserDialog.dismiss();
            this.blockedUserDialog = null;
        }

        if (this.confirmationDialog != null) {
            this.confirmationDialog.dismiss();
            this.confirmationDialog = null;
        }
    }
}
