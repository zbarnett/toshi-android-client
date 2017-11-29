package com.toshi.manager;

import com.toshi.BuildConfig;
import com.toshi.manager.network.IdService;
import com.toshi.model.local.Recipient;
import com.toshi.model.local.User;
import com.toshi.model.network.UserSearchResults;
import com.toshi.util.BuildTypes;
import com.toshi.util.LogUtil;
import com.toshi.util.SharedPrefsUtil;
import com.toshi.view.BaseApplication;

import rx.Completable;
import rx.schedulers.Schedulers;

public class OnboardingManager {

    private static final String ONBOARDING_BOT_NAME = "ToshiBot";
    private static final String DEBUG_ONBOARDING_BOT_NAME = "spambot7777";

    public Completable tryTriggerOnboarding() {
        if (SharedPrefsUtil.hasOnboarded()) return Completable.complete();

        return IdService.getApi()
                .searchByUsername(getOnboardingBotName())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(UserSearchResults::getResults)
                .toObservable()
                .flatMapIterable(users -> users)
                .filter(user -> user.getUsernameForEditing().equals(getOnboardingBotName()))
                .toSingle()
                .doOnSuccess(this::sendOnboardingMessageToOnboardingBot)
                .doOnError(throwable -> LogUtil.exception(getClass(), "Error during sending onboarding message to bot", throwable))
                .toCompletable()
                .onErrorComplete();
    }

    public static String getOnboardingBotName() {
        if (BuildConfig.BUILD_TYPE.equals(BuildTypes.DEBUG)) return DEBUG_ONBOARDING_BOT_NAME;
        else return ONBOARDING_BOT_NAME;
    }

    private void sendOnboardingMessageToOnboardingBot(final User onboardingBot) {
        BaseApplication
                .get()
                .getUserManager()
                .getCurrentUser()
                .doOnSuccess(__ -> SharedPrefsUtil.setHasOnboarded(true))
                .subscribe(
                        currentUser -> sendInitMessage(currentUser, onboardingBot),
                        throwable -> LogUtil.exception(getClass(), "Error during sending onboarding message to bot", throwable)
                );
    }

    private void sendInitMessage(final User sender, final User onboardingBot) {
        BaseApplication
                .get()
                .getSofaMessageManager()
                .sendInitMessage(sender, new Recipient(onboardingBot));
    }
}
