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

package com.toshi.util;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.toshi.R;
import com.toshi.model.local.Review;
import com.toshi.model.network.ServerTime;
import com.toshi.view.BaseApplication;
import com.toshi.view.fragment.DialogFragment.RateDialog;

import rx.Completable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class RatingHandler {

    private AppCompatActivity activity;
    private CompositeSubscription subscriptions;

    private RateDialog rateDialog;
    private String userAddress;

    public RatingHandler(final AppCompatActivity activity, final String userAddress) {
        this.activity = activity;
        this.userAddress = userAddress;
        this.subscriptions = new CompositeSubscription();
        this.rateDialog = (RateDialog) this.activity.getSupportFragmentManager().findFragmentByTag(RateDialog.TAG);
        if (this.rateDialog != null) {
            this.rateDialog.setOnRateDialogClickListener(this::addReview);
        }
    }

    public void rateUser() {
        this.rateDialog = RateDialog.newInstance();
        this.rateDialog.setOnRateDialogClickListener(this::addReview);
        this.rateDialog.show(this.activity.getSupportFragmentManager(), RateDialog.TAG);
    }

    private void addReview(final int rating, final String reviewText) {
        final Review review = new Review()
                .setRating(rating)
                .setReview(reviewText)
                .setReviewee(this.userAddress);

        final Subscription sub =
                submitReview(review)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleSubmitSuccess,
                        throwable -> LogUtil.exception(getClass(), "Error when sending review", throwable)
                );

        this.subscriptions.add(sub);
    }

    private Completable submitReview(final Review review) {
        return BaseApplication
                .get()
                .getRecipientManager()
                .getTimestamp()
                .flatMapCompletable(serverTime -> submitReview(review, serverTime));
    }

    private Completable submitReview(final Review review, final ServerTime serverTime) {
        return BaseApplication.get()
                .getReputationManager()
                .submitReview(review, serverTime.get())
                .toCompletable();
    }

    private void handleSubmitSuccess() {
        if (this.activity == null) return;
        Toast.makeText(
                this.activity,
                this.activity.getString(R.string.review_submitted),
                Toast.LENGTH_SHORT
        ).show();
    }

    public void clear() {
        closeDialog();
        this.subscriptions.clear();
        this.subscriptions = null;
        this.activity = null;
    }

    private void closeDialog() {
        if (this.rateDialog != null) {
            this.rateDialog.dismissAllowingStateLoss();
            this.rateDialog = null;
        }
    }
}
