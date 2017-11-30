package com.toshi.presenter.chat

import com.toshi.model.local.User
import com.toshi.model.sofa.SofaMessage

data class ResendPaymentInfo(
        val receiver: User,
        val sofaMessage: SofaMessage
)