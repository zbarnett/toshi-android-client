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

package com.toshi.model.local.dapp

import android.support.v7.util.DiffUtil
import com.toshi.model.network.dapp.Dapp

class DappDiffUtil(
        private val newDapps: List<Dapp>,
        private val oldDapps: List<Dapp>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldDapps.size

    override fun getNewListSize() = newDapps.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDapps[oldItemPosition].dappId === newDapps[newItemPosition].dappId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDapps[oldItemPosition].dappId === newDapps[newItemPosition].dappId
    }
}