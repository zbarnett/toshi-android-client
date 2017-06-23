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

package com.tokenbrowser.util;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.Report;
import com.tokenbrowser.view.BaseApplication;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class UserReportingHandler {

    private Context context;
    private CompositeSubscription subscriptions;

    private Dialog reportDialog;
    private AlertDialog confirmationDialog;
    private String userAddress;

    public UserReportingHandler(final Context context) {
        this.context = context;
        this.subscriptions = new CompositeSubscription();
    }

    public UserReportingHandler setUserAddress(final String userAddress) {
        this.userAddress = userAddress;
        return this;
    }

    public void showReportDialog() {
        this.reportDialog = new Dialog(this.context);
        this.reportDialog.setContentView(R.layout.view_report_dialog);
        this.reportDialog.findViewById(R.id.spam).setOnClickListener(__ -> reportSpam());
        this.reportDialog.findViewById(R.id.inappropriate).setOnClickListener(__ -> reportInappropriate());
        this.reportDialog.show();
    }

    private void reportSpam() {
        final String details = this.context.getString(R.string.report_spam);
        final Report report = new Report()
                .setUserAddress(this.userAddress)
                .setDetails(details);

        reportUser(report);
    }

    private void reportInappropriate() {
        final String details = this.context.getString(R.string.report_inappropriate);
        final Report report = new Report()
                .setUserAddress(this.userAddress)
                .setDetails(details);

       reportUser(report);
    }

    private void reportUser(final Report report) {
        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .getRecipientManager()
                .reportUser(report)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        __ ->  showConfirmationDialog(),
                        this::handleError
                );

        this.subscriptions.add(sub);
    }

    private void showConfirmationDialog() {
        if (this.context == null) return;

        if (this.reportDialog != null) {
            this.reportDialog.dismiss();
        }

        final AlertDialog.Builder builder =
                DialogUtil.getBaseDialog(
                        this.context,
                        R.string.report_confirmation_title,
                        R.string.report_confirmation_message,
                        R.string.dismiss
                );

        this.confirmationDialog = builder.create();
        this.confirmationDialog.show();
    }

    private void handleError(final Throwable throwable) {
        if (this.context == null) return;

        if (this.reportDialog != null) {
            this.reportDialog.dismiss();
        }

        Toast.makeText(
                this.context,
                this.context.getString(R.string.report_error),
                Toast.LENGTH_SHORT
        ).show();
    }

    public void clear() {
        closeDialogs();
        this.subscriptions.clear();
        this.subscriptions = null;
        this.context = null;
    }

    private void closeDialogs() {
        if (this.reportDialog != null) {
            this.reportDialog.dismiss();
            this.reportDialog = null;
        }

        if (this.confirmationDialog != null) {
            this.confirmationDialog.dismiss();
            this.confirmationDialog = null;
        }
    }
}
