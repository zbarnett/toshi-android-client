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

package com.toshi.util.sharedPrefs

import android.preference.PreferenceManager
import com.toshi.extensions.applyBoolean
import com.toshi.extensions.applyByteArray
import com.toshi.extensions.applyClear
import com.toshi.extensions.applyInt
import com.toshi.extensions.applyString
import com.toshi.extensions.getBoolean
import com.toshi.extensions.getByteArray
import com.toshi.extensions.getString
import com.toshi.util.sharedPrefs.SignalPrefsInterface.Companion.LOCAL_REGISTRATION_ID
import com.toshi.util.sharedPrefs.SignalPrefsInterface.Companion.PASSWORD
import com.toshi.util.sharedPrefs.SignalPrefsInterface.Companion.REGISTERED_WITH_SERVER
import com.toshi.util.sharedPrefs.SignalPrefsInterface.Companion.SERIALIZED_IDENTITY_KEY_PAIR
import com.toshi.util.sharedPrefs.SignalPrefsInterface.Companion.SERIALIZED_LAST_RESORT_KEY
import com.toshi.util.sharedPrefs.SignalPrefsInterface.Companion.SIGNALING_KEY
import com.toshi.util.sharedPrefs.SignalPrefsInterface.Companion.SIGNED_PRE_KEY_ID
import com.toshi.view.BaseApplication

object SignalPrefs : SignalPrefsInterface {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(BaseApplication.get()) }

    override fun getRegisteredWithServer(): Boolean = prefs.getBoolean(REGISTERED_WITH_SERVER)

    override fun setRegisteredWithServer() = prefs.applyBoolean(REGISTERED_WITH_SERVER, true)

    override fun getLocalRegistrationId(): Int = prefs.getInt(LOCAL_REGISTRATION_ID, -1)

    override fun setLocalRegistrationId(registrationId: Int) {
        prefs.applyInt(LOCAL_REGISTRATION_ID, registrationId)
    }

    override fun getSignalingKey(): String? = prefs.getString(SIGNALING_KEY)

    override fun setSignalingKey(signalingKey: String) = prefs.applyString(SIGNALING_KEY, signalingKey)

    override fun getPassword(): String? = prefs.getString(PASSWORD)

    override fun setPassword(password: String) = prefs.applyString(PASSWORD, password)

    override fun getSerializedIdentityKeyPair(): ByteArray? {
        return prefs.getByteArray(SERIALIZED_IDENTITY_KEY_PAIR)
    }

    override fun setSerializedIdentityKeyPair(serializedIdentityKeyPair: ByteArray) {
        prefs.applyByteArray(SERIALIZED_IDENTITY_KEY_PAIR, serializedIdentityKeyPair)
    }

    override fun getSerializedLastResortKey(): ByteArray? {
        return prefs.getByteArray(SERIALIZED_LAST_RESORT_KEY)
    }

    override fun setSerializedLastResortKey(serializedLastResortKey: ByteArray) {
        prefs.applyByteArray(SERIALIZED_LAST_RESORT_KEY, serializedLastResortKey)
    }

    override fun getSignedPreKeyId(): Int = prefs.getInt(SIGNED_PRE_KEY_ID, -1)

    override fun setSignedPreKeyId(signedPreKeyId: Int) {
        prefs.applyInt(SIGNED_PRE_KEY_ID, signedPreKeyId)
    }

    override fun clear() = prefs.applyClear()
}