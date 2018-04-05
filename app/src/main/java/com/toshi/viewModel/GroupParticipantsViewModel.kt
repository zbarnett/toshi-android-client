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
import com.toshi.util.SingleLiveEvent
import com.toshi.util.logging.LogUtil
import com.toshi.view.BaseApplication
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

class GroupParticipantsViewModel : ViewModel() {

    private val recipientManager by lazy { BaseApplication.get().recipientManager }

    private val subscriptions by lazy { CompositeSubscription() }
    private val querySubject by lazy { PublishSubject.create<String>() }
    private val participants by lazy { mutableListOf<User>() }
    private val defaultResults by lazy { mutableListOf<User>() }

    val searchResults by lazy { SingleLiveEvent<List<User>>() }
    val selectedParticipants by lazy { MutableLiveData<List<User>>() }
    val error by lazy { SingleLiveEvent<Int>() }

    init {
        subscribeForQueryChanges()
    }

    private fun subscribeForQueryChanges() {
        val startSearchSub = querySubject.debounce(500, TimeUnit.MILLISECONDS)
                .filter { query -> query.length >= 3 }
                .subscribe(
                        { runSearchQuery(it) },
                        { LogUtil.w("Error while listening for query changes $it") }
                )

        val clearSub = querySubject.filter { query -> query.length < 3 }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { showDefaultResults() },
                        { LogUtil.w("Error while listening for query changes $it") }
                )

        subscriptions.addAll(startSearchSub, clearSub)
    }

    private fun showDefaultResults() {
        if (searchResults.value != defaultResults) searchResults.value = defaultResults
    }

    fun queryUpdated(query: CharSequence?) = querySubject.onNext(query.toString())

    private fun runSearchQuery(query: String) {
        val searchSub = recipientManager
                .searchOnlineUsers(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { searchResults.value = it },
                        { LogUtil.w("Error while search for user $it") }
                )

        subscriptions.add(searchSub)
    }

    fun toggleSelectedParticipant(user: User) {
        if (participants.contains(user)) participants.remove(user)
        else participants.add(user)
        selectedParticipants.value = participants
    }

    fun clearParticipants() {
        participants.clear()
        selectedParticipants.value = participants
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}
