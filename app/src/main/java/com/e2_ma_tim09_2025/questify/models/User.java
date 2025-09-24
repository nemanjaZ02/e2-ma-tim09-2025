package com.e2_ma_tim09_2025.questify.models;

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
    private List<String> equipment;
    private String qrCode;
    private long createdAt;

    public User() {}

    public User(String id, String username, String avatar, int level, String title,
                int powerPoints, int experiencePoints, int coins,
                List<String> badges, List<String> equipment, String qrCode, long createdAt) {
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

    public List<String> getEquipment() { return equipment; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public long getCreatedAt () {return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
