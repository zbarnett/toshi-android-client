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

import android.content.Context
import com.toshi.util.FileNames
import com.toshi.view.BaseApplication

class EthGcmPrefs : EthGcmPrefsInterface {

    private val prefs by lazy { BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE) }

    override fun setEthGcmTokenSentToServer(networkId: String, isSentToServer: Boolean) {
        prefs.edit().putBoolean(networkId, isSentToServer).commit()
    }

    override fun isEthGcmTokenSentToServer(networkId: String) = prefs.getBoolean(networkId, false)

    override fun clear() = prefs.edit().clear().apply()
}