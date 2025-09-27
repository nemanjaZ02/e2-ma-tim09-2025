package com.e2_ma_tim09_2025.questify.models;

public class TaskDifficultyStatsDto {
    private long date; // use timestamp for X-axis
    private int xp;    // XP earned on that day/task

    public TaskDifficultyStatsDto(long date, int xp) {
        this.date = date;
        this.xp = xp;
    }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
}
