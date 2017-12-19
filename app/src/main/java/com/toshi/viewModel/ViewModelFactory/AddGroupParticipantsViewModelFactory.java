package com.toshi.viewModel.ViewModelFactory;

import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.toshi.viewModel.AddGroupParticipantsViewModel;

public class AddGroupParticipantsViewModelFactory implements ViewModelProvider.Factory {

    private String groupId;

    public AddGroupParticipantsViewModelFactory(final String groupId) {
        this.groupId = groupId;
    }

    @NonNull
    @Override
    public AddGroupParticipantsViewModel create(@NonNull Class modelClass) {
        return new AddGroupParticipantsViewModel(this.groupId);
    }
}
