package com.tokenbrowser.presenter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.GroupSetupActivity;
import com.tokenbrowser.view.adapter.GroupParticipantAdapter;
import com.tokenbrowser.view.custom.HorizontalLineDivider;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class GroupSetupPresenter implements Presenter<GroupSetupActivity> {

    private GroupSetupActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(GroupSetupActivity view) {
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
        initClickListeners();
        initRecyclerView();
        initNumberOfParticipantsView();
        fetchUsers();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.activity.getBinding().participants;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new GroupParticipantAdapter());
        recyclerView.setNestedScrollingEnabled(false);

        final int dividerLeftPadding = this.activity.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + this.activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.activity, R.color.divider))
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void initNumberOfParticipantsView() {
        final String participants = this.activity.getString(R.string.number_of_participants, String.format("%d", getParticipantList().size()));
        this.activity.getBinding().numberOfParticipants.setText(participants);
    }

    private void fetchUsers() {
        final Subscription sub =
                Observable.from(getParticipantList())
                .flatMap(this::fetchUser)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> getGroupParticipantAdapter().addUser(user),
                        this::HandleError
                );

        this.subscriptions.add(sub);
    }

    private List<String> getParticipantList() {
        return this.activity.getIntent().getStringArrayListExtra(GroupSetupActivity.PARTICIPANTS);
    }

    private Observable<User> fetchUser(final String tokenId) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserFromAddress(tokenId)
                .toObservable();
    }

    private GroupParticipantAdapter getGroupParticipantAdapter() {
        return (GroupParticipantAdapter)this.activity.getBinding().participants.getAdapter();
    }

    private void HandleError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error during fetching group participants", throwable);
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
