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

package com.toshi.util

import android.content.Context
import android.content.DialogInterface
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import com.toshi.R
import com.toshi.viewModel.BlockingAction

class UserBlockingHandler(
        private val context: Context,
        private val onBlock: () -> Unit,
        private val onUnblock: () -> Unit) {

    private var blockedUserDialog: AlertDialog? = null
    private var confirmationDialog: AlertDialog? = null

    fun showDialog(isBlocked: Boolean) {
        if (isBlocked) showUnblockedDialog()
        else showBlockedDialog()
    }

    private fun showUnblockedDialog() {
        showBlockingDialog(
                R.string.unblock_user_dialog_title,
                R.string.unblock_user_dialog_message,
                R.string.unblock,
                R.string.cancel,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                    onUnblock()
                }
        )
    }

    private fun showBlockedDialog() {
        showBlockingDialog(
                R.string.block_user_dialog_title,
                R.string.block_user_dialog_message,
                R.string.block,
                R.string.cancel,
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                    onBlock()
                }
        )
    }

    private fun showBlockingDialog(@StringRes title: Int,
                                   @StringRes message: Int,
                                   @StringRes positiveButtonText: Int,
                                   @StringRes negativeButtonText: Int,
                                   listener: DialogInterface.OnClickListener) {
        val builder = DialogUtil.getBaseDialog(
                this.context,
                title,
                message,
                positiveButtonText,
                negativeButtonText,
                listener
        )

        this.blockedUserDialog = builder.create()
        this.blockedUserDialog?.show()
    }

    fun showConfirmationDialog(blockedAction: BlockingAction) {
        if (blockedAction == BlockingAction.BLOCKED) handleBlockerUser()
        else handleUnblockUser()
    }

    private fun handleBlockerUser() {
        showConfirmationDialog(
                R.string.block_user_confirmation_dialog_title,
                R.string.block_user_confirmation_dialog_message,
                R.string.dismiss
        )
    }

    private fun handleUnblockUser() {
        showConfirmationDialog(
                R.string.unblock_user_confirmation_dialog_title,
                R.string.unblock_user_confirmation_dialog_message,
                R.string.dismiss
        )
    }

    private fun showConfirmationDialog(@StringRes title: Int,
                                       @StringRes message: Int,
                                       @StringRes negativeButtonText: Int) {
        val baseDialog = DialogUtil.getBaseDialog(
                this.context,
                title,
                message,
                negativeButtonText
        )

        this.confirmationDialog = baseDialog.create()
        this.confirmationDialog?.show()
    }

    fun clear() {
        this.blockedUserDialog?.dismiss()
        this.confirmationDialog?.dismiss()
    }
}