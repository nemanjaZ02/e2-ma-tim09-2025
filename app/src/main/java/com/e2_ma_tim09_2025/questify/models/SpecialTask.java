package com.e2_ma_tim09_2025.questify.models;

import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskStatus;

public class SpecialTask {
    private String id;
    private String userId;
    private String specialMissionId; // ID specijalne misije
    private String allianceId; // Za svaki slucaj
    private int missionNumber; // Broj misije (1, 2, 3...)
    private SpecialTaskType taskType;
    private SpecialTaskStatus status;
    private int currentCount; // Trenutni napredak
    private int maxCount; // Maksimalan broj izvrsenja
    private int damagePerCompletion; // HP damage po izvrsenju
    private long lastCompletedAt; // Poslednji put izvrsen
    private int notCompletedTasksBeforeActivation; // Broj NOT_COMPLETED taskova pre aktivacije (za NO_UNRESOLVED_TASKS)

    public SpecialTask() {}

    public SpecialTask(String userId, String specialMissionId, String allianceId, int missionNumber, SpecialTaskType taskType) {
        this.userId = userId;
        this.specialMissionId = specialMissionId;
        this.allianceId = allianceId;
        this.missionNumber = missionNumber;
        this.taskType = taskType;
        this.status = SpecialTaskStatus.ACTIVE;
        this.currentCount = 0;
        this.lastCompletedAt = 0;
        this.notCompletedTasksBeforeActivation = 0;

        switch (taskType) {
            case SHOP_PURCHASE:
                this.maxCount = 5;
                this.damagePerCompletion = 2;
                break;
            case BOSS_ATTACK:
                this.maxCount = 10;
                this.damagePerCompletion = 2;
                break;
            case TASK_COMPLETION_EASY_NORMAL:
                this.maxCount = 10;
                this.damagePerCompletion = 1;
                break;
            case TASK_COMPLETION_OTHER:
                this.maxCount = 6;
                this.damagePerCompletion = 4;
                break;
            case NO_UNRESOLVED_TASKS:
                this.maxCount = 1;
                this.damagePerCompletion = 10;
                break;
            case ALLIANCE_MESSAGE_DAILY:
                this.maxCount = 14; 
                this.damagePerCompletion = 4;
                break;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSpecialMissionId() {
        return specialMissionId;
    }

    public void setSpecialMissionId(String specialMissionId) {
        this.specialMissionId = specialMissionId;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public SpecialTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(SpecialTaskType taskType) {
        this.taskType = taskType;
    }

    public SpecialTaskStatus getStatus() {
        return status;
    }

    public void setStatus(SpecialTaskStatus status) {
        this.status = status;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(int currentCount) {
        this.currentCount = currentCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getDamagePerCompletion() {
        return damagePerCompletion;
    }

    public void setDamagePerCompletion(int damagePerCompletion) {
        this.damagePerCompletion = damagePerCompletion;
    }

    public long getLastCompletedAt() {
        return lastCompletedAt;
    }

    public void setLastCompletedAt(long lastCompletedAt) {
        this.lastCompletedAt = lastCompletedAt;
    }

    // Helper methods
    public boolean isCompleted() {
        return currentCount >= maxCount;
    }

    public int getRemainingCount() {
        return Math.max(0, maxCount - currentCount);
    }

    public double getProgressPercentage() {
        return (double) currentCount / maxCount * 100;
    }

    public boolean canComplete() {
        return !isCompleted() && status == SpecialTaskStatus.ACTIVE;
    }

    public void complete() {
        if (canComplete()) {
            this.currentCount++;
            this.lastCompletedAt = System.currentTimeMillis();
            
            if (isCompleted()) {
                this.status = SpecialTaskStatus.COMPLETED;
            }
        }
    }

    public int getTotalDamageDealt() {
        return currentCount * damagePerCompletion;
    }
    
    public int getMissionNumber() {
        return missionNumber;
    }
    
    public void setMissionNumber(int missionNumber) {
        this.missionNumber = missionNumber;
    }
    
    public int getNotCompletedTasksBeforeActivation() {
        return notCompletedTasksBeforeActivation;
    }
    
    public void setNotCompletedTasksBeforeActivation(int notCompletedTasksBeforeActivation) {
        this.notCompletedTasksBeforeActivation = notCompletedTasksBeforeActivation;
    }
}
