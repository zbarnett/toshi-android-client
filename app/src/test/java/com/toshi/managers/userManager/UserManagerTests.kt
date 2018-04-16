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

package com.toshi.managers.userManager

import com.toshi.manager.UserManager
import com.toshi.manager.network.IdInterface
import com.toshi.model.network.ServerTime
import com.toshi.testSharedPrefs.TestUserPrefs
import com.toshi.util.sharedPrefs.UserPrefsInterface
import junit.framework.Assert.assertNull
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import rx.Single
import rx.schedulers.Schedulers

class UserManagerTests {

    private lateinit var userPrefs: UserPrefsInterface
    private lateinit var userManager: UserManager

    @Before
    fun before() {
        userPrefs = TestUserPrefs()
        userManager = UserManager(
                idService = mockIdApi(),
                userPrefs = userPrefs,
                scheduler = Schedulers.trampoline()
        )
    }

    private fun mockIdApi(): IdInterface {
        val userApi = Mockito.mock(IdInterface::class.java)
        Mockito.`when`(userApi.timestamp)
                .thenReturn(Single.just(ServerTime(1L)))
        return userApi
    }

    @Test
    fun `check if user prefs returns null when prefs is empty`() {
        userPrefs.clear()
        assertThatPrefsAreCleared()
    }

    @Test
    fun `check if user prefs are empty after clear`() {
        userPrefs.setUserId("1")
        userPrefs.clear()
        assertThatPrefsAreCleared()
    }

    @Test
    fun `check if everthing is cleared`() {
        userPrefs.setUserId("1")
        userManager.clear()
        assertThatPrefsAreCleared()
    }

    private fun assertThatPrefsAreCleared() {
        val userId = userPrefs.getUserId()
        val oldUserId = userPrefs.getOldUserId()
        assertNull(userId)
        assertNull(oldUserId)
    }

    @Test
    fun `check if user prefs returns user id`() {
        userPrefs.clear()
        userPrefs.setUserId("1")
        val userId = userPrefs.getUserId()
        MatcherAssert.assertThat(userId, Matchers.`is`("1"))
    }

    @Test
    fun `test getting user before init`() {
        val currentUser = userManager.getCurrentUser().toBlocking().value()
        assertNull(currentUser)
    }

    // TODO Mock RecipientManager in UserManager. Not possible atm because ToshiManager is null
}