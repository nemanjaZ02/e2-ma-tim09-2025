package com.e2_ma_tim09_2025.questify.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.WorkManager;

import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskRecurrence;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.hilt.android.EntryPointAccessors;

import java.util.concurrent.TimeUnit;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@HiltWorker
public class RecurringTaskWorker extends Worker {

    public static final String KEY_TASK_ID = "task_id";
    private static final String TAG = "RecurringTaskWorker";

    private TaskRepository taskRepository;

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface RecurringTaskWorkerEntryPoint {
        TaskRepository getTaskRepository();
    }

    public RecurringTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        try {
            RecurringTaskWorkerEntryPoint entryPoint = EntryPointAccessors.fromApplication(
                    context.getApplicationContext(),
                    RecurringTaskWorkerEntryPoint.class
            );
            this.taskRepository = entryPoint.getTaskRepository();
        } catch (Exception e) {
            Log.e(TAG, "Failed to inject TaskRepository: " + e.getMessage());
            this.taskRepository = null;
        }
    }

    @AssistedInject
    public RecurringTaskWorker(@Assisted @NonNull Context context,
                               @Assisted @NonNull WorkerParameters workerParams,
                               TaskRepository taskRepository) {
        super(context, workerParams);
        this.taskRepository = taskRepository;
    }

    @AssistedFactory
    public interface Factory {
        RecurringTaskWorker create(Context context, WorkerParameters workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker started for recurring task.");

        if (taskRepository == null) {
            Log.e(TAG, "TaskRepository is null - cannot proceed");
            return Result.failure();
        }

        int taskId = getInputData().getInt(KEY_TASK_ID, -1);
        if (taskId == -1) {
            Log.e(TAG, "Invalid task ID received.");
            return Result.failure();
        }

        Task originalTask = taskRepository.getTaskByIdSync(taskId);
        if (originalTask == null) {
            Log.e(TAG, "Original task with ID " + taskId + " not found. May have been deleted.");
            return Result.failure();
        }

        long newCreatedAt = System.currentTimeMillis();
        long originalDuration = originalTask.getFinishDate() - originalTask.getCreatedAt();
        long newFinishDate = newCreatedAt + originalDuration;

        Task newTaskInstance = new Task(
                originalTask.getName(),
                originalTask.getCategoryId(),
                originalTask.getDescription(),
                originalTask.getDifficulty(),
                originalTask.getPriority(),
                originalTask.getRecurrence(),
                newCreatedAt,
                newFinishDate,
                originalTask.getRemainingTime(),
                TaskStatus.ACTIVE,
                originalTask.getLastInteractionAt(),
                originalTask.getXp(),
                originalTask.getCompletedAt(),
                originalTask.getUserId(),
                originalTask.getLevelWhenCreated(),
                -1
        );

        newTaskInstance.setOriginalTaskId(originalTask.getId());

        long newTaskId = taskRepository.insertAndReturnId(newTaskInstance);
        Task newTask = taskRepository.getTaskByIdSync((int)newTaskId);
        Log.d(TAG, "New task instance created for original task ID: " + originalTask.getId() + ". New task ID: " + newTask.getId());

        scheduleNextRecurringTask(newTask);

        return Result.success();
    }

    private void scheduleNextRecurringTask(Task task) {
        TaskRecurrence recurrence = task.getRecurrence();
        if (recurrence == null) {
            return;
        }

        Task rootTask = taskRepository.getTaskByIdSync(task.getOriginalTaskId());
        if (rootTask == null || rootTask.getStatus() == TaskStatus.PAUSED) {
            Log.d(TAG, "Skipping scheduling because original task is PAUSED or missing.");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long nextOccurrenceTime = calculateNextOccurrenceTime(task);

        if (nextOccurrenceTime > recurrence.getEndDate()) {
            Log.d(TAG, "Recurrence end date reached. No further tasks will be scheduled.");
            return;
        }

        if (nextOccurrenceTime > currentTime) {
            long delay = nextOccurrenceTime - currentTime;

            Data inputData = new Data.Builder()
                    .putInt(KEY_TASK_ID, task.getOriginalTaskId())
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(RecurringTaskWorker.class)
                    .setInputData(inputData)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            String workName = "recurring_task_" + task.getId();
            WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
            );

            Log.d(TAG, "Next recurring task scheduled to run in " + TimeUnit.MILLISECONDS.toSeconds(delay) + " seconds.");
        }
    }

    private long calculateNextOccurrenceTime(Task task) {
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

        long nextTime = System.currentTimeMillis() + intervalMillis;
        return nextTime;
    }
}