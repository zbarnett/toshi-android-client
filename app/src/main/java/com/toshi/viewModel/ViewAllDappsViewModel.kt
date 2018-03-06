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
import com.toshi.model.network.TempDapp
import com.toshi.util.SingleLiveEvent
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ViewAllDappsViewModel : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    val dapps by lazy { MutableLiveData<List<TempDapp>>() }
    val dappsError by lazy { SingleLiveEvent<Int>() }

    init {
        getDapps()
    }

    private fun getDapps() {
        val sub = Single.just(listOf(
                TempDapp("Games", "CryptoTanks", "Tanks"),
                TempDapp("Games", "CryptoPokemon", "Pokemon"),
                TempDapp("Games", "CryptCS", "Counter Strike"),
                TempDapp("Games", "CryptoDigimon", "Digimon")
        ))
                .map { it.sortedBy { it.category } }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { dapps.value = it },
                        { dappsError.value = R.string.error_fetching_dapps }
                )

        subscriptions.add(sub)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}