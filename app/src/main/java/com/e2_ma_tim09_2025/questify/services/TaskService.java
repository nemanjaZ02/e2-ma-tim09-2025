package com.e2_ma_tim09_2025.questify.services;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;
import com.e2_ma_tim09_2025.questify.repositories.TaskCategoryRepository;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class TaskService {

    private static final String TAG = "TaskService";
    private final TaskRepository taskRepository;
    private final TaskCategoryRepository categoryRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable statusCheckRunnable;

    @Inject
    public TaskService(TaskRepository taskRepository, TaskCategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        statusCheckRunnable = new Runnable() {
            @Override
            public void run() {
                executor.execute(() -> {
                    List<Task> activeTasks = taskRepository.getActiveTasks();
                    long currentTime = System.currentTimeMillis();
                    for (Task task : activeTasks) {
                        if (task.getFinishDate() < currentTime) {
                            task.setStatus(TaskStatus.NOT_COMPLETED);
                            taskRepository.update(task);
                            Log.d("TaskService", "Task " + task.getName() + " expired. Status updated to NOT_COMPLETED.");
                        }
                    }
                });
                handler.postDelayed(this, 60_000);
            }
        };
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskRepository.getAll();
    }

    public LiveData<List<TaskCategory>> getAllCategories() {
        return categoryRepository.getAll();
    }

    public void startStatusUpdater() {
        handler.post(statusCheckRunnable);
    }

    public void stopStatusUpdater() {
        handler.removeCallbacks(statusCheckRunnable);
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
                taskRepository.insert(task);
                Log.d(TAG, "Task '" + task.getName() + "' inserted successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error inserting task: " + e.getMessage());
            }
        });
    }

    public void insertCategory(TaskCategory category) {
        executor.execute(() -> {
            if (category == null) {
                Log.e(TAG, "Error: Category object is null.");
                return;
            }
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                Log.e(TAG, "Error: Category name cannot be empty.");
                return;
            }

            try {
                categoryRepository.insert(category);
                Log.d(TAG, "Category '" + category.getName() + "' inserted successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error inserting category: " + e.getMessage());
            }
        });
    }
}