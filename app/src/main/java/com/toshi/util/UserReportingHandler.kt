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

import android.app.Dialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import com.toshi.R
import com.toshi.model.local.Report

class UserReportingHandler(
        private val context: Context,
        private val onReportListener: (Report) -> Unit) {

    private var reportDialog: Dialog? = null
    private var confirmationDialog: AlertDialog? = null

    fun showReportDialog(userAddress: String) {
        this.reportDialog = Dialog(this.context)
        this.reportDialog?.setContentView(R.layout.view_report_dialog)
        this.reportDialog?.findViewById<View>(R.id.spam)?.setOnClickListener { reportSpam(userAddress) }
        this.reportDialog?.findViewById<View>(R.id.inappropriate)?.setOnClickListener { reportInappropriate(userAddress) }
        this.reportDialog?.show()
    }

    private fun reportSpam(userAddress: String) {
        val details = context.getString(R.string.report_spam)
        val report = Report()
                .setUserAddress(userAddress)
                .setDetails(details)

        onReportListener(report)
    }

    private fun reportInappropriate(userAddress: String) {
        val details = context.getString(R.string.report_inappropriate)
        val report = Report()
                .setUserAddress(userAddress)
                .setDetails(details)

        onReportListener(report)
    }

    fun showConfirmationDialog() {
        this.reportDialog?.dismiss()

        val builder = DialogUtil.getBaseDialog(
                this.context,
                R.string.report_confirmation_title,
                R.string.report_confirmation_message,
                R.string.dismiss
        )

        this.confirmationDialog = builder.create()
        this.confirmationDialog?.show()
    }

    fun clear() {
        this.reportDialog?.dismiss()
        this.confirmationDialog?.dismiss()
    }
}