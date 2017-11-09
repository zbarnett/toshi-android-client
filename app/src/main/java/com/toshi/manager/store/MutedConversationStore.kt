package com.toshi.manager.store

import com.toshi.model.local.MutedConversation
import com.toshi.view.BaseApplication
import rx.Completable
import rx.Single

class MutedConversationStore {
    fun isMuted(threadId: String): Single<Boolean> {
        return Single.fromCallable {
            val realm = BaseApplication.get().realm
            val mutedConversation = realm.where(MutedConversation::class.java)
                    .equalTo("threadId", threadId)
                    .findFirst()
            val isMuted = mutedConversation != null
            realm.close()
            isMuted
        }
    }

    fun save(threadId: String): Completable {
        return Completable.fromAction {
            val realm = BaseApplication.get().realm
            realm.beginTransaction()
            realm.insertOrUpdate(MutedConversation(threadId))
            realm.commitTransaction()
            realm.close()
        }
    }

    fun delete(threadId: String): Completable {
        return Completable.fromAction {
            val realm = BaseApplication.get().realm
            realm.beginTransaction()
            realm
                    .where(MutedConversation::class.java)
                    .equalTo("threadId", threadId)
                    .findFirst()
                    ?.deleteFromRealm()
            realm.commitTransaction()
            realm.close()
        }
    }
}