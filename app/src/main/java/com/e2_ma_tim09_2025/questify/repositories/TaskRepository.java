package com.e2_ma_tim09_2025.questify.repositories;

import android.util.Log;

import com.e2_ma_tim09_2025.questify.dao.TaskCategoryDao;
import com.e2_ma_tim09_2025.questify.dao.TaskDao;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRepository {

    private static final String TAG = "TaskRepository";

    private final TaskDao taskDao;
    private final TaskCategoryDao categoryDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public TaskRepository(TaskDao taskDao, TaskCategoryDao categoryDao) {
        this.taskDao = taskDao;
        this.categoryDao = categoryDao;
    }

    public void insertCategory(TaskCategory category) {
        executor.execute(() -> {
            try {
                categoryDao.insertCategory(category);
                Log.d(TAG, "Category inserted successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error inserting category: " + e.getMessage());
            }
        });
    }

    public void insertTask(Task task) {
        executor.execute(() -> {
            if (task == null) {
                Log.e(TAG, "Error: Task object is null.");
                return;
            }

            if (task.getName() == null || task.getName().trim().isEmpty()) {
                Log.e(TAG, "Error: Task name cannot be empty.");
                return;
            }

            if (task.getCategoryId() <= 0) {
                Log.e(TAG, "Error: Invalid category ID.");
                return;
            }

            try {
                taskDao.insertTask(task);
                Log.d(TAG, "Task '" + task.getName() + "' inserted successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error inserting task: " + e.getMessage());
            }
        });
    }

    public List<Task> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public List<TaskCategory> getAllCategories() {
        return categoryDao.getAllCategories();
    }
}