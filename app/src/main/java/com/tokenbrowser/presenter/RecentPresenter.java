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

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.ContactThread;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.UserSearchType;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.UserSearchActivity;
import com.tokenbrowser.view.adapter.RecentAdapter;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.custom.HorizontalLineDivider;
import com.tokenbrowser.view.fragment.toplevel.RecentFragment;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public final class RecentPresenter implements
        Presenter<RecentFragment>,
        OnItemClickListener<ContactThread> {

    private RecentFragment fragment;
    private boolean firstTimeAttaching = true;
    private RecentAdapter adapter;
    private CompositeSubscription subscriptions;

    @Override
    public void onViewAttached(final RecentFragment fragment) {
        this.fragment = fragment;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        this.adapter = new RecentAdapter()
                .setOnItemClickListener(this);
    }

    private void initShortLivingObjects() {
        initClickListeners();
        initRecentsAdapter();
        populateRecentsAdapter();
        attachSubscriber();
    }

    private void initClickListeners() {
        this.fragment.getBinding().startChat.setOnClickListener(__ -> goToUserSearchActivity());
    }

    private void initRecentsAdapter() {
        final RecyclerView recyclerView = this.fragment.getBinding().recents;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.fragment.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.adapter);

        final int dividerLeftPadding = this.fragment.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                                     + this.fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.fragment.getContext(), R.color.divider))
                .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void populateRecentsAdapter() {
        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .loadAllContactThreads()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleThreads,
                        this::handleThreadsError
                );

        this.subscriptions.add(sub);
    }

    private void handleThreads(final List<ContactThread> contactThreads) {
        this.adapter.setContactThreads(contactThreads);
        updateEmptyState();
    }

    private void handleThreadsError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error fetching threads", throwable);
    }

    private void attachSubscriber() {
        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .registerForAllContactThreadChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleThread,
                        this::handleThreadError
                );

        this.subscriptions.add(sub);
    }

    private void handleThread(final ContactThread updatedContactThread) {
        this.adapter.updateThread(updatedContactThread);
        updateEmptyState();
    }

    private void handleThreadError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error during fetching thread", throwable);
    }

    private void updateEmptyState() {
        if (this.fragment == null) {
            return;
        }
        // Hide empty state if we have some content
        final boolean showingEmptyState = this.fragment.getBinding().emptyStateSwitcher.getCurrentView().getId() == this.fragment.getBinding().emptyState.getId();
        final boolean shouldShowEmptyState = this.adapter.getItemCount() == 0;

        if (shouldShowEmptyState && !showingEmptyState) {
            this.fragment.getBinding().emptyStateSwitcher.showPrevious();
        } else if (!shouldShowEmptyState && showingEmptyState) {
            this.fragment.getBinding().emptyStateSwitcher.showNext();
        }
    }

    @Override
    public void onItemClick(final ContactThread clickedContactThread) {
        if (this.fragment == null) return;
        final Intent intent = new Intent(this.fragment.getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, clickedContactThread.getThreadId());
        this.fragment.startActivity(intent);
    }

    public void handleActionMenuClicked(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_conversation: {
                goToUserSearchActivity();
            }
        }
    }

    private void goToUserSearchActivity() {
        final Intent intent = new Intent(this.fragment.getContext(), UserSearchActivity.class)
                .putExtra(UserSearchActivity.VIEW_TYPE, UserSearchType.CONTACT_THREAD);
        this.fragment.startActivity(intent);
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.fragment = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.adapter = null;
    }
}
