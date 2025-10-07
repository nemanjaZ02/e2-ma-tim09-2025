package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.e2_ma_tim09_2025.questify.dtos.TaskCategoryStatsDto;
import com.e2_ma_tim09_2025.questify.dtos.TaskStatusStatsDto;
import com.e2_ma_tim09_2025.questify.dtos.TaskStreakStatsDto;
import com.e2_ma_tim09_2025.questify.models.TaskDifficultyStatsDto;
import com.e2_ma_tim09_2025.questify.services.UserStatisticsService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private final UserStatisticsService userStatisticsService;
    
    private final MutableLiveData<Integer> activeDaysStreak = new MutableLiveData<>();
    private final MutableLiveData<TaskStatusStatsDto> taskStatusStats = new MutableLiveData<>();
    private final MutableLiveData<TaskStreakStatsDto> longestTaskStreak = new MutableLiveData<>();
    private final MutableLiveData<List<TaskCategoryStatsDto>> completedTasksPerCategory = new MutableLiveData<>();
    private final MutableLiveData<List<TaskDifficultyStatsDto>> averageDifficultyXP = new MutableLiveData<>();
    private final MutableLiveData<List<TaskDifficultyStatsDto>> xpLast7Days = new MutableLiveData<>();
    private final MutableLiveData<UserStatisticsService.MissionStatsDto> missionStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public StatisticsViewModel(UserStatisticsService userStatisticsService) {
        System.out.println("DEBUG: StatisticsViewModel constructor called");
        System.out.println("DEBUG: userStatisticsService is null: " + (userStatisticsService == null));
        this.userStatisticsService = userStatisticsService;
    }

    public LiveData<Integer> getActiveDaysStreak() {
        return activeDaysStreak;
    }

    public LiveData<TaskStatusStatsDto> getTaskStatusStats() {
        return taskStatusStats;
    }

    public LiveData<TaskStreakStatsDto> getLongestTaskStreak() {
        return longestTaskStreak;
    }

    public LiveData<List<TaskCategoryStatsDto>> getCompletedTasksPerCategory() {
        return completedTasksPerCategory;
    }

    public LiveData<List<TaskDifficultyStatsDto>> getAverageDifficultyXP() {
        return averageDifficultyXP;
    }

    public LiveData<List<TaskDifficultyStatsDto>> getXpLast7Days() {
        return xpLast7Days;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<UserStatisticsService.MissionStatsDto> getMissionStats() {
        return missionStats;
    }

    public void loadAllStatistics() {
        System.out.println("DEBUG: StatisticsViewModel.loadAllStatistics() called");
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.out.println("DEBUG: No current user found");
            return;
        }

        String userId = currentUser.getUid();
        System.out.println("DEBUG: Loading statistics for userId: " + userId);
        isLoading.setValue(true);

        // Load all statistics on background thread
        executor.execute(() -> {
            try {
                System.out.println("DEBUG: Starting statistics loading on background thread");
                
                // Load all statistics
                loadActiveDaysStreak(userId);
                loadTaskStatusStats(userId);
                loadLongestTaskStreak(userId);
                loadCompletedTasksPerCategory(userId);
                loadAverageDifficultyXP(userId);
                loadXpLast7Days(userId);
                loadMissionStats(userId);

                System.out.println("DEBUG: Statistics loading completed on background thread");
            } catch (Exception e) {
                System.out.println("DEBUG: Exception in background statistics loading: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    private void loadActiveDaysStreak(String userId) {
        System.out.println("DEBUG: loadActiveDaysStreak called for userId: " + userId);
        try {
            System.out.println("DEBUG: About to call userStatisticsService.calculateActiveDaysStreak");
            int streak = userStatisticsService.calculateActiveDaysStreak(userId);
            System.out.println("DEBUG: calculateActiveDaysStreak returned: " + streak);
            activeDaysStreak.postValue(streak);
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in loadActiveDaysStreak: " + e.getMessage());
            e.printStackTrace();
            activeDaysStreak.postValue(0);
        }
    }

    private void loadTaskStatusStats(String userId) {
        try {
            TaskStatusStatsDto stats = userStatisticsService.calculateTaskStatusStats(userId);
            taskStatusStats.postValue(stats);
        } catch (Exception e) {
            taskStatusStats.postValue(new TaskStatusStatsDto(0, 0, 0, 0));
        }
    }

    private void loadLongestTaskStreak(String userId) {
        try {
            TaskStreakStatsDto streak = userStatisticsService.calculateLongestTaskStreak(userId);
            longestTaskStreak.postValue(streak);
        } catch (Exception e) {
            longestTaskStreak.postValue(new TaskStreakStatsDto(0));
        }
    }

    private void loadCompletedTasksPerCategory(String userId) {
        try {
            List<TaskCategoryStatsDto> stats = userStatisticsService.getCompletedTasksPerCategory(userId);
            completedTasksPerCategory.postValue(stats);
        } catch (Exception e) {
            completedTasksPerCategory.postValue(java.util.Collections.emptyList());
        }
    }

    private void loadAverageDifficultyXP(String userId) {
        try {
            List<TaskDifficultyStatsDto> stats = userStatisticsService.getAverageDifficultyXPPerDay(userId);
            averageDifficultyXP.postValue(stats);
        } catch (Exception e) {
            averageDifficultyXP.postValue(java.util.Collections.emptyList());
        }
    }

    private void loadXpLast7Days(String userId) {
        try {
            List<TaskDifficultyStatsDto> stats = userStatisticsService.getXPLast7Days(userId);
            xpLast7Days.postValue(stats);
        } catch (Exception e) {
            xpLast7Days.postValue(java.util.Collections.emptyList());
        }
    }
    
    private void loadMissionStats(String userId) {
        System.out.println("DEBUG: loadMissionStats called for userId: " + userId);
        try {
            System.out.println("DEBUG: About to call userStatisticsService.getMissionStatistics");
            userStatisticsService.getMissionStatistics(userId, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    UserStatisticsService.MissionStatsDto stats = task.getResult();
                    System.out.println("DEBUG: Mission stats loaded successfully: " + 
                        "Started=" + stats.getStartedMissions() + 
                        ", Finished=" + stats.getFinishedMissions() + 
                        ", SuccessRate=" + stats.getSuccessRate() + "%");
                    missionStats.postValue(stats);
                } else {
                    System.out.println("DEBUG: Failed to load mission stats: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    missionStats.postValue(null);
                }
            });
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in loadMissionStats: " + e.getMessage());
            e.printStackTrace();
            missionStats.postValue(null);
        }
    }
}
