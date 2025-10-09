package com.e2_ma_tim09_2025.questify.models;

public class MyEquipment {

    private String id;
    private String equipmentId;
    private int leftAmount;//pratim za koliko bitki mi je ostalo
    private int timesUpgraded;
    private boolean isActivated = false;
    private int timesGottenAgain; // ovo mi je za ono da ispratim ako sam dobila vise puta isto oruzje

    public MyEquipment() {}

    public MyEquipment(String id, String equipmentId, int timesUpgraded, int leftAmount, int timesGottenAgain) {
        this.id = id;
        this.timesUpgraded = timesUpgraded;
        this.leftAmount = leftAmount;
        this.equipmentId = equipmentId;
        this.timesGottenAgain = timesGottenAgain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTimesUpgraded() {
        return timesUpgraded;
    }

    public void setTimesUpgraded(int timesUpgraded) {
        this.timesUpgraded = timesUpgraded;
    }

    public int getLeftAmount() {
        return leftAmount;
    }

    public void setLeftAmount(int leftAmount) {
        this.leftAmount = leftAmount;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public int getTimesGottenAgain() {
        return timesGottenAgain;
    }

    public void setTimesGottenAgain(int timesGottenAgain) {
        this.timesGottenAgain = timesGottenAgain;
    }
}
