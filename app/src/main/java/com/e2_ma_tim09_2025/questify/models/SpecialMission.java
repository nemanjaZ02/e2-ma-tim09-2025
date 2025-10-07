package com.e2_ma_tim09_2025.questify.models;

import com.e2_ma_tim09_2025.questify.models.enums.SpecialMissionStatus;

public class SpecialMission {
    private String allianceId; // 1-1 sa alliance, nema svoj ID
    private SpecialMissionStatus status;
    private long startTime;
    private long endTime; // 2 nedelje od startTime
    private int totalDamageDealt;
    private boolean rewardsDistributed;
    private SpecialBoss boss; // Boss je direktno u misiji
    private int missionNumber; // Broj misije (1, 2, 3...)

    public SpecialMission() {}

    public SpecialMission(String allianceId) {
        this.allianceId = allianceId;
        this.status = SpecialMissionStatus.INACTIVE; // Neaktivna na početku
        this.startTime = 0; // Nije pokrenuta
        this.endTime = 0; // Nije pokrenuta
        this.totalDamageDealt = 0;
        this.rewardsDistributed = false;
        this.missionNumber = 0; // Početni broj - nije pokrenuta
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public SpecialMissionStatus getStatus() {
        return status;
    }

    public void setStatus(SpecialMissionStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return status == SpecialMissionStatus.ACTIVE;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getTotalDamageDealt() {
        return totalDamageDealt;
    }

    public void setTotalDamageDealt(int totalDamageDealt) {
        this.totalDamageDealt = totalDamageDealt;
    }

    public boolean isRewardsDistributed() {
        return rewardsDistributed;
    }

    public void setRewardsDistributed(boolean rewardsDistributed) {
        this.rewardsDistributed = rewardsDistributed;
    }

    public long getTimeRemaining() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public void addDamage(int damage) {
        this.totalDamageDealt += damage;
    }
    
    // Boss getter i setter
    public SpecialBoss getBoss() {
        return boss;
    }
    
    public void setBoss(SpecialBoss boss) {
        this.boss = boss;
    }
    
    // Convenience metode za boss
    public boolean isBossDefeated() {
        return boss != null && boss.isDefeated();
    }
    
    public int getBossCurrentHealth() {
        return boss != null ? boss.getCurrentHealth() : 0;
    }
    
    public int getBossMaxHealth() {
        return boss != null ? boss.getMaxHealth() : 0;
    }
    
    public double getBossHealthPercentage() {
        return boss != null ? boss.getHealthPercentage() : 0.0;
    }
    
    public void dealDamageToBoss(int damage) {
        if (boss != null) {
            boss.takeDamage(damage);
        }
    }
    
    public int getMissionNumber() {
        return missionNumber;
    }
    
    public void setMissionNumber(int missionNumber) {
        this.missionNumber = missionNumber;
    }
}
