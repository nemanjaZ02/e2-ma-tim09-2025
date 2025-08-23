package com.e2_ma_tim09_2025.questify.db;

import androidx.room.TypeConverter;
import com.e2_ma_tim09_2025.questify.models.TaskRecurrence;
import com.e2_ma_tim09_2025.questify.models.enums.RecurrenceUnit;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.google.gson.Gson;

public class Converters {
    @TypeConverter
    public static int fromTaskDifficulty(TaskDifficulty difficulty) {
        return difficulty == null ? 0 : difficulty.ordinal();
    }

    @TypeConverter
    public static TaskDifficulty toTaskDifficulty(int ordinal) {
        return TaskDifficulty.values()[ordinal];
    }

    @TypeConverter
    public static int fromTaskPriority(TaskPriority priority) {
        return priority == null ? 0 : priority.ordinal();
    }

    @TypeConverter
    public static TaskPriority toTaskPriority(int ordinal) {
        return TaskPriority.values()[ordinal];
    }

    @TypeConverter
    public static String fromTaskRecurrence(TaskRecurrence recurrence) {
        return new Gson().toJson(recurrence);
    }

    @TypeConverter
    public static TaskRecurrence toTaskRecurrence(String json) {
        return new Gson().fromJson(json, TaskRecurrence.class);
    }

    @TypeConverter
    public static int fromRecurrenceUnit(RecurrenceUnit unit) {
        return unit == null ? 0 : unit.ordinal();
    }

    @TypeConverter
    public static RecurrenceUnit toRecurrenceUnit(int ordinal) {
        return RecurrenceUnit.values()[ordinal];
    }
}