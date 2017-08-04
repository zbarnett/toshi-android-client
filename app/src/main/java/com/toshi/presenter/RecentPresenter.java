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

package com.toshi.presenter;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.toshi.R;
import com.toshi.model.local.Conversation;
import com.toshi.util.LogUtil;
import com.toshi.util.UserSearchType;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.ChatActivity;
import com.toshi.view.activity.UserSearchActivity;
import com.toshi.view.adapter.RecentAdapter;
import com.toshi.view.adapter.listeners.OnItemClickListener;
import com.toshi.view.custom.HorizontalLineDivider;
import com.toshi.view.fragment.toplevel.RecentFragment;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public final class RecentPresenter implements
        Presenter<RecentFragment>,
        OnItemClickListener<Conversation> {

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
        this.fragment.getBinding().add.setOnClickListener(__ -> goToUserSearchActivity());
    }

    private void initRecentsAdapter() {
        final RecyclerView recyclerView = this.fragment.getBinding().recents;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.fragment.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.adapter);
        addSwipeToDeleteListener(recyclerView);

        final int dividerLeftPadding = fragment.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin)
                + fragment.getResources().getDimensionPixelSize(R.dimen.list_item_avatar_margin);
        final int dividerRightPadding = fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.fragment.getContext(), R.color.divider))
                        .setRightPadding(dividerRightPadding)
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void addSwipeToDeleteListener(final RecyclerView recyclerView) {
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(final RecyclerView recyclerView1, final RecyclerView.ViewHolder viewHolder, final RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                    adapter.removeItemAtWithUndo(viewHolder.getAdapterPosition(), recyclerView);
                }
            });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void populateRecentsAdapter() {
        final Subscription sub =
                BaseApplication
                .get()
                .getSofaMessageManager()
                .loadAllConversations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleConversations,
                        this::handleConversationsError
                );

        this.subscriptions.add(sub);
    }

    private void handleConversations(final List<Conversation> conversations) {
        this.adapter.setConversations(conversations);
        updateEmptyState();
    }

    private void handleConversationsError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error fetching conversations", throwable);
    }

    private void attachSubscriber() {
        final Subscription sub =
                BaseApplication
                .get()
                .getSofaMessageManager()
                .registerForAllConversationChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleConversation,
                        this::handleConversationError
                );

        this.subscriptions.add(sub);
    }

    private void handleConversation(final Conversation updatedConversation) {
        this.adapter.updateConversation(updatedConversation);
        updateEmptyState();
    }

    private void handleConversationError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error during fetching conversation", throwable);
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
    public void onItemClick(final Conversation clickedConversation) {
        if (this.fragment == null) return;
        final Intent intent = new Intent(this.fragment.getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA__THREAD_ID, clickedConversation.getThreadId());
        this.fragment.startActivity(intent);
    }

    private void goToUserSearchActivity() {
        if (this.fragment == null || this.fragment.getContext() == null) return;
        final Intent intent = new Intent(this.fragment.getContext(), UserSearchActivity.class)
                .putExtra(UserSearchActivity.VIEW_TYPE, UserSearchType.CONVERSATION);
        this.fragment.startActivity(intent);
    }

    @Override
    public void onViewDetached() {
        this.adapter.doDelete();
        this.subscriptions.clear();
        this.fragment = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.adapter = null;
    }
}
