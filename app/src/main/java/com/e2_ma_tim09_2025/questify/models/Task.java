package com.e2_ma_tim09_2025.questify.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;

@Entity(tableName = "tasks",
    foreignKeys = @ForeignKey(
            entity = TaskCategory.class,
            parentColumns = "id",
            childColumns = "category_id",
            onDelete = ForeignKey.CASCADE
    ), indices = {@Index(value = {"category_id"})})
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    @ColumnInfo(name = "category_id")
    private int categoryId;
    private String description;
    private TaskDifficulty difficulty;
    private TaskPriority priority;
    private TaskRecurrence recurrence;
    private long remainingTime;
    @Ignore
    private int userId;
    private boolean isDone;

    public Task(String name, int categoryId, String description, TaskDifficulty difficulty,
                TaskPriority priority, TaskRecurrence recurrence, long remainingTime, boolean isDone) {
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        this.difficulty = difficulty;
        this.priority = priority;
        this.recurrence = recurrence;
        this.remainingTime = remainingTime;
        this.isDone = isDone;
    }

    public TaskDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(TaskDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
    }

    public TaskRecurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(TaskRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}