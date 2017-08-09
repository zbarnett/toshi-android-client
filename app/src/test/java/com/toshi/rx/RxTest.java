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

package com.toshi.rx;


import org.junit.Test;

import rx.Observable;

public class RxTest {

    // When filtering you need to catch an error because the filter can return 0 items.
    @Test
    public void emptyFilterDoesNotThrowException() {
        Observable
                .just(1)
                .filter(val -> val > 1)
                .first()
                .toSingle()
                .onErrorReturn(t -> null)
                .toBlocking()
                .value();
    }
}
