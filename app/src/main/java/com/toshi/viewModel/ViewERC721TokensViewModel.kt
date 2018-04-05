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
import com.toshi.model.network.ERC721TokenWrapper
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class ViewERC721TokensViewModel(contractAddress: String) : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    private val balanceManager by lazy { BaseApplication.get().balanceManager }

    val collectible by lazy { MutableLiveData<ERC721TokenWrapper>() }
    val error by lazy { SingleLiveEvent<Int>() }

    init {
        getERC721Token(contractAddress)
    }

    private fun getERC721Token(contactAddress: String) {
        val sub = balanceManager
                .getERC721Token(contactAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { collectible.value = it },
                        { error.value = R.string.collectible_error }
                )

        subscriptions.add(sub)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}