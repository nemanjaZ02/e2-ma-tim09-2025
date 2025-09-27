package com.e2_ma_tim09_2025.questify.dtos;

public class TaskStatusStatsDto {
    private int totalCreated;
    private int completed;
    private int notCompleted;
    private int canceled;

    public TaskStatusStatsDto(int totalCreated, int completed, int notCompleted, int canceled) {
        this.totalCreated = totalCreated;
        this.completed = completed;
        this.notCompleted = notCompleted;
        this.canceled = canceled;
    }

    // Getters and setters
    public int getTotalCreated() { return totalCreated; }
    public int getCompleted() { return completed; }
    public int getNotCompleted() { return notCompleted; }
    public int getCanceled() { return canceled; }

    public void setTotalCreated(int totalCreated) { this.totalCreated = totalCreated; }
    public void setCompleted(int completed) { this.completed = completed; }
    public void setNotCompleted(int notCompleted) { this.notCompleted = notCompleted; }
    public void setCanceled(int canceled) { this.canceled = canceled; }
}
