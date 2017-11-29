package com.toshi.view.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.extensions.isVisible
import com.toshi.extensions.startActivity
import com.toshi.extensions.startActivityAndFinish
import com.toshi.extensions.toast
import com.toshi.util.TermsDialog
import com.toshi.viewModel.LandingViewModel
import kotlinx.android.synthetic.main.activity_landing.*

class LandingActivity : AppCompatActivity() {

    private lateinit var viewModel: LandingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(LandingViewModel::class.java)
    }

    private fun initClickListeners() {
        signIn.setOnClickListener { startActivity<SignInActivity>() }
        createNewAccount.setOnClickListener { showsTermDialog() }
    }

    private fun showsTermDialog() {
        TermsDialog(
                this,
                { viewModel.handleCreateNewAccountClicked() }
        ).show()
    }

    private fun initObservers() {
        viewModel.isLoading.observe(this, Observer {
            isLoading -> isLoading?.let { loadingSpinner.isVisible(it) }
        })
        viewModel.walletError.observe(this, Observer {
            errorMessage -> errorMessage?.let { toast(it) }
        })
        viewModel.onboardingBotId.observe(this, Observer {
            onboardingBotId -> onboardingBotId?.let { goToChatActivity(it) }
        })
        viewModel.onboardingError.observe(this, Observer {
            startActivityAndFinish<MainActivity>()
        })
    }

    private fun goToChatActivity(onboardingBotId: String) {
        val mainIntent = Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA__ACTIVE_TAB, 1)

        val chatIntent = Intent(this, ChatActivity::class.java)
                .putExtra(ChatActivity.EXTRA__THREAD_ID, onboardingBotId)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val nextIntent = TaskStackBuilder.create(this)
                .addParentStack(MainActivity::class.java)
                .addNextIntent(mainIntent)
                .addNextIntent(chatIntent)

        startActivities(nextIntent.intents)
        finish()
    }
}