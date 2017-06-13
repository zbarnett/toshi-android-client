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

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tokenbrowser.R;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.GroupParticipantsActivity;
import com.tokenbrowser.view.adapter.GroupParticipantAdapter;
import com.tokenbrowser.view.custom.HorizontalLineDivider;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class GroupParticipantsPresenter implements Presenter<GroupParticipantsActivity> {

    private GroupParticipantsActivity activity;
    private CompositeSubscription subscriptions;

    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(GroupParticipantsActivity view) {
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
        initRecyclerView();
        initSearch();
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.activity.getBinding().searchResults;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final GroupParticipantAdapter adapter = new GroupParticipantAdapter();
        recyclerView.setAdapter(adapter);

        final int dividerLeftPadding = this.activity.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + this.activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.activity, R.color.divider))
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void initSearch() {
        final Subscription sub =
                RxTextView
                .textChangeEvents(this.activity.getBinding().userInput)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(event -> event.text().toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::submitQuery,
                        this::handleSearchError
                );

        this.subscriptions.add(sub);
    }

    private void submitQuery(final String query) {
        if (query.length() < 3) {
            this.getParticipantsAdapter().clear();
            return;
        }

        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .searchOnlineUsers(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        users -> getParticipantsAdapter().setUsers(users),
                        this::handleSearchError
                );

        this.subscriptions.add(sub);
    }

    private void handleSearchError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error while searching for user", throwable);
    }

    private GroupParticipantAdapter getParticipantsAdapter() {
        return (GroupParticipantAdapter) this.activity.getBinding().searchResults.getAdapter();
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.activity = null;
    }
}
