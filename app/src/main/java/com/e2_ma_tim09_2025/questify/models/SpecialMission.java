package com.e2_ma_tim09_2025.questify.models;

import com.e2_ma_tim09_2025.questify.models.enums.SpecialMissionStatus;

public class SpecialMission {
    private String allianceId; // 1-1 sa alliance, nema svoj ID
    private SpecialMissionStatus status;
    private long startTime;
    private long endTime; // 2 nedelje od startTime
    private int totalDamageDealt;
    private boolean rewardsDistributed;

    public SpecialMission() {}

    public SpecialMission(String allianceId) {
        this.allianceId = allianceId;
        this.status = SpecialMissionStatus.ACTIVE;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (14 * 24 * 60 * 60 * 1000L); // 2 nedelje
        this.totalDamageDealt = 0;
        this.rewardsDistributed = false;
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

    public boolean isExpired() {
        return System.currentTimeMillis() > endTime;
    }

    public long getTimeRemaining() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public void addDamage(int damage) {
        this.totalDamageDealt += damage;
    }
}
