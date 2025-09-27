package com.e2_ma_tim09_2025.questify.dtos;

public class TaskCategoryStatsDto {
    private String categoryName;
    private int completedTasks;

    public TaskCategoryStatsDto(String categoryName, int completedTasks) {
        this.categoryName = categoryName;
        this.completedTasks = completedTasks;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
}
