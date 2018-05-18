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

package com.toshi.view.activity

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.extensions.getViewModel
import com.toshi.extensions.isVisible
import com.toshi.extensions.startActivity
import com.toshi.extensions.toast
import com.toshi.util.KeyboardUtil
import com.toshi.util.sharedPrefs.AppPrefs
import com.toshi.viewModel.SignInViewModel
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    companion object {
        private const val PASSPHRASE_LENGTH = 12
    }

    private lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        initSignInPassphraseView()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = getViewModel()
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
        infoView.setOnClickListener { startActivity<SignInInfoActivity>() }
        signIn.setOnClickListener { handleSignInClicked() }
    }

    private fun initSignInPassphraseView() {
        passphraseInputView.apply {
            setOnPassphraseFinishListener { handlePassphraseFinished() }
            setOnPassphraseUpdateListener { updateSignInButton(it) }
            setOnKeyboardListener { keyboard.showKeyboard() }
        }
    }

    private fun handlePassphraseFinished() {
        signIn.setText(R.string.sign_in)
        signIn.setBackgroundResource(R.drawable.background_with_radius_primary_color)
        signIn.isEnabled = true
    }

    private fun updateSignInButton(approvedWords: Int) {
        val wordsLeft = PASSPHRASE_LENGTH - approvedWords
        if (wordsLeft > 0) {
            val wordsLeftString = resources.getQuantityString(R.plurals.words, wordsLeft, wordsLeft)
            disableSignIn(wordsLeftString)
        }
    }

    private fun disableSignIn(string: String) {
        signIn.text = string
        signIn.setBackgroundResource(R.drawable.background_with_radius_disabled)
        signIn.isEnabled = false
    }

    private fun handleSignInClicked() {
        val approvedWords = passphraseInputView.approvedWordList
        if (approvedWords.size != PASSPHRASE_LENGTH) {
            toast(R.string.sign_in_length_error_message)
            return
        }

        val masterSeed = approvedWords.joinToString(" ")
        viewModel.tryCreateWallet(masterSeed)
    }

    private fun initObservers() {
        viewModel.isLoading.observe(this, Observer {
            if (it != null) loadingSpinner.isVisible(it)
        })
        viewModel.passphrase.observe(this, Observer {
            if (it != null) passphraseInputView.setWordList(it as ArrayList<String>)
        })
        viewModel.walletSuccess.observe(this, Observer {
            if (it != null) goToMainActivity()
        })
        viewModel.error.observe(this, Observer {
            if (it != null) toast(it)
        })
    }

    private fun goToMainActivity() {
        KeyboardUtil.hideKeyboard(passphraseInputView)
        AppPrefs.setSignedIn()
        startActivity<MainActivity> { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
        ActivityCompat.finishAffinity(this)
    }

    override fun onBackPressed() {
        if (keyboard.isVisible()) keyboard.hideKeyboard()
        else super.onBackPressed()
    }
}