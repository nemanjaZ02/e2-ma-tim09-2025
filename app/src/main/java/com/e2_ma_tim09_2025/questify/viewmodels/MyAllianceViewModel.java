package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.AllianceService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MyAllianceViewModel extends ViewModel {
    
    private final AllianceService allianceService;
    private final UserService userService;
    
    private final MutableLiveData<List<Alliance>> alliancesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> membersLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> eligibleUsersLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> invitationSentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> allianceDeletedLiveData = new MutableLiveData<>();
    
    @Inject
    public MyAllianceViewModel(AllianceService allianceService, UserService userService) {
        this.allianceService = allianceService;
        this.userService = userService;
    }
    
    public LiveData<List<Alliance>> getAlliances() {
        return alliancesLiveData;
    }
    
    public LiveData<List<User>> getMembers() {
        return membersLiveData;
    }
    
    public LiveData<List<User>> getEligibleUsers() {
        return eligibleUsersLiveData;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<Boolean> getInvitationSent() {
        return invitationSentLiveData;
    }
    
    public LiveData<Boolean> getAllianceDeleted() {
        return allianceDeletedLiveData;
    }
    
    public void loadAlliances() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        isLoadingLiveData.postValue(true);
        allianceService.getAlliancesByLeader(currentUserId, new OnCompleteListener<List<Alliance>>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<List<Alliance>> task) {
                isLoadingLiveData.postValue(false);
                if (task.isSuccessful()) {
                    alliancesLiveData.postValue(task.getResult());
                } else {
                    errorMessageLiveData.postValue("Failed to load alliances: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            }
        });
    }
    
    public void loadAllianceMembers(String allianceId) {
        isLoadingLiveData.postValue(true);
        allianceService.getAllianceMembers(allianceId, new OnCompleteListener<List<User>>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<List<User>> task) {
                isLoadingLiveData.postValue(false);
                if (task.isSuccessful()) {
                    membersLiveData.postValue(task.getResult());
                } else {
                    errorMessageLiveData.postValue("Failed to load members: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            }
        });
    }
    
    public void loadEligibleUsers(String allianceId) {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        isLoadingLiveData.postValue(true);
        allianceService.getEligibleUsersForInvitation(allianceId, currentUserId, new OnCompleteListener<List<User>>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<List<User>> task) {
                isLoadingLiveData.postValue(false);
                if (task.isSuccessful()) {
                    eligibleUsersLiveData.postValue(task.getResult());
                } else {
                    errorMessageLiveData.postValue("Failed to load eligible users: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            }
        });
    }
    
    public void sendInvitation(String allianceId, String toUserId) {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        isLoadingLiveData.postValue(true);
        allianceService.sendAllianceInvitation(allianceId, currentUserId, toUserId, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<Void> task) {
                isLoadingLiveData.postValue(false);
                if (task.isSuccessful()) {
                    invitationSentLiveData.postValue(true);
                    // Reload eligible users to remove the invited user
                    loadEligibleUsers(allianceId);
                } else {
                    errorMessageLiveData.postValue("Failed to send invitation: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            }
        });
    }
    
    public void clearError() {
        errorMessageLiveData.postValue(null);
    }
    
    public void clearInvitationSent() {
        invitationSentLiveData.postValue(false);
    }
    
    public void deleteAlliance(String allianceId) {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        isLoadingLiveData.postValue(true);
        allianceService.deleteAlliance(allianceId, currentUserId, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<Void> task) {
                isLoadingLiveData.postValue(false);
                if (task.isSuccessful()) {
                    allianceDeletedLiveData.postValue(true);
                    // Clear the current alliance data
                    alliancesLiveData.postValue(new ArrayList<>());
                    membersLiveData.postValue(new ArrayList<>());
                } else {
                    errorMessageLiveData.postValue("Failed to delete alliance: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            }
        });
    }
    
    public void clearAllianceDeleted() {
        allianceDeletedLiveData.postValue(false);
    }
}
