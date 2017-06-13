/*
 * 	Copyright (c) 2017. Token Browser, Inc
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

package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityGroupParticipantsBinding;
import com.tokenbrowser.presenter.GroupParticipantsPresenter;
import com.tokenbrowser.presenter.LoaderIds;
import com.tokenbrowser.presenter.factory.GroupParticipantsPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public class GroupParticipantsActivity extends BasePresenterActivity<GroupParticipantsPresenter, GroupParticipantsActivity> {

    private ActivityGroupParticipantsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_group_participants);
    }

    public ActivityGroupParticipantsBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<GroupParticipantsPresenter> getPresenterFactory() {
        return new GroupParticipantsPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull GroupParticipantsPresenter presenter) {}

    @Override
    protected int loaderId() {
        return LoaderIds.get(this.getClass().getCanonicalName());
    }
}
