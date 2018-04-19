/*
 * 	Copyright (c) 2017. Toshi Inc
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
import com.toshi.manager.RecipientManager
import com.toshi.model.local.User
import com.toshi.model.network.UserSection
import com.toshi.model.network.user.UserType
import com.toshi.util.SingleLiveEvent
import com.toshi.view.BaseApplication
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class PopularSearchViewModel(
        private val recipientManager: RecipientManager = BaseApplication.get().recipientManager
) : ViewModel() {

    private val subscriptions by lazy { CompositeSubscription() }

    val popularBots by lazy { MutableLiveData<List<User>>() }
    val popularGroups by lazy { MutableLiveData<List<User>>() }
    val popularUsers by lazy { MutableLiveData<List<User>>() }
    val error by lazy { SingleLiveEvent<Int>() }
    val isLoading by lazy { MutableLiveData<Boolean>() }

    init {
        getPopularSearches()
    }

    private fun getPopularSearches() {
        val sub = recipientManager
                .getPopularSearches()
                .map { breakResultIntoLists(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { isLoading.value = true }
                .doAfterTerminate { isLoading.value = false }
                .subscribe(
                        { handleResult(it) },
                        { error.value = R.string.error_fetching_users }
                )

        subscriptions.add(sub)
    }

    private fun handleResult(userLists: List<List<User>>) {
        popularBots.value = userLists[0]
        popularGroups.value = userLists[1]
        popularUsers.value = userLists[2]
    }

    private fun breakResultIntoLists(sections: List<UserSection>): List<List<User>> {
        val bots by lazy { mutableListOf<User>() }
        val groups by lazy { mutableListOf<User>() }
        val users by lazy { mutableListOf<User>() }

        sections
                .flatMap { it.results }
                .forEach {
                    when (it.type) {
                        UserType.BOT -> bots.add(it)
                        UserType.GROUPBOT -> groups.add(it)
                        UserType.USER -> users.add(it)
                    }
                }

        return listOf(bots, groups, users)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}