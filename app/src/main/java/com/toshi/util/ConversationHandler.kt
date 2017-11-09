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

import android.support.v4.app.Fragment
import android.util.Pair
import com.toshi.model.local.Conversation
import com.toshi.view.BaseApplication
import com.toshi.view.fragment.DialogFragment.ConversationOptionsDialogFragment
import com.toshi.view.fragment.DialogFragment.Option
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class ConversationHandler(
        private val fragment: Fragment,
        private val deleteListener:
        (Conversation) -> Unit) {

    private val subscriptions by lazy { CompositeSubscription() }

    fun showConversationOptionsDialog(conversation: Conversation) {
        val sub = Single.zip(
                isMuted(conversation),
                isBlocked(conversation),
                { first, second -> Pair(first, second) }
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
                { pair -> showConversationOptionsDialog(conversation, pair.first, pair.second) },
                { throwable -> LogUtil.e(javaClass, "Error: " + throwable) }
        )

        this.subscriptions.add(sub)
    }

    //TODO: Handle users in groups being blocked
    private fun isBlocked(conversation: Conversation): Single<Boolean> {
        return BaseApplication
                .get()
                .recipientManager
                .isUserBlocked(conversation.threadId)
    }

    private fun isMuted(conversation: Conversation): Single<Boolean> {
        return getRecipientManager()
                .isConversationMuted(conversation.threadId)
    }

    private fun showConversationOptionsDialog(conversation: Conversation, isMuted: Boolean, isBlocked: Boolean) {
        val fragment = ConversationOptionsDialogFragment.newInstance(isMuted, isBlocked)
                .setItemClickListener({ handleSelectedOption(conversation, it) })
        fragment.show(this.fragment.fragmentManager, ConversationOptionsDialogFragment.TAG)
    }

    private fun handleSelectedOption(conversation: Conversation, option: Option) {
        when (option) {
            Option.UNMUTE -> setMute(conversation, false)
            Option.MUTE -> setMute(conversation, true)
            Option.UNBLOCK -> setBlocked(conversation, false)
            Option.BLOCK -> setBlocked(conversation, true)
            Option.DELETE -> deleteConversation(conversation)
        }
    }

    private fun setMute(conversation: Conversation, mute: Boolean) {
       val muteAction =
               if (mute) getRecipientManager().muteConveration(conversation.threadId)
               else getRecipientManager().unmuteConversation(conversation.threadId)

        muteAction
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { throwable -> LogUtil.e(javaClass, "Error while muting/unmuting conversation " + throwable) }
                )
    }

    private fun setBlocked(conversation: Conversation, block: Boolean) {
        val blockAction =
                if (block) getRecipientManager().blockUser(conversation.threadId)
                else getRecipientManager().unblockUser(conversation.threadId)

        blockAction
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { },
                        { throwable -> LogUtil.e(javaClass, "Error while blocking/unblocking user " + throwable) }
                )
    }

    private fun getRecipientManager() = BaseApplication.get().recipientManager

    private fun deleteConversation(conversation: Conversation) {
        val sub = BaseApplication
                .get()
                .sofaMessageManager
                .deleteConversation(conversation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { deleteListener(conversation) },
                        { throwable -> LogUtil.e(javaClass, "Error while blocking user " + throwable) }
                )

        this.subscriptions.add(sub)
    }

    fun clear() = subscriptions.clear()
}