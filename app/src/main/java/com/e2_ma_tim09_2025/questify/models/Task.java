package com.e2_ma_tim09_2025.questify.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;

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
    private long createdAt;
    private long finishDate;
    private long remainingTime;
    @Ignore
    private int userId;
    private TaskStatus status;

    public Task(String name, int categoryId, String description, TaskDifficulty difficulty,
                TaskPriority priority, TaskRecurrence recurrence, long createdAt, long finishDate, long remainingTime, TaskStatus status) {
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        this.difficulty = difficulty;
        this.priority = priority;
        this.recurrence = recurrence;
        this.createdAt = createdAt;
        this.finishDate = finishDate;
        this.status = status;
        this.remainingTime = remainingTime;
    }

    public TaskDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(TaskDifficulty difficulty) {
        this.difficulty = difficulty;
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public long getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(long finishDate) {
        this.finishDate = finishDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
    }
}