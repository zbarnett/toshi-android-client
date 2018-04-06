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

import com.toshi.manager.network.DirectoryInterface
import com.toshi.manager.network.DirectoryService
import com.toshi.model.network.dapp.DappResult
import com.toshi.model.network.dapp.DappSearchResult
import com.toshi.model.network.dapp.DappSections
import rx.Scheduler
import rx.Single
import rx.schedulers.Schedulers

class DappManager(
        private val directoryService: DirectoryInterface = DirectoryService.get(),
        private val scheduler: Scheduler = Schedulers.io()
) {

    fun getFrontPageDapps(): Single<DappSections> {
        return directoryService
                .getFrontpageDapps()
                .subscribeOn(scheduler)
    }

    fun search(input: String): Single<DappSearchResult> {
        return directoryService
                .search(input)
                .subscribeOn(scheduler)
    }

    fun getAllDapps(): Single<DappSearchResult> {
        return directoryService
                .getAllDapps()
                .subscribeOn(scheduler)
    }

    fun getAllDappsWithOffset(offset: Int): Single<DappSearchResult> {
        return directoryService
                .getAllDappsWithOffset(offset, 20)
                .subscribeOn(scheduler)
    }

    fun getAllDappsInCategoryWithOffset(categoryId: Int, offset: Int): Single<DappSearchResult> {
        return directoryService
                .getAllDappsInCategory(categoryId, offset, 20)
                .subscribeOn(scheduler)
    }

    fun getDapp(dappId: Long): Single<DappResult> {
        return directoryService
                .getDapp(dappId)
                .subscribeOn(scheduler)
    }
}