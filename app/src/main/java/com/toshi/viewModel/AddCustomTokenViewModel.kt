/*
 * Copyright (c) 2017. Toshi Inc
 *
 *  This program is free software: you can redistribute it and/or modify
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
import com.toshi.manager.token.TokenManager
import com.toshi.model.network.token.CustomERCToken
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class AddCustomTokenViewModel(
        private val baseApplication: BaseApplication = BaseApplication.get(),
        private val tokenManager: TokenManager = baseApplication.tokenManager,
        private val observeScheduler: Scheduler = AndroidSchedulers.mainThread()

) : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    val isLoading by lazy { MutableLiveData<Boolean>() }
    val success by lazy { SingleLiveEvent<Unit>() }
    val error by lazy { SingleLiveEvent<Int>() }

    fun addCustomToken(customERCToken: CustomERCToken) {
        val sub = tokenManager
                .addCustomToken(customERCToken)
                .observeOn(observeScheduler)
                .doOnSubscribe { isLoading.value = true }
                .doOnTerminate { isLoading.value = false }
                .subscribe(
                        { success.value = Unit },
                        { error.value = R.string.add_custom_token_error }
                )

        subscriptions.add(sub)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}