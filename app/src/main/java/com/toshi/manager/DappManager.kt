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

package com.toshi.manager

import com.toshi.manager.network.DirectoryService
import com.toshi.model.network.dapp.DappResult
import com.toshi.model.network.dapp.DappSections
import com.toshi.model.network.dapp.Dapps
import rx.Single
import rx.schedulers.Schedulers

class DappManager {

    fun getFrontPageDapps(): Single<DappSections> {
        return DirectoryService
                .get()
                .getFrontpageDapps()
                .subscribeOn(Schedulers.io())
    }

    fun search(input: String): Single<Dapps> {
        return DirectoryService
                .get()
                .search(input)
                .map { it.results }
                .subscribeOn(Schedulers.io())
    }

    fun getAllDapps(): Single<Dapps> {
        return DirectoryService
                .get()
                .getAllDapps()
                .map { it.results }
                .subscribeOn(Schedulers.io())
    }

    fun getAllDappsInCategory(categoryId: Int): Single<Dapps> {
        return DirectoryService
                .get()
                .getAllDappsInCategory(categoryId)
                .map { it.results }
                .subscribeOn(Schedulers.io())
    }

    fun getDapp(dappId: Long): Single<DappResult> {
        return DirectoryService
                .get()
                .getDapp(dappId)
                .subscribeOn(Schedulers.io())
    }
}