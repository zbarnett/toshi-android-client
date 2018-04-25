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

interface SignalPrefsInterface {

    companion object {
        const val LOCAL_REGISTRATION_ID = "pref_local_registration_id"
        const val SERIALIZED_IDENTITY_KEY_PAIR = "serialized_identity_key_pair_pref"
        const val SERIALIZED_LAST_RESORT_KEY = "serialized_last_resort_key_pref"
        const val SIGNALING_KEY = "signaling_key_pref"
        const val SIGNED_PRE_KEY_ID = "signed_pre_key_id"
        const val PASSWORD = "password_pref"
        const val REGISTERED_WITH_SERVER = "have_registered_with_server_pref"
    }

    fun getRegisteredWithServer(): Boolean
    fun setRegisteredWithServer()
    fun getLocalRegistrationId(): Int
    fun setLocalRegistrationId(registrationId: Int)
    fun getSignalingKey(): String?
    fun setSignalingKey(signalingKey: String)
    fun getPassword(): String?
    fun setPassword(password: String)
    fun getSerializedIdentityKeyPair(): ByteArray?
    fun setSerializedIdentityKeyPair(serializedIdentityKeyPair: ByteArray)
    fun getSerializedLastResortKey(): ByteArray?
    fun setSerializedLastResortKey(serializedLastResortKey: ByteArray)
    fun getSignedPreKeyId(): Int
    fun setSignedPreKeyId(signedPreKeyId: Int)
    fun clear()
}