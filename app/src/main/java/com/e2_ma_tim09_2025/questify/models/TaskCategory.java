package com.e2_ma_tim09_2025.questify.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "taskCategories")
public class TaskCategory {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String userId;
    private String description;
    private int color;

    public TaskCategory(String name, String description, int color, String userId) {
        this.name = name;
        this.userId = userId;
        this.description = description;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}