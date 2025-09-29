package com.e2_ma_tim09_2025.questify.models;

import java.util.ArrayList;
import java.util.List;

public class Alliance {
    private String id;
    private String name;
    private String leaderId;
    private List<String> memberIds;
    private boolean missionStarted;

    public Alliance() {}

    public Alliance(String id, String name, String leaderId) {
        this.id = id;
        this.name = name;
        this.leaderId = leaderId;
        this.memberIds = new ArrayList<>();
        this.memberIds.add(leaderId);
        this.missionStarted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isMissionStarted() {
        return missionStarted;
    }

    public void setMissionStarted(boolean missionStarted) {
        this.missionStarted = missionStarted;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
