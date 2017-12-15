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

package com.toshi.manager.chat.tasks

import org.whispersystems.signalservice.api.push.exceptions.EncapsulatedExceptions
import rx.Completable

abstract class BaseGroupTask {
    // Ignore EncapsulatedExceptions if an user has signed out
    protected fun handleException(throwable: Throwable): Completable {
        if (throwable is EncapsulatedExceptions) {
            val isUnregisteredUsers = !throwable.unregisteredUserExceptions.isEmpty()
            val isNetworkExceptions = !throwable.networkExceptions.isEmpty()
            val isUntrustedIdentityExceptions = !throwable.untrustedIdentityExceptions.isEmpty()
            if (isUnregisteredUsers && !isNetworkExceptions && !isUntrustedIdentityExceptions) {
                return Completable.complete()
            }
        }
        return Completable.error(throwable)
    }
}