/*
 *
 *  * 	Copyright (c) 2018. Toshi Inc
 *  *
 *  * 	This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License, or
 *  *     (at your option) any later version.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.toshi.crypto

import android.content.Context
import com.toshi.util.sharedPrefs.WalletPrefs
import com.toshi.util.sharedPrefs.WalletPrefsInterface
import com.toshi.view.BaseApplication
import rx.Single

class HdWalletBuilder(
        private val walletPrefs: WalletPrefsInterface = WalletPrefs(),
        private val context: Context = BaseApplication.get()
) {

    private val hdWallet by lazy { HDWallet(walletPrefs, context) }

    fun createWallet(): Single<HDWallet> = hdWallet.createWallet()

    fun createFromMasterSeed(masterSeed: String): Single<HDWallet> {
        return hdWallet.createFromMasterSeed(masterSeed)
    }

    fun getExistingWallet(): Single<HDWallet> = hdWallet.getExistingWallet()
}