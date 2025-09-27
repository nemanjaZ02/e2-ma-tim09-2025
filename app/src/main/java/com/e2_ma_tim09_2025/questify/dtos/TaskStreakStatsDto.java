package com.e2_ma_tim09_2025.questify.dtos;

public class TaskStreakStatsDto {
    private int longestStreak;

    public TaskStreakStatsDto(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
}
