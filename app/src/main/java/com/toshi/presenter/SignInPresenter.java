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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.google.common.base.Joiner;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.toshi.R;
import com.toshi.crypto.HDWallet;
import com.toshi.manager.ToshiManager;
import com.toshi.util.LogUtil;
import com.toshi.util.SharedPrefsUtil;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.MainActivity;
import com.toshi.view.activity.SignInActivity;
import com.toshi.view.activity.SignInInfoActivity;
import com.toshi.view.adapter.SignInPassphraseAdapter;
import com.toshi.view.custom.SpaceDecoration;

import org.bitcoinj.crypto.MnemonicCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SignInPresenter implements Presenter<SignInActivity> {

    private static final int PASSPHRASE_LENGTH = 12;

    private SignInActivity activity;
    private boolean firstTimeAttaching = true;
    private CompositeSubscription subscriptions;

    private boolean onGoingTask = false;
    private List<String> wordList;
    private List<String> approvedWords;
    private boolean isHidden;

    @Override
    public void onViewAttached(SignInActivity view) {
        this.activity = view;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        initLists();
        initWordList();
    }

    private void initShortLivingObjects() {
        initPassphraseList();
        initPassphraseView();
        initClickListeners();
        updateSignInView();
    }

    private void initLists() {
        this.wordList = new ArrayList<>();
        this.approvedWords = new ArrayList<>();
    }

    private void initWordList() {
        try {
            this.wordList = new MnemonicCode().getWordList();
        } catch (IOException e) {
            LogUtil.e(getClass(), e.toString());
        }
    }

    private void initPassphraseList() {
        final RecyclerView passphraseList = this.activity.getBinding().passphraseList;
        if (passphraseList.getAdapter() != null) return;

        final ChipsLayoutManager chipsLayoutManager = ChipsLayoutManager.newBuilder(this.activity)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                .setMaxViewsInRow(4)
                .build();
        final int itemSpacing = this.activity.getResources().getDimensionPixelSize(R.dimen.passphrase_sign_in_spacing);
        passphraseList.addItemDecoration(new SpaceDecoration(itemSpacing));
        passphraseList.setLayoutManager(chipsLayoutManager);
        final SignInPassphraseAdapter adapter = new SignInPassphraseAdapter(this.approvedWords)
                .setOnItemClickListener(this::handleWordClicked);
        passphraseList.setAdapter(adapter);

        if (this.isHidden) {
            adapter.hideWords();
        } else {
            adapter.showWords();
        }
    }

    private void handleWordClicked(final int position) {
        this.approvedWords.remove(position);
        final SignInPassphraseAdapter adapter = (SignInPassphraseAdapter) this.activity.getBinding().passphraseList.getAdapter();
        adapter.setPassphrase(this.approvedWords);
        updateSignInView();
    }

    private void initPassphraseView() {
        this.activity.getBinding().suggestionView.setOnPasteListener(this::handlePastedString);

        final Subscription sub =
                RxTextView
                .textChanges(this.activity.getBinding().suggestionView.getWordView())
                .skip(1)
                .map(CharSequence::toString)
                .doOnNext(this::clearSuggestion)
                .filter(input -> input.length() > 0)
                .doOnNext(__ -> hideErrorMessage())
                .observeOn(Schedulers.io())
                .flatMap(this::getWordSuggestion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleWordSuggestion,
                        throwable -> LogUtil.e(getClass(), throwable.toString())
                );

        this.subscriptions.add(sub);
    }

    private void clearSuggestion(final String string) {
        if (string.length() > 0) return;
        this.activity.getBinding().suggestionView.clearSuggestion();
    }

    private Observable<String> getWordSuggestion(final String startOfWord) {
        return Observable.fromCallable(() -> {
            if (startOfWord.length() == 0) return null;

            final int searchResult = Collections.binarySearch(this.wordList, startOfWord);
            if (Math.abs(searchResult) >= this.wordList.size()) return null;

            final int index = searchResult < 0 ? Math.abs(searchResult) - 1 : searchResult;
            final String suggestion = this.wordList.get(index);
            if (!suggestion.startsWith(startOfWord)) return null;

            return suggestion;
        });
    }

    private void handleWordSuggestion(final String suggestion) {
        if (suggestion == null) {
            this.activity.getBinding().suggestionView.clearSuggestion();
            final String passphraseInput = this.activity.getBinding().suggestionView.getWordView().getText().toString();
            showErrorMessage(this.activity.getString(R.string.no_suggestion_found, passphraseInput));
            return;
        }

        this.activity.getBinding().suggestionView.setSuggestion(suggestion);
    }

    private void showErrorMessage(final String message) {
        this.activity.getBinding().hidePassphrase.setVisibility(View.GONE);
        this.activity.getBinding().errorView.setVisibility(View.VISIBLE);
        this.activity.getBinding().errorView.setText(message);
        updateInputUnderline(R.color.error_color);
    }

    private void hideErrorMessage() {
        this.activity.getBinding().hidePassphrase.setVisibility(View.VISIBLE);
        this.activity.getBinding().errorView.setVisibility(View.GONE);
        updateInputUnderline(R.color.input_underline);
    }

    private void updateInputUnderline(final @ColorRes int colorRes) {
        final EditText passphrase = this.activity.getBinding().suggestionView.getWordView();
        final Drawable originalDrawable = passphrase.getBackground();
        final int color = ContextCompat.getColor(this.activity, colorRes);
        DrawableCompat.setTintList(originalDrawable, ColorStateList.valueOf(color));
        passphrase.setBackground(originalDrawable);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().hidePassphrase.setOnClickListener(__ -> handleHidePassphraseClicked());
        this.activity.getBinding().infoView.setOnClickListener(__ -> handleInfoViewClicked());
        this.activity.getBinding().suggestionView.getWordView().setOnEditorActionListener((__, actionId, ___) -> handleEnterClicked(actionId));
        this.activity.getBinding().signIn.setOnClickListener(v -> handleSignInClicked());
    }

    private void handleHidePassphraseClicked() {
        this.isHidden = !this.isHidden;
        final SignInPassphraseAdapter adapter = (SignInPassphraseAdapter) this.activity.getBinding().passphraseList.getAdapter();
        if (this.isHidden) {
            adapter.hideWords();
        } else {
            adapter.showWords();
        }
    }

    private void handleInfoViewClicked() {
        final Intent intent = new Intent(this.activity, SignInInfoActivity.class);
        this.activity.startActivity(intent);
    }

    private boolean handleEnterClicked(final int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            final String word = this.activity.getBinding().suggestionView.getSuggestioniew().getText().toString();
            if (!word.isEmpty()) {
                addWordToList(word);
            }
            return true;
        }
        return false;
    }

    private void addWordToList(final String word) {
        this.activity.getBinding().suggestionView.clear();
        addWord(word);
        updateSignInView();
    }

    private void addWord(final String word) {
        this.approvedWords.add(word);
        addWordsToAdapter(this.approvedWords);
    }

    private void addWordsToAdapter(final List<String> wordList) {
        final SignInPassphraseAdapter adapter = (SignInPassphraseAdapter) this.activity.getBinding().passphraseList.getAdapter();
        adapter.setPassphrase(wordList);
    }

    private void updateSignInView() {
        if (this.approvedWords.size() == 0) {
            disableSignIn(this.activity.getString(R.string.sign_in));
            return;
        }

        final int wordsLeft = PASSPHRASE_LENGTH - this.approvedWords.size();
        if (wordsLeft > 0) {
            final String wordsLeftString = this.activity.getResources().getQuantityString(R.plurals.words, wordsLeft, wordsLeft);
            disableSignIn(wordsLeftString);
        } else if (wordsLeft == 0) {
            enableSignIn();
        }
    }

    private void disableSignIn(final String string) {
        final Button signIn = this.activity.getBinding().signIn;
        signIn.setText(string);
        signIn.setBackgroundResource(R.drawable.background_with_radius_disabled);
        signIn.setEnabled(false);

        this.activity.getBinding().messageWrapper.setVisibility(View.VISIBLE);
        this.activity.getBinding().suggestionView.setVisibility(View.VISIBLE);
    }

    private void enableSignIn() {
        final Button signIn = this.activity.getBinding().signIn;
        signIn.setText(R.string.sign_in);
        signIn.setBackgroundResource(R.drawable.background_with_radius_primary_color);
        signIn.setEnabled(true);

        this.activity.getBinding().messageWrapper.setVisibility(View.GONE);
        this.activity.getBinding().suggestionView.setVisibility(View.GONE);
    }

    private void handleSignInClicked() {
        if (this.approvedWords.size() != PASSPHRASE_LENGTH) {
            showToast(R.string.sign_in_length_error_message);
            return;
        }

        final Joiner joiner = Joiner.on(" ");
        final String masterSeed = joiner.join(this.approvedWords);
        tryCreateWallet(masterSeed);
    }

    private void tryCreateWallet(final String masterSeed) {
        if (this.onGoingTask) return;
        startLoadingTask();

        final Subscription sub =
                new HDWallet()
                .createFromMasterSeed(masterSeed)
                .flatMap(this::initWallet)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(__ -> SharedPrefsUtil.setHasBackedUpPhrase())
                .subscribe(
                        __ -> handleWalletSuccess(),
                        __ -> handleWalletError()
                );

        this.subscriptions.add(sub);
    }

    private void startLoadingTask() {
        this.onGoingTask = true;
        this.activity.getBinding().loadingSpinner.setVisibility(View.VISIBLE);
    }

    private Single<ToshiManager> initWallet(final HDWallet wallet) {
        return BaseApplication
                .get()
                .getToshiManager()
                .init(wallet);
    }

    private void handleWalletSuccess() {
        stopLoadingTask();
        goToMainActivity();
    }

    private void goToMainActivity() {
        SharedPrefsUtil.setSignedIn();
        final Intent intent = new Intent(this.activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.activity.startActivity(intent);
        ActivityCompat.finishAffinity(this.activity);
    }

    private void handleWalletError() {
        showToast(R.string.unable_to_restore_wallet);
        stopLoadingTask();
    }

    private void stopLoadingTask() {
        this.onGoingTask = false;
        this.activity.getBinding().loadingSpinner.setVisibility(View.GONE);
    }

    private void showToast(final @StringRes int stringId) {
        Toast.makeText(
                this.activity,
                this.activity.getString(stringId),
                Toast.LENGTH_SHORT
        ).show();
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