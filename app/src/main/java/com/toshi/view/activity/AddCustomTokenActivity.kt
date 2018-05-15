/*
 * Copyright (c) 2017. Toshi Inc
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

package com.toshi.view.activity

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import com.toshi.R
import com.toshi.crypto.util.isPaymentAddressValid
import com.toshi.extensions.getViewModel
import com.toshi.extensions.isVisible
import com.toshi.extensions.safeToInt
import com.toshi.extensions.startActivityForResult
import com.toshi.extensions.toast
import com.toshi.model.network.token.CustomERCToken
import com.toshi.util.KeyboardUtil
import com.toshi.util.QrCodeHandler
import com.toshi.util.ScannerResultType
import com.toshi.view.adapter.listeners.TextChangedListener
import com.toshi.viewModel.AddCustomTokenViewModel
import kotlinx.android.synthetic.main.activity_add_custom_token.addTokenBtn
import kotlinx.android.synthetic.main.activity_add_custom_token.contractAddress
import kotlinx.android.synthetic.main.activity_add_custom_token.decimals
import kotlinx.android.synthetic.main.activity_add_custom_token.loadingOverlay
import kotlinx.android.synthetic.main.activity_add_custom_token.name
import kotlinx.android.synthetic.main.activity_add_custom_token.symbol
import kotlinx.android.synthetic.main.activity_chat.closeButton

class AddCustomTokenActivity : AppCompatActivity() {

    companion object {
        private const val PAYMENT_SCAN_REQUEST_CODE = 200
    }

    private lateinit var viewModel: AddCustomTokenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_custom_token)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        initScannerClickListener()
        initTextListeners()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = getViewModel()
    }

    private fun initClickListeners() {
        addTokenBtn.setOnClickListener { handleAddTokenBtnClicked() }
        closeButton.setOnClickListener { hideKeyboardAndFinish() }
    }

    private fun handleAddTokenBtnClicked() {
        KeyboardUtil.hideKeyboard(contractAddress)
        viewModel.addCustomToken(buildCustomERCToken())
    }

    private fun buildCustomERCToken(): CustomERCToken {
        val contractAddress: String = contractAddress.text.toString()
        val name = name.text.toString()
        val symbol = symbol.text.toString()
        val decimals = decimals.text.toString().safeToInt()
        return CustomERCToken(
                contractAddress = contractAddress,
                name = name,
                symbol = symbol,
                decimals = decimals
        )
    }

    private fun hideKeyboardAndFinish() {
        KeyboardUtil.hideKeyboard(contractAddress)
        finish()
    }

    private fun initScannerClickListener() {
        contractAddress.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val rightDrawable = 2
                val widthOfRightDrawable = contractAddress.compoundDrawables[rightDrawable].bounds.width()
                if (event.rawX >= (contractAddress.right - widthOfRightDrawable)) {
                    startScanQrActivity()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun startScanQrActivity() = startActivityForResult<ScannerActivity>(PAYMENT_SCAN_REQUEST_CODE) {
        putExtra(ScannerActivity.SCANNER_RESULT_TYPE, ScannerResultType.PAYMENT_ADDRESS)
    }

    private fun initTextListeners() {
        contractAddress.addTextChangedListener(textListener)
        name.addTextChangedListener(textListener)
        symbol.addTextChangedListener(textListener)
        decimals.addTextChangedListener(textListener)
    }

    private val textListener = object : TextChangedListener() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val isInputValid = isInputValid()
            if (isInputValid) enableAddBtn()
            else disableAddBtn()
        }
    }

    private fun isInputValid(): Boolean {
        val isContractAddressValid = isPaymentAddressValid(contractAddress.text.toString())
        val isNameValid = name.text.isNotEmpty()
        val isSymbolValid = symbol.text.isNotEmpty()
        val isDecimalsValid = decimals.text.isNotEmpty()
        return isContractAddressValid && isNameValid && isSymbolValid && isDecimalsValid
    }

    private fun enableAddBtn() {
        addTokenBtn.isClickable = true
        addTokenBtn.setBackgroundResource(R.drawable.background_with_radius_primary_color)
    }

    private fun disableAddBtn() {
        addTokenBtn.isClickable = false
        addTokenBtn.setBackgroundResource(R.drawable.background_with_radius_disabled)
    }

    private fun initObservers() {
        viewModel.isLoading.observe(this, Observer {
            if (it != null) loadingOverlay.isVisible(it)
        })
        viewModel.error.observe(this, Observer {
            if (it != null) toast(it)
        })
        viewModel.success.observe(this, Observer {
            if (it != null) toast(R.string.add_custom_token_success)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultIntent)
        if (requestCode != PAYMENT_SCAN_REQUEST_CODE || resultCode != Activity.RESULT_OK || resultIntent == null) return
        val paymentAddress = resultIntent.getStringExtra(QrCodeHandler.ACTIVITY_RESULT)
        contractAddress.setText(paymentAddress)
    }
}