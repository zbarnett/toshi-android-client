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
import com.toshi.model.network.dapp.Dapp
import com.toshi.model.network.dapp.DappSearchResult
import com.toshi.model.network.dapp.DappSections
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

class DappViewModel : ViewModel() {

    private val dappManager by lazy { BaseApplication.get().dappManager }
    private val subscriptions by lazy { CompositeSubscription() }
    private val searchSubject by lazy { PublishSubject.create<String>() }

    val searchResult by lazy { MutableLiveData<DappSearchResult>() }
    val dappsError by lazy { SingleLiveEvent<Int>() }
    val dappSections by lazy { MutableLiveData<DappSections>() }
    val allDapps by lazy { MutableLiveData<List<Dapp>>() }

    init {
        getFeaturedDapps()
        initSearchListener()
    }

    private fun getFeaturedDapps() {
        val sub = dappManager
                .getFrontPageDapps()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { dappSections.value = it },
                        { R.string.error_fetching_dapps }
                )

        subscriptions.add(sub)
    }

    fun search(input: String) = searchSubject.onNext(input)

    private fun initSearchListener() {
        val sub = searchSubject
                .debounce(500, TimeUnit.MILLISECONDS)
                .flatMap { searchForDapps(it).toObservable() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { searchResult.value = it },
                        { dappsError.value = R.string.error_fetching_dapps }
                )

        subscriptions.add(sub)
    }

    private fun searchForDapps(input: String): Single<DappSearchResult> {
        return dappManager.search(input)
    }

    fun getAllDapps() {
        if (allDapps.value != null) return
        val sub = dappManager
                .getAllDapps()
                .map { it.results.dapps }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { allDapps.value = it },
                        { dappsError.value = R.string.error_fetching_dapps }
                )

        subscriptions.add(sub)
    }

    fun getCategories() = searchResult.value?.results?.categories ?: emptyMap()

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}