package com.e2_ma_tim09_2025.questify.models;

import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;

public class Boss {
    private BossStatus status;
    private String userId;
    private int currentHealth;
    private int maxHealth;
    private int coinsDrop;
    private int attacksLeft;
    private double hitChance;

    public Boss(BossStatus status, String userId, int currentHealth, int maxHealth, int coinsDrop) {
        this.status = status;
        this.userId = userId;
        this.currentHealth = currentHealth;
        this.maxHealth = maxHealth;
        this.coinsDrop = coinsDrop;
        this.attacksLeft = 5;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BossStatus getStatus() {
        return status;
    }

    public void setStatus(BossStatus status) {
        this.status = status;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getCoinsDrop() {
        return coinsDrop;
    }

    public void setCoinsDrop(int coinsDrop) {
        this.coinsDrop = coinsDrop;
    }

    public int getAttacksLeft() {
        return attacksLeft;
    }

    public void setAttacksLeft(int attacksLeft) {
        this.attacksLeft = attacksLeft;
    }
}
