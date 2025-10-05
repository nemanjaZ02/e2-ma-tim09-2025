package com.e2_ma_tim09_2025.questify.models;

public class SpecialBoss {
    private String id;
    private String specialMissionId; // ID specijalne misije
    private String allianceId; // Za svaki slučaj
    private int currentHealth;
    private int maxHealth;
    private int coinsDrop; // 50% od sledećeg regularnog boss-a
    private boolean isDefeated;

    public SpecialBoss() {}

    public SpecialBoss(String specialMissionId, String allianceId, int memberCount) {
        this.specialMissionId = specialMissionId;
        this.allianceId = allianceId;
        this.maxHealth = 100 * memberCount; // HP = 100 * broj članova
        this.currentHealth = this.maxHealth;
        this.coinsDrop = 0; // Postaviće se kada se misija završi
        this.isDefeated = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpecialMissionId() {
        return specialMissionId;
    }

    public void setSpecialMissionId(String specialMissionId) {
        this.specialMissionId = specialMissionId;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
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

    public boolean isDefeated() {
        return isDefeated;
    }

    public void setDefeated(boolean defeated) {
        isDefeated = defeated;
    }

    // Helper methods
    public double getHealthPercentage() {
        return (double) currentHealth / maxHealth * 100;
    }

    public void takeDamage(int damage) {
        this.currentHealth = Math.max(0, this.currentHealth - damage);
        
        if (this.currentHealth <= 0) {
            this.isDefeated = true;
        }
    }
}
