package com.e2_ma_tim09_2025.questify.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;

@Dao
public interface TaskCategoryDao {
    @Insert
    void insert(TaskCategory category);
    @Query("SELECT * FROM taskCategories")
    LiveData<List<TaskCategory>> getAll();
    @Query("SELECT * FROM taskCategories WHERE id = :categoryId")
    LiveData<TaskCategory> getById(int categoryId);
}