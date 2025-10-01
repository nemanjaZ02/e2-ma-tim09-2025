package com.e2_ma_tim09_2025.questify.models;

public class AllianceConflictResult {
    private boolean canAccept;
    private boolean needsConfirmation;
    private boolean success;
    private String reason;
    private Alliance currentAlliance;
    private String newAllianceId;
    private String inviteId;

    public AllianceConflictResult() {}

    public boolean isCanAccept() {
        return canAccept;
    }

    public void setCanAccept(boolean canAccept) {
        this.canAccept = canAccept;
    }

    public boolean isNeedsConfirmation() {
        return needsConfirmation;
    }

    public void setNeedsConfirmation(boolean needsConfirmation) {
        this.needsConfirmation = needsConfirmation;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Alliance getCurrentAlliance() {
        return currentAlliance;
    }

    public void setCurrentAlliance(Alliance currentAlliance) {
        this.currentAlliance = currentAlliance;
    }

    public String getNewAllianceId() {
        return newAllianceId;
    }

    public void setNewAllianceId(String newAllianceId) {
        this.newAllianceId = newAllianceId;
    }

    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }
}
