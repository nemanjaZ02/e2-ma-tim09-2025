package com.e2_ma_tim09_2025.questify.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;

@Dao
public interface TaskCategoryDao {
    @Insert
    void insert(TaskCategory category);
    @Update
    void update(TaskCategory category);
    @Delete
    void delete(TaskCategory category);
    @Query("SELECT * FROM taskCategories")
    LiveData<List<TaskCategory>> getAll();
    @Query("SELECT * FROM taskCategories")
    List<TaskCategory> getAll2();
    @Query("SELECT * FROM taskCategories WHERE id = :categoryId")
    LiveData<TaskCategory> getById(int categoryId);

    @Query("SELECT * FROM taskCategories WHERE userId = :userId")
    List<TaskCategory> getTaskCategoriesByUser(String userId);

    @Query("SELECT * FROM taskCategories WHERE userId = :userId")
    LiveData<List<TaskCategory>> getTaskCategoriesByUserLiveData(String userId);
}