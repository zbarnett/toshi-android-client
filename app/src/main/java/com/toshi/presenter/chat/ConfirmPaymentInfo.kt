package com.toshi.presenter.chat

import com.toshi.model.local.User

data class ConfirmPaymentInfo(
        val receiver: User,
        val amount: String
)