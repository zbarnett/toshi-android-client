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
import com.toshi.model.local.Group
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class GroupInfoViewModel : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }

    val group by lazy { SingleLiveEvent<Group>() }
    val fetchGroupError by lazy { SingleLiveEvent<Throwable>() }
    val leaveGroup by lazy { SingleLiveEvent<Boolean>() }
    val leaveGroupError by lazy { SingleLiveEvent<Throwable>() }

    fun fetchGroup(groupId: String) {
        val subscription = BaseApplication.get()
                .recipientManager
                .getGroupFromId(groupId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { group.value = it },
                        { fetchGroupError.value = it }
                )
        this.subscriptions.add(subscription)
    }

    fun leaveGroup() {
        group.value?.let {
            val leaveSub = BaseApplication
                    .get()
                    .sofaMessageManager
                    .leaveGroup(it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { leaveGroup.value = true },
                            { leaveGroupError.value = it }
                    )
            subscriptions.add(leaveSub)
        }
    }

    override fun onCleared() {
        super.onCleared()
        this.subscriptions.clear()
    }
}