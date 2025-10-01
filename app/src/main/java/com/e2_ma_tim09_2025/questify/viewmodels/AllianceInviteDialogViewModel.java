package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.services.AllianceInviteService;
import com.e2_ma_tim09_2025.questify.services.UserService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AllianceInviteDialogViewModel extends ViewModel {
    
    private final AllianceInviteService allianceInviteService;
    private final UserService userService;
    
    @Inject
    public AllianceInviteDialogViewModel(AllianceInviteService allianceInviteService, UserService userService) {
        this.allianceInviteService = allianceInviteService;
        this.userService = userService;
    }
    
    public AllianceInviteService getAllianceInviteService() {
        return allianceInviteService;
    }
    
    public UserService getUserService() {
        return userService;
    }
}
