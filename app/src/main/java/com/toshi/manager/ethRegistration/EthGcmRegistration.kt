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

package com.toshi.manager.ethRegistration

import com.toshi.crypto.HDWallet
import com.toshi.manager.network.EthereumInterface
import com.toshi.manager.network.EthereumService
import com.toshi.model.local.network.Network
import com.toshi.model.local.network.Networks
import com.toshi.model.network.GcmDeregistration
import com.toshi.model.network.GcmRegistration
import com.toshi.model.network.ServerTime
import com.toshi.util.gcm.GcmToken
import com.toshi.util.gcm.GcmTokenInterface
import com.toshi.util.logging.LogUtil
import com.toshi.util.sharedPrefs.AppPrefsInterface
import com.toshi.util.sharedPrefs.EthGcmPrefs
import com.toshi.util.sharedPrefs.EthGcmPrefsInterface
import rx.Completable
import rx.Scheduler
import rx.schedulers.Schedulers

class EthGcmRegistration(
        private val networks: Networks = Networks.getInstance(),
        private val appPrefs: AppPrefsInterface,
        private val ethService: EthereumInterface,
        private val gcmPrefs: EthGcmPrefsInterface = EthGcmPrefs(),
        private val gcmToken: GcmTokenInterface = GcmToken(),
        private val subscribeOnScheduler: Scheduler = Schedulers.io()
) {

    private lateinit var wallet: HDWallet

    fun init(wallet: HDWallet) {
        this.wallet = wallet
    }

    //Don't unregister the default network
    fun changeNetwork(network: Network): Completable {
        return if (networks.onDefaultNetwork()) {
            changeEthBaseUrl(network)
                    .andThen(registerEthGcm())
                    .subscribeOn(subscribeOnScheduler)
                    .doOnCompleted { appPrefs.setCurrentNetwork(network) }
        } else gcmToken
                .get()
                .flatMapCompletable { unregisterFromEthGcm(it) }
                .andThen(changeEthBaseUrl(network))
                .andThen(registerEthGcm())
                .subscribeOn(subscribeOnScheduler)
                .doOnCompleted { appPrefs.setCurrentNetwork(network) }
    }

    fun unregisterFromEthGcm(token: String?): Completable {
        val currentNetworkId = networks.currentNetwork.id
        return ethService
                .timestamp
                .subscribeOn(subscribeOnScheduler)
                .flatMapCompletable { unregisterEthGcmWithTimestamp(token, it) }
                .doOnCompleted { gcmPrefs.setEthGcmTokenSentToServer(currentNetworkId, false) }
    }

    private fun changeEthBaseUrl(network: Network): Completable {
        return Completable.fromAction { EthereumService.get().changeBaseUrl(network.url) }
    }

    fun forceRegisterEthGcm(): Completable {
        if (!::wallet.isInitialized) {
            return Completable.error(IllegalStateException("Unable to register GCM as class hasn't been initialised yet"))
        }
        val currentNetworkId = networks.currentNetwork.id
        gcmPrefs.setEthGcmTokenSentToServer(currentNetworkId, false)
        return registerEthGcm()
    }

    private fun registerEthGcm(): Completable {
        val currentNetworkId = networks.currentNetwork.id
        return if (gcmPrefs.isEthGcmTokenSentToServer(currentNetworkId)) Completable.complete()
        else gcmToken
                .get()
                .flatMapCompletable { registerEthGcmToken(it) }
    }

    private fun registerEthGcmToken(token: String?): Completable {
        return ethService
                .timestamp
                .subscribeOn(subscribeOnScheduler)
                .flatMapCompletable { registerEthGcmWithTimestamp(token, it) }
                .doOnCompleted { setEthGcmTokenSentToServer() }
                .doOnError { handleGcmRegisterError(it) }
    }

    private fun setEthGcmTokenSentToServer() {
        val currentNetworkId = networks.currentNetwork.id
        gcmPrefs.setEthGcmTokenSentToServer(currentNetworkId, true)
    }

    @Throws(IllegalStateException::class)
    private fun registerEthGcmWithTimestamp(token: String?, serverTime: ServerTime?): Completable {
        return when {
            serverTime == null -> throw IllegalStateException("ServerTime was null")
            token == null -> throw IllegalStateException("token was null")
            else -> ethService.registerGcm(serverTime.get(), GcmRegistration(token, wallet.paymentAddress))
        }
    }

    private fun handleGcmRegisterError(throwable: Throwable) {
        LogUtil.exception("Error during registering of GCM", throwable)
        val currentNetworkId = networks.currentNetwork.id
        gcmPrefs.setEthGcmTokenSentToServer(currentNetworkId, false)
    }

    private fun unregisterEthGcmWithTimestamp(token: String?, serverTime: ServerTime?): Completable {
        return when {
            serverTime == null -> throw IllegalStateException("Unable to fetch server time")
            token == null -> throw IllegalStateException("token was null")
            else -> ethService.unregisterGcm(serverTime.get(), GcmDeregistration(token))
        }
    }

    fun clear() = gcmPrefs.clear()
}