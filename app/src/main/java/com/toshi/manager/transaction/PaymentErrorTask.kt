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

package com.toshi.manager.transaction

import com.toshi.R
import com.toshi.manager.model.PaymentTask
import com.toshi.model.local.Recipient
import com.toshi.model.local.User
import com.toshi.model.network.SofaError
import com.toshi.model.sofa.SofaAdapters
import com.toshi.model.sofa.SofaMessage
import com.toshi.util.LocaleUtil
import com.toshi.util.LogUtil
import com.toshi.view.BaseApplication
import com.toshi.view.notification.ChatNotificationManager
import com.toshi.view.notification.ExternalPaymentNotificationManager
import retrofit2.HttpException
import java.io.IOException

class PaymentErrorTask {

    fun handleOutgoingExternalPaymentError(error: Throwable, paymentTask: PaymentTask) {
        LogUtil.exception(javaClass, "Error sending payment.", error)
        val payment = paymentTask.payment
        val paymentAddress = payment.toAddress
                ?.let { it }
                ?: BaseApplication.get().getString(R.string.unknown)
        ExternalPaymentNotificationManager.showExternalPaymentFailed(paymentAddress)
    }

    fun handleOutgoingPaymentError(error: Throwable, receiver: User, storedSofaMessage: SofaMessage) {
        val errorMessage = parseErrorResponse(error)
        storedSofaMessage.errorMessage = errorMessage
        LogUtil.exception(javaClass, "Error creating transaction $error")
        showOutgoingPaymentFailedNotification(receiver)
    }

    private fun parseErrorResponse(error: Throwable): SofaError? {
        return if (error is HttpException) parseErrorResponse(error)
        else SofaError().createNotDeliveredMessage(BaseApplication.get())
    }

    private fun parseErrorResponse(error: HttpException): SofaError? {
        return try {
            val body = error.response().errorBody()?.string() ?: ""
            SofaAdapters.get().sofaErrorsFrom(body)
        } catch (e: IOException) {
            LogUtil.e(javaClass, "Error while parsing payment error response $e")
            null
        }
    }

    private fun showOutgoingPaymentFailedNotification(user: User) {
        val content = getNotificationContent(user.displayName)
        ChatNotificationManager.showChatNotification(Recipient(user), content)
    }

    private fun getNotificationContent(content: String): String {
        return String.format(
                LocaleUtil.getLocale(),
                BaseApplication.get().getString(R.string.payment_failed_message),
                content
        )
    }
}