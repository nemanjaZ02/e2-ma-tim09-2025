package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.AllianceService;
import com.e2_ma_tim09_2025.questify.services.SpecialMissionService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MemberAllianceViewModel extends ViewModel {
    
    private final AllianceService allianceService;
    private final UserService userService;
    private final SpecialMissionService specialMissionService;
    
    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> membersLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> leaderLiveData = new MutableLiveData<>();
    private final MutableLiveData<SpecialMission> specialMissionLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    
    @Inject
    public MemberAllianceViewModel(AllianceService allianceService, UserService userService, SpecialMissionService specialMissionService) {
        this.allianceService = allianceService;
        this.userService = userService;
        this.specialMissionService = specialMissionService;
    }
    
    public LiveData<Alliance> getAlliance() {
        return allianceLiveData;
    }
    
    public LiveData<List<User>> getMembers() {
        return membersLiveData;
    }
    
    public LiveData<User> getLeader() {
        return leaderLiveData;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<SpecialMission> getSpecialMission() {
        return specialMissionLiveData;
    }
    
    public void loadUserAlliance() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        isLoadingLiveData.postValue(true);
        
        // Use AllianceService.getUserMemberAlliance which finds alliance where user is a member but NOT a leader
        allianceService.getUserMemberAlliance(currentUserId, allianceTask -> {
            if (!allianceTask.isSuccessful() || allianceTask.getResult() == null) {
                isLoadingLiveData.postValue(false);
                errorMessageLiveData.postValue("You are not a member of any alliance");
                return;
            }
            
            Alliance alliance = allianceTask.getResult();
            if (alliance != null) {
                allianceLiveData.postValue(alliance);
                
                // Load alliance members
                allianceService.getAllianceMembers(alliance.getId(), membersTask -> {
                    if (membersTask.isSuccessful()) {
                        membersLiveData.postValue(membersTask.getResult());
                    } else {
                        errorMessageLiveData.postValue("Failed to load alliance members");
                    }
                });
                
                // Load alliance leader
                userService.getUser(alliance.getLeaderId(), leaderTask -> {
                    if (leaderTask.isSuccessful() && leaderTask.getResult() != null && leaderTask.getResult().exists()) {
                        User leader = leaderTask.getResult().toObject(User.class);
                        leaderLiveData.postValue(leader);
                    } else {
                        errorMessageLiveData.postValue("Failed to load alliance leader");
                    }
                });
            } else {
                errorMessageLiveData.postValue("Failed to parse alliance data");
            }
            
            isLoadingLiveData.postValue(false);
        });
    }
    
    public void clearError() {
        errorMessageLiveData.postValue(null);
    }
    
    public void loadSpecialMission(String allianceId) {
        specialMissionService.getSpecialMission(allianceId, new OnCompleteListener<SpecialMission>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<SpecialMission> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    specialMissionLiveData.postValue(task.getResult());
                } else {
                    specialMissionLiveData.postValue(null);
                }
            }
        });
    }
}
