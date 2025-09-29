package com.e2_ma_tim09_2025.questify.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import com.e2_ma_tim09_2025.questify.models.Task;

@Dao
public interface TaskDao {
    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks")
    LiveData<List<Task>> getAll();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getById(int taskId);

    @Query("SELECT * FROM tasks WHERE status = 'ACTIVE'")
    List<Task> getActiveTasks();

    // Ove 3 su za recurring taskove ispod
    @Query("SELECT * FROM tasks WHERE recurrence IS NOT NULL")
    List<Task> getRecurringTasks();

    @Query("SELECT * FROM tasks WHERE originalTaskId = :originalTaskId")
    LiveData<List<Task>> getTaskInstances(int originalTaskId);

    // WorkManager ne radi sa LiveData
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskByIdSync(int taskId);

    @Query("SELECT * FROM tasks WHERE userId = :userId")
    List<Task> getTasksByUser(String userId);

    @Query("SELECT * FROM tasks WHERE userId = :userId")
    LiveData<List<Task>> getTasksByUserLiveData(String userId);
}