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

package com.toshi.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.toshi.R
import com.toshi.crypto.HdWalletBuilder
import com.toshi.manager.ToshiManager
import com.toshi.util.SingleLiveEvent
import com.toshi.util.logging.LogUtil
import com.toshi.util.sharedPrefs.AppPrefs
import com.toshi.view.BaseApplication
import org.bitcoinj.crypto.MnemonicCode
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.IOException

class SignInViewModel(
        private val toshiManager: ToshiManager = BaseApplication.get().toshiManager,
        private val subscribeScheduler: Scheduler = Schedulers.io(),
        private val observeScheduler: Scheduler = AndroidSchedulers.mainThread()
) : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    val isLoading by lazy { MutableLiveData<Boolean>() }
    val passphrase by lazy { MutableLiveData<List<String>>() }
    val walletSuccess by lazy { SingleLiveEvent<Unit>() }
    val error by lazy { SingleLiveEvent<Int>() }

    init {
        initWordList()
    }

    private fun initWordList() {
        try {
            val wordList = MnemonicCode().wordList
            passphrase.value = wordList
        } catch (e: IOException) {
            LogUtil.exception("Error while getting passhrase $e")
        }
    }

    fun tryCreateWallet(masterSeed: String) {
        if (isLoading.value == true) return
        val sub = HdWalletBuilder()
                .buildFromMasterSeed(masterSeed)
                .flatMapCompletable { toshiManager.init(it) }
                .doOnCompleted { AppPrefs.setHasBackedUpPhrase() }
                .subscribeOn(subscribeScheduler)
                .observeOn(observeScheduler)
                .doOnSubscribe { isLoading.value = true }
                .doOnTerminate { isLoading.value = false }
                .subscribe(
                        { walletSuccess.value = Unit },
                        { error.value = R.string.unable_to_restore_wallet }
                )

        subscriptions.add(sub)
    }
}