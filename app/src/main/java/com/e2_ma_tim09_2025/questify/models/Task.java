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
        ), indices = {
        @Index(value = {"category_id"}),
        @Index(value = {"originalTaskId"})
})
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
    private String userId;
    private TaskStatus status;
    private Integer originalTaskId;
    private long lastInteractionAt;
    private long completedAt;
    private int xp; //koliko ukupno xp je donio zadatak nakon rjesavanja, uzimajuci u obzir bitnost, tezinu i nivo korisnika
    private int levelWhenCreated;
    private int levelWhenCompleted;
    private boolean isQuotaExceeded;

    public Task(String name, int categoryId, String description, TaskDifficulty difficulty,
                TaskPriority priority, TaskRecurrence recurrence, long createdAt, long finishDate, long remainingTime, TaskStatus status, long lastInteractionAt,
                int xp, long completedAt, String userId,  int levelWhenCreated, int levelWhenCompleted) {
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
        this.originalTaskId = null;
        this.lastInteractionAt = lastInteractionAt;//DODATI OVO I ISPOD U OVAJ STO JE IGNORE NISAM BILA SIGURNA
        this.xp = xp;
        this.completedAt = completedAt;
        this.userId = userId;
        this.levelWhenCreated = levelWhenCreated;
        this.levelWhenCompleted = levelWhenCompleted;
        this.isQuotaExceeded = false;
    }

    @Ignore
    public Task(int id, String name, int categoryId, String description, TaskDifficulty difficulty, TaskPriority priority,
                TaskRecurrence recurrence, long createdAt, long finishDate, long remainingTime, String userId, TaskStatus status,
                Integer originalTaskId, int xp, long completedAt, int levelWhenCreated, int levelWhenCompleted) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        this.difficulty = difficulty;
        this.priority = priority;
        this.recurrence = recurrence;
        this.createdAt = createdAt;
        this.finishDate = finishDate;
        this.remainingTime = remainingTime;
        this.userId = userId;
        this.status = status;
        this.originalTaskId = originalTaskId;
        this.xp = xp;
        this.completedAt = completedAt;
        this.levelWhenCreated = levelWhenCreated;
        this.levelWhenCompleted = levelWhenCompleted;
        this.isQuotaExceeded = false;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public Integer getOriginalTaskId() {
        return originalTaskId;
    }

    public void setOriginalTaskId(Integer originalTaskId) {
        this.originalTaskId = originalTaskId;
    }

    public long getLastInteractionAt() {
        return lastInteractionAt;
    }
    public void setLastInteractionAt(long lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }
    public int getXp() {
        return xp;
    }
    public void setXp(int xp) {
        this.xp = xp;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public int getLevelWhenCreated() {
        return levelWhenCreated;
    }

    public void setLevelWhenCreated(int levelWhenCreated) {
        this.levelWhenCreated = levelWhenCreated;
    }

    public int getLevelWhenCompleted() {
        return levelWhenCompleted;
    }

    public void setLevelWhenCompleted(int levelWhenCompleted) {
        this.levelWhenCompleted = levelWhenCompleted;
    }

    public boolean getIsQuotaExceeded() {
        return isQuotaExceeded;
    }

    public void setIsQuotaExceeded(boolean isQuotaExceeded) {
        this.isQuotaExceeded = isQuotaExceeded;
    }
}