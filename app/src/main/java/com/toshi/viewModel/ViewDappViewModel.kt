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
import android.content.Intent
import com.toshi.R
import com.toshi.model.network.dapp.DappResult
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import com.toshi.view.activity.ViewDappActivity.Companion.DAPP_ID
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class ViewDappViewModel(private val intent: Intent) : ViewModel() {

    private val dappManager by lazy { BaseApplication.get().dappManager }
    private val subscriptions by lazy { CompositeSubscription() }

    val dapp by lazy { MutableLiveData<DappResult>() }
    val error by lazy { SingleLiveEvent<Int>() }

    init {
        getDapp(getDappId())
    }

    private fun getDappId() = intent.getLongExtra(DAPP_ID, -1)

    private fun getDapp(dappId: Long) {
        val sub = dappManager
                .getDapp(dappId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { dapp.value = it },
                        { error.value = R.string.error_fetching_dapp }
                )

        subscriptions.add(sub)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}