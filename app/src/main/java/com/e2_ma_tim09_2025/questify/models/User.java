package com.e2_ma_tim09_2025.questify.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String id;
    private String username;
    private String avatar;
    private int level;
    private String title;
    private int powerPoints;
    private int experiencePoints;
    private int coins;
    private List<String> badges;
    private List<MyEquipment> equipment;
    private String qrCode;
    private long createdAt;
    private List<String> friends;
    private String allianceId;
    private List<String> fcmTokens;

    public User() {
        friends = new ArrayList<>();
        fcmTokens = new ArrayList<>();
    }

    public User(String id, String username, String avatar, int level, String title,
                int powerPoints, int experiencePoints, int coins,
                List<String> badges, List<MyEquipment> equipment, String qrCode, long createdAt,
                ArrayList<String> friends, String allianceId) {
        this.id = id;
        this.username = username;
        this.avatar = avatar;
        this.level = level;
        this.title = title;
        this.powerPoints = powerPoints;
        this.experiencePoints = experiencePoints;
        this.coins = coins;
        this.badges = badges;
        this.equipment = equipment;
        this.qrCode = qrCode;
        this.createdAt = createdAt;
        this.friends = friends;
        this.allianceId = allianceId;
        this.fcmTokens = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPowerPoints() { return powerPoints; }
    public void setPowerPoints(int powerPoints) { this.powerPoints = powerPoints; }

    public int getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(int experiencePoints) { this.experiencePoints = experiencePoints; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }

    public List<MyEquipment> getEquipment() { return equipment; }
    public void setEquipment(List<MyEquipment> equipment) { this.equipment = equipment; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public long getCreatedAt () {return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public List<String> getFcmTokens() {
        return fcmTokens;
    }

    public void setFcmTokens(List<String> fcmTokens) {
        this.fcmTokens = fcmTokens;
    }
}
