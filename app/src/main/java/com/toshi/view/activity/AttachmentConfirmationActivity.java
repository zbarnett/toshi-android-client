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

package com.toshi.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.toshi.R;
import com.toshi.databinding.ActivityAttachmentConfirmationBinding;
import com.toshi.presenter.AttachmentConfirmationPresenter;
import com.toshi.presenter.LoaderIds;
import com.toshi.presenter.factory.AttachmentConfirmationPresenterFactory;
import com.toshi.presenter.factory.PresenterFactory;

public class AttachmentConfirmationActivity extends BasePresenterActivity<AttachmentConfirmationPresenter, AttachmentConfirmationActivity> {

    public static final String ATTACHMENT_URI = "attachment_uri";
    public static final String ATTACHMENT_PATH = "attachment_path";

    private ActivityAttachmentConfirmationBinding binding;
    private AttachmentConfirmationPresenter presenter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_attachment_confirmation);
    }

    public ActivityAttachmentConfirmationBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<AttachmentConfirmationPresenter> getPresenterFactory() {
        return new AttachmentConfirmationPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull AttachmentConfirmationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void onPresenterDestroyed() {
        this.presenter = null;
    }

    @Override
    protected int loaderId() {
        return LoaderIds.get(this.getClass().getCanonicalName());
    }

    @Override
    public void onBackPressed() {
        if (this.presenter == null) {
            super.onBackPressed();
            return;
        }

        this.presenter.handleBackButtonClicked();
    }
}
