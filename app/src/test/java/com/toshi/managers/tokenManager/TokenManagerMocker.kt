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

package com.toshi.managers.tokenManager

import com.toshi.crypto.HDWallet
import com.toshi.manager.network.EthereumServiceInterface
import com.toshi.manager.token.TokenManager
import com.toshi.managers.balanceManager.EthereumServiceMocker
import com.toshi.masterSeed
import com.toshi.mockWallet
import com.toshi.mockWalletSubject
import com.toshi.model.local.network.Network
import com.toshi.model.local.network.Networks
import com.toshi.testSharedPrefs.TestAppPrefs
import com.toshi.testStores.TestTokenStore
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

class TokenManagerMocker {

    fun mock(): TokenManager {
        return TokenManager(
                tokenStore = TestTokenStore(),
                networks = mockNetworks(),
                ethService = mockEthService(),
                walletObservable = mockWalletObservable(),
                connectivitySubject = mockConnectivitySubject(),
                scheduler = Schedulers.trampoline()
        )
    }

    private fun mockNetworks(): Networks {
        val networkList = listOf(
                Network("1", "Mainnet", "https://www.mainnet.org"),
                Network("3", "Ropsten", "https://www.ropsten.org")
        )
        val appPrefs = TestAppPrefs()
        return Networks.getInstance(networkList, appPrefs)
    }

    private fun mockEthService(): EthereumServiceInterface = EthereumServiceMocker().mock()

    private fun mockWalletObservable(): Observable<HDWallet> {
        val wallet = mockWallet(masterSeed)
        return mockWalletSubject(wallet)
    }

    private fun mockConnectivitySubject(): BehaviorSubject<Boolean> {
        val subject = BehaviorSubject.create<Boolean>()
        subject.onNext(true)
        return subject
    }
}