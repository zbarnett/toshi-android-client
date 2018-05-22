/*
 *
 *  * 	Copyright (c) 2018. Toshi Inc
 *  *
 *  * 	This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License, or
 *  *     (at your option) any later version.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.toshi.managers.recipientManager

import com.toshi.manager.RecipientManager
import com.toshi.manager.network.IdService
import com.toshi.manager.store.BlockedUserStore
import com.toshi.manager.store.GroupStore
import com.toshi.manager.store.UserStore
import com.toshi.managers.userManager.IdServiceMocker
import com.toshi.model.local.User
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import rx.Completable
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

class RecipientManagerMocker {
    fun mock(): RecipientManager {
        return RecipientManager(
                idService = mockIdService(),
                groupStore = mockGroupStore(),
                userStore = mockUserStore(),
                blockedUserStore = mockBlockedUserStore(),
                connectivitySubject = mockConnectivitySubject(),
                scheduler = Schedulers.trampoline()
        )
    }

    private fun mockIdService(): IdService {
        return IdServiceMocker().mock()
    }

    private fun mockGroupStore(): GroupStore {
        return Mockito.mock(GroupStore::class.java)
    }

    private fun mockUserStore(): UserStore {
        val userStore = Mockito.mock(UserStore::class.java)
        Mockito.`when`(userStore.save(any(User::class.java)))
                .thenReturn(Completable.complete())
        return userStore
    }

    private fun mockBlockedUserStore(): BlockedUserStore {
        return Mockito.mock(BlockedUserStore::class.java)
    }

    private fun mockConnectivitySubject(): BehaviorSubject<Boolean> {
        val subject = BehaviorSubject.create<Boolean>()
        subject.onNext(true)
        return subject
    }
}