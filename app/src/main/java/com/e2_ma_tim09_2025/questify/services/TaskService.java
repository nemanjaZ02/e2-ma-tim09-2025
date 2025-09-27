package com.e2_ma_tim09_2025.questify.services;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingWorkPolicy;

import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.TaskRecurrence;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;
import com.e2_ma_tim09_2025.questify.repositories.TaskCategoryRepository;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.e2_ma_tim09_2025.questify.utils.RecurringTaskWorker;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class TaskService {

    private static final String TAG = "TaskService";
    private final TaskRepository taskRepository;
    private final TaskCategoryRepository categoryRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable statusCheckRunnable;
    private final WorkManager workManager; // DODANO
    private final UserService userService;
    private final UserRepository userRepository;



    @Inject
    public TaskService(@ApplicationContext Context context, TaskRepository taskRepository, TaskCategoryRepository categoryRepository, UserService userService,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.workManager = WorkManager.getInstance(context);
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

    public void completeTask(Task task) {
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
            if (task.getStatus() != TaskStatus.ACTIVE) {
                Log.e(TAG, "Error: You can complete only active tasks.");
                return;
            }

            String userId = userRepository.getCurrentUserId();

            try {
                // 1. Mark task as completed
                //PREBACILA SAM KOD ISPOD OVOGA!!!!!!!!!!!

                // 2. Fetch the user from UserRepository
                userRepository.getUser(userId, taskSnapshot -> {
                    if (taskSnapshot.isSuccessful() && taskSnapshot.getResult() != null && taskSnapshot.getResult().exists()) {
                        DocumentSnapshot document = taskSnapshot.getResult();

                        // Extract user's current level
                        int currentLevel = document.contains("level")
                                ? document.getLong("level").intValue()
                                : 0;

                        // Calculate XP based on task and user's current level
                        int xpFromImportance = userService.calculateXpForImportance(task.getPriority(), currentLevel);
                        int xpFromDifficulty = userService.calculateXpForDifficulty(task.getDifficulty(), currentLevel);
                        int totalXp = xpFromImportance + xpFromDifficulty;

                        task.setStatus(TaskStatus.COMPLETED);
                        task.setXp(totalXp);
                        taskRepository.complete(task);
                        Log.d(TAG, "Task '" + task.getName() + "' completed successfully.");


                        // 3. Award XP (UserService handles leveling, PP, title)
                        userService.addXP(userId, totalXp, addXpTask -> {
                            if (addXpTask.isSuccessful()) {
                                Log.d(TAG, "XP awarded successfully to user: " + userId);
                            } else {
                                Log.e(TAG, "Failed to add XP: " + addXpTask.getException());
                            }
                        });

                        Log.d(TAG, "Total XP awarded for task: " + totalXp);

                    } else {
                        Log.e(TAG, "Failed to fetch user for XP calculation");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error completing task: " + e.getMessage());
            }
        });
    }
    public void cancelTask(Task task) {
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
            if (task.getStatus() == TaskStatus.COMPLETED) {
                Log.e(TAG, "Error: You can't cancel completed task.'");
                return;
            }

            try {
                taskRepository.cancel(task);
                Log.d(TAG, "Task '" + task.getName() + "' completed successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error updating task: " + e.getMessage());
            }
        });
    }

    public void updateTask(Task task) {
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
            if (task.getStatus() != TaskStatus.ACTIVE) {
                Log.e(TAG, "Error: You can only edit active quests.");
                return;
            }

            try {
                taskRepository.update(task);
                Log.d(TAG, "Task '" + task.getName() + "' updated successfully.");
                if (task.getRecurrence() != null) {
                    scheduleNextRecurringTask(task);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating task: " + e.getMessage());
            }
        });
    }

    public void pauseTask(Task task) {
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
            if (task.getStatus() != TaskStatus.ACTIVE) {
                Log.e(TAG, "Error: Can't pause inactive task");
                return;
            }

            try {
                long remainingTime = task.getFinishDate() - System.currentTimeMillis();
                taskRepository.pause(task, (int)remainingTime);
                Log.d(TAG, "Task '" + task.getName() + "' paused successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error updating task: " + e.getMessage());
            }
        });
    }

    public void unpauseTask(Task task) {
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
            if (task.getStatus() != TaskStatus.PAUSED) {
                Log.e(TAG, "Error: You can only unpause paused task");
                return;
            }

            try {
                long newFinishDate = System.currentTimeMillis() + task.getRemainingTime();
                taskRepository.unpause(task, newFinishDate);
                Log.d(TAG, "Task '" + task.getName() + "' paused successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error updating task: " + e.getMessage());
            }
        });
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskRepository.getAll();
    }

    public LiveData<List<TaskCategory>> getAllCategories() {
        return categoryRepository.getAll();
    }
    public List<TaskCategory> getAllCategoriesSync() {
        return categoryRepository.getAll2();
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
                long taskId = taskRepository.insertAndReturnId(task);

                Log.d(TAG, "Task '" + task.getName() + "' inserted successfully with ID: " + taskId);

                if (task.getRecurrence() != null) {
                    scheduleNextRecurringTask(taskRepository.getTaskByIdSync((int)taskId));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting task: " + e.getMessage());
            }
        });
    }

    public void deleteTask(Task task) {
        executor.execute(() -> {
            if (task == null) {
                Log.e(TAG, "Error: Task object is null, cannot delete.");
                return;
            }
            try {
                taskRepository.delete(task);
                Log.d(TAG, "Task '" + task.getName() + "' deleted successfully.");
                if (task.getRecurrence() != null) {
                    workManager.cancelUniqueWork(getRecurringTaskWorkName(task.getId()));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting task: " + e.getMessage());
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

    public LiveData<Task> getTaskById(int taskId) {
        return taskRepository.getById(taskId);
    }

    public LiveData<TaskCategory> getTaskCategoryById(int categoryId) {
        return categoryRepository.getById(categoryId);
    }
    public List<Task> getTasksByUser(String userId) {
        return taskRepository.getTasksByUser(userId);
    }

    private void scheduleNextRecurringTask(Task task) {
        TaskRecurrence recurrence = task.getRecurrence();
        if (recurrence == null) return;

        long currentTime = System.currentTimeMillis();
        long nextOccurrenceTime = calculateNextOccurrenceTime(task);

        if (nextOccurrenceTime > currentTime) {
            long delay = nextOccurrenceTime - currentTime;

            Data inputData = new Data.Builder()
                    .putInt(RecurringTaskWorker.KEY_TASK_ID, task.getId())
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(RecurringTaskWorker.class)
                    .setInputData(inputData)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            String workName = getRecurringTaskWorkName(task.getId());
            workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, workRequest);
            Log.d(TAG, "Scheduled recurring task with ID: " + task.getId() + " to run in " + TimeUnit.MILLISECONDS.toSeconds(delay) + " seconds.");
        }
    }

    private String getRecurringTaskWorkName(int taskId) {
        return "recurring_task_" + taskId;
    }

    private long calculateNextOccurrenceTime(Task task) {
        long lastCompletionTime = task.getCreatedAt();
        TaskRecurrence recurrence = task.getRecurrence();
        if (recurrence == null) return 0;

        long intervalMillis = 0;
        switch (recurrence.getUnit()) {
            case MINUTE:
                intervalMillis = TimeUnit.MINUTES.toMillis(recurrence.getInterval());
                break;
            case DAY:
                intervalMillis = TimeUnit.DAYS.toMillis(recurrence.getInterval());
                break;
            case WEEK:
                intervalMillis = TimeUnit.DAYS.toMillis(recurrence.getInterval() * 7);
                break;
        }

        long nextTime = lastCompletionTime + intervalMillis;
        return nextTime;
    }
}