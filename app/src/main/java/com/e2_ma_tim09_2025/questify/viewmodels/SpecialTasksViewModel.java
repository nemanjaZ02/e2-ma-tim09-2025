package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.SpecialTask;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.services.AllianceService;
import com.e2_ma_tim09_2025.questify.services.BossService;
import com.e2_ma_tim09_2025.questify.services.SpecialMissionService;
import com.e2_ma_tim09_2025.questify.services.SpecialTaskService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SpecialTasksViewModel extends ViewModel {
    
    private final SpecialTaskService specialTaskService;
    private final UserService userService;
    private final BossService bossService;
    private final AllianceService allianceService;
    private final SpecialMissionService specialMissionService;
    
    private final MutableLiveData<List<SpecialTask>> specialTasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<SpecialTask>> myAllianceTasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<SpecialTask>> memberAllianceTasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> currentAllianceIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<SpecialMission> specialMissionLiveData = new MutableLiveData<>();
    private final MutableLiveData<SpecialMission> myAllianceSpecialMissionLiveData = new MutableLiveData<>();
    private final MutableLiveData<SpecialMission> memberAllianceSpecialMissionLiveData = new MutableLiveData<>();
    private final MutableLiveData<Alliance> currentAlliance = new MutableLiveData<>();
    private final LiveData<Boss> boss;
    
    @Inject
    public SpecialTasksViewModel(SpecialTaskService specialTaskService, UserService userService, BossService bossService, AllianceService allianceService, SpecialMissionService specialMissionService) {
        this.specialTaskService = specialTaskService;
        this.userService = userService;
        this.bossService = bossService;
        this.allianceService = allianceService;
        this.specialMissionService = specialMissionService;
        
        fetchCurrentUser();
        
        MediatorLiveData<Boss> bossMediator = new MediatorLiveData<>();
        this.boss = bossMediator;
        
        currentUser.observeForever(user -> {
            if (user != null) {
                LiveData<Boss> userBoss = bossService.getBoss(user.getId());
                bossMediator.addSource(userBoss, bossVal -> {
                    bossMediator.setValue(bossVal);
                });
            }
        });
    }
    
    public LiveData<List<SpecialTask>> getSpecialTasks() {
        return specialTasksLiveData;
    }
    
    public LiveData<List<SpecialTask>> getMyAllianceTasks() {
        return myAllianceTasksLiveData;
    }
    
    public LiveData<List<SpecialTask>> getMemberAllianceTasks() {
        return memberAllianceTasksLiveData;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    
    public LiveData<String> getCurrentAllianceId() {
        return currentAllianceIdLiveData;
    }
    
    public LiveData<Boolean> isBossActive() {
        return Transformations.map(boss, bossVal -> {
            if (bossVal == null) return false;
            return bossVal.getStatus() == com.e2_ma_tim09_2025.questify.models.enums.BossStatus.ACTIVE;
        });
    }
    
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    
    public LiveData<SpecialMission> getSpecialMission() {
        return specialMissionLiveData;
    }
    
    public LiveData<SpecialMission> getMyAllianceSpecialMission() {
        return myAllianceSpecialMissionLiveData;
    }
    
    public LiveData<SpecialMission> getMemberAllianceSpecialMission() {
        return memberAllianceSpecialMissionLiveData;
    }
    
    public LiveData<Alliance> getCurrentAlliance() {
        return currentAlliance;
    }
    
    public void fetchCurrentUser() {
        String uid = userService.getCurrentUserId();
        if (uid != null) {
            userService.getUser(uid, task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    User user = task.getResult().toObject(User.class);
                    currentUser.postValue(user);
                } else {
                    currentUser.postValue(null);
                }
            });
        } else {
            currentUser.postValue(null);
        }
    }
    
    public void loadSpecialTasks() {
        User user = currentUser.getValue();
        if (user == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        // Load user's alliance and special tasks
        loadUserAllianceAndTasks(user.getId());
    }
    
    private void loadUserAllianceAndTasks(String userId) {
        isLoadingLiveData.postValue(true);
        
        // Get user's alliance using AllianceService
        allianceService.getUserAlliance(userId, new OnCompleteListener<Alliance>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<Alliance> allianceTask) {
                if (allianceTask.isSuccessful() && allianceTask.getResult() != null) {
                    Alliance alliance = allianceTask.getResult();
                    String allianceId = alliance.getId();
                    System.out.println("DEBUG: Found alliance for user: " + allianceId);
                    
                    // User is in an alliance
                    currentAllianceIdLiveData.postValue(allianceId);
                    currentAlliance.postValue(alliance); // Postavi alliance za boss status
                    System.out.println("DEBUG: Loading special tasks for userId=" + userId + ", allianceId=" + allianceId);
                    
                    // First check if mission is expired or completed
                    specialMissionService.checkAndCompleteMission(allianceId, checkTask -> {
                        // Load special mission
                        specialMissionService.getSpecialMission(allianceId, missionTask -> {
                            if (missionTask.isSuccessful() && missionTask.getResult() != null) {
                                specialMissionLiveData.postValue(missionTask.getResult());
                            }
                        });
                        
                        // Load special tasks for this alliance with real-time listener
                        specialTaskService.listenToUserSpecialTasks(userId, allianceId, new OnCompleteListener<List<SpecialTask>>() {
                        @Override
                        public void onComplete(com.google.android.gms.tasks.Task<List<SpecialTask>> specialTaskTask) {
                            isLoadingLiveData.postValue(false);
                            if (specialTaskTask.isSuccessful()) {
                                List<SpecialTask> tasks = specialTaskTask.getResult();
                                System.out.println("DEBUG: Loaded " + (tasks != null ? tasks.size() : 0) + " special tasks");
                                specialTasksLiveData.postValue(tasks);
                            } else {
                                String errorMsg = "Failed to load special tasks: " + 
                                    (specialTaskTask.getException() != null ? specialTaskTask.getException().getMessage() : "Unknown error");
                                System.out.println("DEBUG: " + errorMsg);
                                errorMessageLiveData.postValue(errorMsg);
                                specialTasksLiveData.postValue(new ArrayList<>());
                            }
                        }
                        });
                    });
                } else {
                    // User is not in any alliance
                    System.out.println("DEBUG: User is not in any alliance");
                    isLoadingLiveData.postValue(false);
                    specialTasksLiveData.postValue(new ArrayList<>());
                    currentAllianceIdLiveData.postValue(null);
                    currentAlliance.postValue(null); // Reset alliance
                }
            }
        });
    }
    
    public void loadSpecialTasksForAlliance(String allianceId) {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        if (allianceId == null || allianceId.isEmpty()) {
            specialTasksLiveData.postValue(new ArrayList<>());
            return;
        }
        
        isLoadingLiveData.postValue(true);
        currentAllianceIdLiveData.postValue(allianceId);
        
        specialTaskService.listenToUserSpecialTasks(currentUserId, allianceId, new OnCompleteListener<List<SpecialTask>>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<List<SpecialTask>> task) {
                isLoadingLiveData.postValue(false);
                if (task.isSuccessful()) {
                    specialTasksLiveData.postValue(task.getResult());
                } else {
                    errorMessageLiveData.postValue("Failed to load special tasks: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    specialTasksLiveData.postValue(new ArrayList<>());
                }
            }
        });
    }
    
    public void loadAllSpecialTasks() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        isLoadingLiveData.postValue(true);
        
        // Load My Alliance tasks (where user is leader)
        allianceService.getAlliancesByLeader(currentUserId, new OnCompleteListener<List<Alliance>>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<List<Alliance>> task) {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    // User has an alliance they lead
                    Alliance myAlliance = task.getResult().get(0);
                    loadTasksForAlliance(currentUserId, myAlliance.getId(), myAllianceTasksLiveData);
                    // Load special mission for My Alliance
                    loadMyAllianceSpecialMission(myAlliance.getId());
                } else {
                    // No alliance where user is leader
                    myAllianceTasksLiveData.postValue(new ArrayList<>());
                }
            }
        });
        
        // Load Member Alliance tasks (where user is member but not leader)
        allianceService.getUserMemberAlliance(currentUserId, new OnCompleteListener<Alliance>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<Alliance> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    // User is a member of an alliance
                    Alliance memberAlliance = task.getResult();
                    loadTasksForAlliance(currentUserId, memberAlliance.getId(), memberAllianceTasksLiveData);
                    // Load special mission for Member Alliance
                    loadMemberAllianceSpecialMission(memberAlliance.getId());
                } else {
                    // No alliance where user is member
                    memberAllianceTasksLiveData.postValue(new ArrayList<>());
                }
                isLoadingLiveData.postValue(false);
            }
        });
    }
    
    private void loadTasksForAlliance(String userId, String allianceId, MutableLiveData<List<SpecialTask>> targetLiveData) {
        specialTaskService.listenToUserSpecialTasks(userId, allianceId, new OnCompleteListener<List<SpecialTask>>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<List<SpecialTask>> task) {
                if (task.isSuccessful()) {
                    targetLiveData.postValue(task.getResult());
                } else {
                    targetLiveData.postValue(new ArrayList<>());
                }
            }
        });
    }
    
    private void loadSpecialMissionForAlliance(String allianceId) {
        specialMissionService.getSpecialMission(allianceId, new OnCompleteListener<SpecialMission>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<SpecialMission> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    specialMissionLiveData.postValue(task.getResult());
                }
            }
        });
    }
    
    private void loadMyAllianceSpecialMission(String allianceId) {
        specialMissionService.getSpecialMission(allianceId, new OnCompleteListener<SpecialMission>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<SpecialMission> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    myAllianceSpecialMissionLiveData.postValue(task.getResult());
                }
            }
        });
    }
    
    private void loadMemberAllianceSpecialMission(String allianceId) {
        specialMissionService.getSpecialMission(allianceId, new OnCompleteListener<SpecialMission>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<SpecialMission> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    memberAllianceSpecialMissionLiveData.postValue(task.getResult());
                }
            }
        });
    }
    
    public void clearError() {
        errorMessageLiveData.postValue(null);
    }
    
    public void logout() {
        userService.logout();
    }
}
