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

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.toshi.R;
import com.toshi.model.local.Contact;
import com.toshi.model.local.User;
import com.toshi.util.KeyboardUtil;
import com.toshi.util.LogUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.ContactSearchActivity;
import com.toshi.view.activity.ViewUserActivity;
import com.toshi.view.adapter.UserAdapter;
import com.toshi.view.custom.HorizontalLineDivider;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.ConnectableObservable;
import rx.subscriptions.CompositeSubscription;

public class ContactSearchPresenter implements Presenter<ContactSearchActivity> {

    private ContactSearchActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final ContactSearchActivity view) {
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

    public void initShortLivingObjects() {
        initClickListeners();
        initRecyclerView();
        initSearch();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseClicked);
        this.activity.getBinding().clearButton.setOnClickListener(__ -> this.activity.getBinding().search.setText(null));
    }

    private void handleCloseClicked(final View v) {
        if (v == null || this.activity == null) return;
        KeyboardUtil.hideKeyboard(v);
        this.activity.onBackPressed();
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.activity.getBinding().searchResults;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final UserAdapter adapter = new UserAdapter()
                .setOnItemClickListener(this::handleUserClicked);
        recyclerView.setAdapter(adapter);

        final int dividerLeftPadding = this.activity.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + this.activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin)
                + this.activity.getResources().getDimensionPixelSize(R.dimen.list_item_avatar_margin);
        final int dividerRightPadding = this.activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.activity, R.color.divider))
                        .setRightPadding(dividerRightPadding)
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void initSearch() {
        final ConnectableObservable<String> sourceObservable =
                RxTextView
                .textChangeEvents(this.activity.getBinding().search)
                .skip(1)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(event -> event.text().toString())
                .publish();

        final Subscription searchSub =
                sourceObservable
                .filter(query -> query.length() > 0)
                .subscribe(
                        this::runSearchQuery,
                        throwable ->  LogUtil.exception(getClass(), "Error while searching for user", throwable)
                );

        final Subscription uiSub =
                sourceObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::updateSearchUi,
                        throwable ->  LogUtil.exception(getClass(), "Error while updating user search ui", throwable)
                );

        final Subscription sourceSub = sourceObservable.connect();
        this.subscriptions.addAll(sourceSub, searchSub, uiSub);
    }

    private void runSearchQuery(final String query) {
        final Subscription searchSub =
                searchContacts(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::addUsersToList,
                        throwable ->  LogUtil.exception(getClass(), "Error while searching for user", throwable)
                );

        this.subscriptions.add(searchSub);
    }

    private Single<List<User>> searchContacts(final String query) {
        return BaseApplication
                .get()
                .getRecipientManager()
                .searchContacts(query)
                .toObservable()
                .flatMapIterable(contacts -> contacts)
                .map(Contact::getUser)
                .toList()
                .toSingle();
    }

    private void addUsersToList(final List<User> users) {
        final UserAdapter adapter = getAdapter();
        if (adapter == null) return;
        adapter.setUsers(users);
    }

    private void updateSearchUi(final String query) {
        final UserAdapter adapter = getAdapter();
        if (adapter == null) return;

        final ImageButton clearButton = this.activity.getBinding().clearButton;
        if (query.length() == 0) {
            clearButton.setVisibility(View.GONE);
            adapter.clear();
            return;
        }

        clearButton.setVisibility(View.VISIBLE);
    }

    private UserAdapter getAdapter() {
        if (this.activity == null) return null;
        return (UserAdapter) this.activity.getBinding().searchResults.getAdapter();
    }

    private void handleUserClicked(final User user) {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, ViewUserActivity.class)
                .putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, user.getToshiId());
        this.activity.startActivity(intent);
        this.activity.finish();
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
