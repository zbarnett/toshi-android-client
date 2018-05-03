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

package com.toshi.util.sharedPrefs

import com.toshi.model.local.network.Network

interface AppPrefsInterface {

    companion object {
        const val HAS_ONBOARDED = "hasOnboarded"
        const val HAS_SIGNED_OUT = "hasSignedIn"
        const val HAS_BACKED_UP_PHRASE = "hasBackedUpPhrase"
        const val HAS_LOADED_APP_FIRST_TIME = "hasLoadedAppFirstTime"
        const val LOCAL_CURRENCY_CODE = "localCurrencyCode"
        const val WAS_MIGRATED = "wasMigrated"
        const val FORCE_USER_UPDATE = "forceUserUpdate_2"
        const val CURRENT_NETWORK = "currentNetwork"
        const val HAS_CLEARED_NOTIFICATION_CHANNELS = "hasClearedNotificationChannels"
    }

    fun hasOnboarded(): Boolean
    fun setHasOnboarded(hasOnboarded: Boolean)
    fun hasLoadedApp(): Boolean
    fun setHasLoadedApp()
    fun setSignedIn()
    fun setSignedOut()
    fun hasSignedOut(): Boolean
    fun setHasBackedUpPhrase()
    fun hasBackedUpPhrase(): Boolean
    fun saveCurrency(currencyCode: String)
    fun getCurrency(): String
    fun getCurrencyFromLocaleAndSave(): String
    fun setWasMigrated(wasMigrated: Boolean)
    fun wasMigrated(): Boolean
    fun setForceUserUpdate(forceUpdate: Boolean)
    fun shouldForceUserUpdate(): Boolean
    fun setCurrentNetwork(network: Network)
    fun getCurrentNetworkId(): String?
    fun setHasClearedNotificationChannels()
    fun hasClearedNotificationChannels(): Boolean
    fun clear()
}