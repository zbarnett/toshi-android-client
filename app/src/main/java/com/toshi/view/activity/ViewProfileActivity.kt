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
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.toshi.R
import com.toshi.extensions.startActivity
import com.toshi.model.local.User
import com.toshi.model.network.ReputationScore
import com.toshi.util.ImageUtil
import com.toshi.viewModel.ViewProfileViewModel
import kotlinx.android.synthetic.main.activity_view_profile.*

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)
        init()
    }

    private fun init() {
        initViewModel()
        initClickListeners()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ViewProfileViewModel::class.java)
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener { finish() }
        editProfileButton.setOnClickListener { startActivity<EditProfileActivity>() }
        avatar.setOnClickListener { startFullscreenImageActivity() }
    }

    private fun startFullscreenImageActivity() {
        val avatar = viewModel.user.value?.avatar
        avatar?.let {
            startActivity<FullscreenImageActivity> { putExtra(FullscreenImageActivity.IMAGE_URL, it) }
        }
    }

    private fun initObservers() {
        viewModel.user.observe(this, Observer {
            user -> user?.let { updateView(it) } ?: handleNoUser()
        })
        viewModel.reputation.observe(this, Observer {
            reputation -> reputation?.let { handleReputation(it) }
        })
        viewModel.isConnected.observe(this, Observer {
            isConnected -> isConnected?.let { editProfileButton.isEnabled = isConnected }
        })
    }

    private fun updateView(localUser: User) {
        name.text = localUser.displayName
        username.text = localUser.username
        aboutUser.text = localUser.about
        location.text = localUser.location
        ImageUtil.load(localUser.avatar, avatar)
    }

    private fun handleNoUser() {
        name.text = getString(R.string.profile__unknown_name)
        username.text = ""
        aboutUser.text = ""
        location.text = ""
        ratingView.setStars(0.0)
    }

    private fun handleReputation(reputation: ReputationScore) {
        val revCount = reputation.reviewCount
        val ratingText = resources.getQuantityString(R.plurals.ratings, revCount, revCount)
        reviewCount.text = ratingText
        ratingView.setStars(reputation.averageRating)
        reputationScore.text = reputation.averageRating.toString()
        ratingInfo.setRatingInfo(reputation)
    }
}