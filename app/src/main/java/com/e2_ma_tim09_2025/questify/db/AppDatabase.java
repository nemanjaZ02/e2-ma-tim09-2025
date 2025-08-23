package com.e2_ma_tim09_2025.questify.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.e2_ma_tim09_2025.questify.dao.TaskCategoryDao;
import com.e2_ma_tim09_2025.questify.dao.TaskDao;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;

@Database(entities = {Task.class, TaskCategory.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskDao taskDao();
    public abstract TaskCategoryDao taskCategoryDao();
}