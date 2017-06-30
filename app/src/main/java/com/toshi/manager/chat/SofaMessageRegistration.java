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

package com.toshi.manager.chat;


import android.content.SharedPreferences;

import com.toshi.crypto.signal.ChatService;
import com.toshi.crypto.signal.SignalPreferences;
import com.toshi.crypto.signal.store.ProtocolStore;
import com.toshi.manager.network.IdService;
import com.toshi.model.local.Recipient;
import com.toshi.model.local.User;
import com.toshi.model.network.UserSearchResults;
import com.toshi.model.sofa.Message;
import com.toshi.model.sofa.SofaAdapters;
import com.toshi.model.sofa.SofaMessage;
import com.toshi.service.RegistrationIntentService;
import com.toshi.util.LogUtil;
import com.toshi.util.SharedPrefsUtil;
import com.toshi.view.BaseApplication;

import org.whispersystems.libsignal.util.guava.Optional;

import java.io.IOException;

import rx.Completable;
import rx.schedulers.Schedulers;

public class SofaMessageRegistration {

    private static final String ONBOARDING_BOT_NAME = "TokenBot";

    private final SharedPreferences sharedPreferences;
    private final ChatService chatService;
    private final ProtocolStore protocolStore;
    private String gcmToken;

    public SofaMessageRegistration(
            final SharedPreferences sharedPreferences,
            final ChatService chatService,
            final ProtocolStore protocolStore) {
        this.sharedPreferences = sharedPreferences;
        this.chatService = chatService;
        this.protocolStore = protocolStore;

        if (this.sharedPreferences == null || this.chatService == null || this.protocolStore == null) {
            throw new NullPointerException("Initialised with null");
        }
    }

    public void setGcmToken(final String token) {
        this.gcmToken = token;
        tryRegisterGcm();
    }

    public Completable registerIfNeeded() {
        if (!haveRegisteredWithServer()) {
            return registerWithServer();
        } else {
            tryRegisterGcm();
            tryTriggerOnboarding();
            return Completable.complete();
        }
    }

    private Completable registerWithServer() {
        return this.chatService
                .registerKeys(this.protocolStore)
                .doOnCompleted(SignalPreferences::setRegisteredWithServer)
                .doOnCompleted(this::tryRegisterGcm)
                .doOnCompleted(this::tryTriggerOnboarding);
    }

    private boolean haveRegisteredWithServer() {
        return SignalPreferences.getRegisteredWithServer();
    }

    private void tryRegisterGcm() {
        if (this.gcmToken == null) {
            return;
        }

        if (this.sharedPreferences.getBoolean(RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false)) {
            // Already registered
            return;
        }
        try {
            final Optional<String> optional = Optional.of(this.gcmToken);
            this.chatService.setGcmId(optional);
            this.sharedPreferences.edit().putBoolean
                    (RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, true).apply();
            this.gcmToken = null;
        } catch (IOException e) {
            this.sharedPreferences.edit().putBoolean
                    (RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false).apply();
            LogUtil.d(getClass(), "Error during registering of GCM " + e.getMessage());
        }
    }

    public Completable tryUnregisterGcm() {
        return Completable.fromAction(() -> {
                try {
                    this.chatService.setGcmId(Optional.absent());
                    this.sharedPreferences.edit().putBoolean
                            (RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false).apply();
                } catch (IOException e) {
                    LogUtil.d(getClass(), "Error during unregistering of GCM " + e.getMessage());
                }
            })
            .subscribeOn(Schedulers.io());
    }


    private void tryTriggerOnboarding() {
        if (SharedPrefsUtil.hasOnboarded()) return;

        IdService.getApi()
                .searchByUsername(ONBOARDING_BOT_NAME)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(UserSearchResults::getResults)
                .toObservable()
                .flatMapIterable(users -> users)
                .filter(user -> user.getUsernameForEditing().equals(ONBOARDING_BOT_NAME))
                .toSingle()
                .subscribe(
                        this::sendOnboardMessageToOnboardingBot,
                        this::handleOnboardingBotError
                );
    }

    private void sendOnboardMessageToOnboardingBot(final User onboardingBot) {
        BaseApplication
                .get()
                .getUserManager()
                .getCurrentUser()
                .map(this::generateOnboardingMessage)
                .doOnSuccess(__ -> SharedPrefsUtil.setHasOnboarded())
                .subscribe(
                        onboardingMessage -> this.sendOnboardingMessage(onboardingMessage, new Recipient(onboardingBot)),
                        this::handleOnboardingBotError
                );
    }

    private void handleOnboardingBotError(final Throwable throwable) {
        LogUtil.exception(getClass(), "Error during sending onboarding message to bot", throwable);
    }

    private void sendOnboardingMessage(final SofaMessage onboardingMessage, final Recipient onboardingBot) {
        BaseApplication
                .get()
                .getSofaMessageManager()
                .sendMessage(onboardingBot, onboardingMessage);
    }

    private SofaMessage generateOnboardingMessage(final User localUser) {
        final Message sofaMessage = new Message().setBody("");
        final String messageBody = SofaAdapters.get().toJson(sofaMessage);
        return new SofaMessage().makeNew(localUser, messageBody);
    }
}
