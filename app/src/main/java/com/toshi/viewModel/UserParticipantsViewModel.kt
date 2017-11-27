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

import android.arch.lifecycle.ViewModel
import com.toshi.model.local.User
import com.toshi.util.LogUtil
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

class UserParticipantsViewModel : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }
    private val querySubject by lazy { PublishSubject.create<String>() }

    val searchResults by lazy { SingleLiveEvent<List<User>>() }

    init {
        subscribeForQueryChanges()
    }

    private fun subscribeForQueryChanges() {
        val sub = querySubject.debounce(500, TimeUnit.MILLISECONDS)
                .filter { query -> query.length > 1 }
                .subscribe(
                        { runSearchQuery(it) },
                        { LogUtil.e(javaClass, "Error while listening for query changes $it") }
                )
        this.subscriptions.add(sub)
    }

    fun queryUpdated(query: CharSequence) = querySubject.onNext(query.toString())

    private fun runSearchQuery(query: String) {
        val searchSub = searchOnlineUsers(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { searchResults.value = it },
                        { LogUtil.e(javaClass, "Error while search for user $it") }
                )

        this.subscriptions.add(searchSub)
    }

    private fun searchOnlineUsers(query: String): Single<List<User>> {
        return BaseApplication.get()
                .recipientManager
                .searchOnlineUsersAndApps(query)
    }

    override fun onCleared() {
        super.onCleared()
        this.subscriptions.clear()
    }
}