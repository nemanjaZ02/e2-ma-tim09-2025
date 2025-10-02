package com.e2_ma_tim09_2025.questify.models;

import com.e2_ma_tim09_2025.questify.models.enums.EquipmentType;

public class Equipment {

    private String id;
    private String name;
    private EquipmentType type;
    private boolean isUpgradable;
    private double price;
    private int lasting; // 1-jednokratna 2-dvokratna 3-trajna
    private String refersTo; //na sta utice ta oprema da li na xp/pp itd
    private double referingAmount; //koliko povecava nesto 5%

    public Equipment() {} // Default constructor for Firebase

    public Equipment(String id, double referingAmount, String refersTo, int lasting, double price, boolean isUpgradable, EquipmentType type, String name) {
        this.id = id;
        this.referingAmount = referingAmount;
        this.refersTo = refersTo;
        this.lasting = lasting;
        this.price = price;
        this.isUpgradable = isUpgradable;
        this.type = type;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getReferingAmount() {
        return referingAmount;
    }

    public void setReferingAmount(double referingAmount) {
        this.referingAmount = referingAmount;
    }

    public String getRefersTo() {
        return refersTo;
    }

    public void setRefersTo(String refersTo) {
        this.refersTo = refersTo;
    }

    public int getLasting() {
        return lasting;
    }

    public void setLasting(int lasting) {
        this.lasting = lasting;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isUpgradable() {
        return isUpgradable;
    }

    public void setUpgradable(boolean upgradable) {
        isUpgradable = upgradable;
    }

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
