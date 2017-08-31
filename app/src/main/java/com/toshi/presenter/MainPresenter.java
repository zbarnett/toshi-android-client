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
import android.os.Bundle;
import android.support.v4.BuildConfig;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.toshi.R;
import com.toshi.manager.SofaMessageManager;
import com.toshi.model.local.User;
import com.toshi.util.BuildTypes;
import com.toshi.util.LogUtil;
import com.toshi.util.SharedPrefsUtil;
import com.toshi.util.SoundManager;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.MainActivity;
import com.toshi.view.activity.ScannerActivity;
import com.toshi.view.adapter.NavigationAdapter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainPresenter implements Presenter<MainActivity> {
    private static final int DEFAULT_TAB = 0;
    private static final int SCAN_POSITION = 2;

    private MainActivity activity;
    private boolean firstTimeAttached = true;
    private NavigationAdapter adapter;
    private CompositeSubscription subscriptions;

    private final AHBottomNavigation.OnTabSelectedListener tabListener = new AHBottomNavigation.OnTabSelectedListener() {
        @Override
        public boolean onTabSelected(final int position, final boolean wasSelected) {
            if (activity == null) return false;
            if (position == SCAN_POSITION) {
                openScanActivity();
                return false;
            }
            final FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(activity.getBinding().fragmentContainer.getId(), adapter.getItem(position)).commit();

            if (!wasSelected) {
                SoundManager.getInstance().playSound(SoundManager.TAB_BUTTON);
            }
            return true;
        }
    };

    @Override
    public void onViewAttached(final MainActivity activity) {
        this.activity = activity;

        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            this.adapter = new NavigationAdapter(this.activity, R.menu.navigation);
            this.subscriptions = new CompositeSubscription();
            manuallySelectFirstTab();
        }
        initNavBar();
        trySelectTabFromIntent();
        attachUnreadMessagesSubscription();
        attachUserSubscription();
        handleHasBackedUpPhrase();
        showBetaWarningDialog();
    }

    private void manuallySelectFirstTab() {
        this.tabListener.onTabSelected(DEFAULT_TAB, false);
    }

    private void initNavBar() {
        final AHBottomNavigation navBar = this.activity.getBinding().navBar;
        final AHBottomNavigationAdapter menuInflater = new AHBottomNavigationAdapter(this.activity, R.menu.navigation);
        menuInflater.setupWithBottomNavigation(navBar);

        navBar.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        navBar.setAccentColor(ContextCompat.getColor(this.activity, R.color.colorPrimary));
        navBar.setInactiveIconColor(ContextCompat.getColor(this.activity, R.color.inactiveIconColor));
        navBar.setInactiveTextColor(ContextCompat.getColor(this.activity, R.color.inactiveTextColor));

        navBar.setOnTabSelectedListener(this.tabListener);
        navBar.setSoundEffectsEnabled(false);
        navBar.setBehaviorTranslationEnabled(false);

        navBar.setTitleTextSizeInSp(13.0f, 12.0f);
    }

    private void attachUnreadMessagesSubscription() {
        final SofaMessageManager messageManager =
                BaseApplication
                .get()
                .getSofaMessageManager();

        final Subscription allChangesSubscription =
                messageManager.registerForAllConversationChanges()
                .flatMap((unused) -> messageManager.areUnreadMessages().toObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleUnreadMessages,
                        this::handleUnreadMessagesError
                );

        final Subscription firstTimeSubscription =
                messageManager
                .areUnreadMessages()
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleUnreadMessages,
                        this::handleUnreadMessagesError
                );

        this.subscriptions.add(allChangesSubscription);
        this.subscriptions.add(firstTimeSubscription);
    }

    private void handleUnreadMessagesError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error while fetching unread messages", throwable);
    }

    private void handleUnreadMessages(final boolean areUnreadMessages) {
        if (areUnreadMessages) {
            showUnreadBadge();
        } else {
            hideUnreadBadge();
        }
    }

    private void showUnreadBadge() {
        this.activity.getBinding().navBar.setNotification(" ", 1);
    }

    private void hideUnreadBadge() {
        this.activity.getBinding().navBar.setNotification("", 1);
    }

    // ToDo:This code can be removed July 31st 2017
    private void attachUserSubscription() {
        final Subscription sub =
                BaseApplication
                        .get()
                        .getUserManager()
                        .getUserObservable()
                        .filter(user -> user != null)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::tryShowMigrationDialog,
                                ex -> LogUtil.e(getClass(), ex.toString())
                        );

        this.subscriptions.add(sub);
    }

    private void tryShowMigrationDialog(final User user) {
        if (!SharedPrefsUtil.wasMigrated()) return;

        final String message = BaseApplication.get().getString(R.string.dialog__migration_message);
        final SpannableString clickableMessage = new SpannableString(message);
        Linkify.addLinks(clickableMessage, Linkify.ALL);

        try {
            ((TextView) new AlertDialog.Builder(this.activity, R.style.AlertDialogCustom)
                    .setTitle(R.string.dialog__migration_title)
                    .setMessage(clickableMessage)
                    .setPositiveButton(R.string.continue_, (dialog, __) -> dialog.dismiss())
                    .show()
                    .findViewById(android.R.id.message))
                    .setMovementMethod(LinkMovementMethod.getInstance());
        } catch (final NullPointerException ex) {
            LogUtil.e(getClass(), "Error when rendering migration dialog.");
        }

        SharedPrefsUtil.setWasMigrated(false);
    }

    private void handleHasBackedUpPhrase() {
        if (SharedPrefsUtil.hasBackedUpPhrase()) {
            hideAlertBadge();
        } else {
            showAlertBadge();
        }
    }

    private void showBetaWarningDialog() {
        final boolean isReleaseBuild = BuildConfig.BUILD_TYPE.equals(BuildTypes.RELEASE);
        if (SharedPrefsUtil.hasLoadedApp() || isReleaseBuild) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this.activity, R.style.AlertDialogCustom);
        builder.setTitle(R.string.beta_warning_title)
                .setMessage(R.string.beta_warning_message)
                .setPositiveButton(R.string.continue_, (dialog, which) -> dialog.dismiss());
        builder.create().show();
        SharedPrefsUtil.setHasLoadedApp();
    }

    private void hideAlertBadge() {
        this.activity.getBinding().navBar.setNotification("", 4);
    }

    private void showAlertBadge() {
        this.activity.getBinding().navBar.setNotification("!", 4);
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
        this.subscriptions.clear();
    }

    @Override
    public void onDestroyed() {
        this.adapter = null;
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        trySelectTabFromIntent();
    }

    private void trySelectTabFromIntent() {
        final Intent intent = this.activity.getIntent();
        final int activeTab = intent.getIntExtra(MainActivity.EXTRA__ACTIVE_TAB, this.activity.getBinding().navBar.getCurrentItem());
        this.activity.getIntent().removeExtra(MainActivity.EXTRA__ACTIVE_TAB);
        this.activity.getBinding().navBar.setCurrentItem(activeTab);
    }

    private void openScanActivity() {
        final Intent intent = new Intent(this.activity, ScannerActivity.class);
        this.activity.startActivity(intent);
    }
}
