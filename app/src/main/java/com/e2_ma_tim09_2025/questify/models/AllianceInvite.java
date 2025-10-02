package com.e2_ma_tim09_2025.questify.models;

import com.e2_ma_tim09_2025.questify.models.enums.AllianceInviteStatus;

public class AllianceInvite {
    private String id;
    private String allianceId;
    private String fromUserId;
    private String toUserId;
    private boolean accepted;  // false by default
    private long timestamp;
    private AllianceInviteStatus status;

    public AllianceInvite() {}

    public AllianceInvite(String id, String allianceId, String fromUserId, String toUserId, AllianceInviteStatus status) {
        this.id = id;
        this.allianceId = allianceId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.accepted = false;
        this.timestamp = System.currentTimeMillis();
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public AllianceInviteStatus getStatus() {
        return status;
    }

    public void setStatus(AllianceInviteStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AllianceInvite{" +
                "id='" + id + '\'' +
                ", allianceId='" + allianceId + '\'' +
                ", fromUserId='" + fromUserId + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", accepted=" + accepted +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }
}
