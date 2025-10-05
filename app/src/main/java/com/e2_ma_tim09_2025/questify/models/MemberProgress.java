package com.e2_ma_tim09_2025.questify.models;

public class MemberProgress {
    private String userId;
    private String username;
    private int completedTasks;
    private int totalDamage;
    private int maxTasks;
    
    public MemberProgress() {}
    
    public MemberProgress(String userId, String username, int completedTasks, int totalDamage, int maxTasks) {
        this.userId = userId;
        this.username = username;
        this.completedTasks = completedTasks;
        this.totalDamage = totalDamage;
        this.maxTasks = maxTasks;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getCompletedTasks() {
        return completedTasks;
    }
    
    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }
    
    public int getTotalDamage() {
        return totalDamage;
    }
    
    public void setTotalDamage(int totalDamage) {
        this.totalDamage = totalDamage;
    }
    
    public int getMaxTasks() {
        return maxTasks;
    }
    
    public void setMaxTasks(int maxTasks) {
        this.maxTasks = maxTasks;
    }
    
    public double getProgressPercentage() {
        if (maxTasks == 0) return 0.0;
        return Math.min(100.0, (double) completedTasks / maxTasks * 100.0);
    }
}
