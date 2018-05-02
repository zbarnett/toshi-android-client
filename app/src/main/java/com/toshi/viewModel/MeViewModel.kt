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
import com.toshi.model.local.User
import com.toshi.model.network.Balance
import com.toshi.util.SingleLiveEvent
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class MeViewModel : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    private val userManager by lazy { BaseApplication.get().userManager }
    private val balanceManager by lazy { BaseApplication.get().balanceManager }

    val user by lazy { MutableLiveData<User>() }
    val singelBalance by lazy { SingleLiveEvent<Balance>() }

    init {
        fetchUser()
    }

    private fun fetchUser() {
        val sub = userManager
                .getCurrentUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { user.value = it },
                        { LogUtil.exception("Error during fetching user $it") }
                )

        subscriptions.add(sub)
    }

    fun getBalance() {
        val sub = balanceManager
                .balanceObservable
                .first()
                .toSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { singelBalance.value = it },
                        { LogUtil.exception("Error showing dialog $it") }
                )

        subscriptions.add(sub)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}