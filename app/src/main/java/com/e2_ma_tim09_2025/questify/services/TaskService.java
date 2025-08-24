package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.repositories.TaskCategoryRepository;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskService {

    private static final String TAG = "TaskService";
    private final TaskRepository taskRepository;
    private final TaskCategoryRepository categoryRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public TaskService(TaskRepository taskRepository, TaskCategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskRepository.getAll();
    }

    public LiveData<List<TaskCategory>> getAllCategories() {
        return categoryRepository.getAll();
    }

    public void insertTask(Task task) {
        executor.execute(() -> {
            // Logika provere poslovnih pravila:
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
                taskRepository.insert(task);
                Log.d(TAG, "Task '" + task.getName() + "' inserted successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error inserting task: " + e.getMessage());
            }
        });
    }
}