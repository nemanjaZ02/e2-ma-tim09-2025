package com.e2_ma_tim09_2025.questify.repositories;

import androidx.lifecycle.LiveData;
import com.e2_ma_tim09_2025.questify.dao.TaskDao;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class TaskRepository {

    private final TaskDao taskDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public TaskRepository(TaskDao taskDao) {
        this.taskDao = taskDao;
    }
    public LiveData<List<Task>> getAll() {
        return taskDao.getAll();
    }
    public void insert(Task task) {
        executor.execute(() -> taskDao.insert(task));
    }
    public long insertAndReturnId(Task task) {
        return taskDao.insert(task);
    }
    public void delete(Task task) {
        executor.execute(() -> taskDao.delete(task));
    }

    public void update(Task task) {
        executor.execute(() -> taskDao.update(task));
    }
    public LiveData<Task> getById(int taskId) { return taskDao.getById(taskId);}
    public List<Task> getActiveTasks() {
        return taskDao.getActiveTasks();
    }
    public void complete(Task task) {
        executor.execute(() -> {
            task.setStatus(TaskStatus.COMPLETED);
            task.setLastInteractionAt(System.currentTimeMillis());
            task.setCompletedAt(System.currentTimeMillis());
            task.setXp(task.getXp());
            taskDao.update(task);
        });
    }
    public void cancel(Task task) {
        executor.execute(() -> {
            task.setStatus(TaskStatus.CANCELLED);
            task.setLastInteractionAt(System.currentTimeMillis());
            taskDao.update(task);
        });
    }
    public void pause(Task task, long remainingTime) {
        executor.execute(() -> {
            task.setStatus(TaskStatus.PAUSED);
            task.setRemainingTime(remainingTime);
            task.setLastInteractionAt(System.currentTimeMillis());
            taskDao.update(task);
        });
    }
    public void unpause(Task task, long newFinishDate) {
        executor.execute(() -> {
            task.setStatus(TaskStatus.ACTIVE);
            task.setFinishDate(newFinishDate);
            task.setLastInteractionAt(System.currentTimeMillis());
            taskDao.update(task);
        });
    }

    public List<Task> getRecurringTasks() {
        return taskDao.getRecurringTasks();
    }

    public Task getTaskByIdSync(int taskId) {
        return taskDao.getTaskByIdSync(taskId);
    }

    public LiveData<List<Task>> getTaskInstances(int originalTaskId) {
        return taskDao.getTaskInstances(originalTaskId);
    }

    public List<Task> getTasksByUser(String userId){
        return taskDao.getTasksByUser(userId);
    }

    public LiveData<List<Task>> getTasksByUserLiveData(String userId){
        return taskDao.getTasksByUserLiveData(userId);
    }

    public int countTasksByDifficultyToday(String userId, TaskDifficulty difficulty) {
        return taskDao.countTasksByDifficultyToday(userId, difficulty);
    }

    public int countTasksByDifficultyThisWeek(String userId, TaskDifficulty difficulty) {
        return taskDao.countTasksByDifficultyThisWeek(userId, difficulty);
    }

    public int countTasksByDifficultyThisMonth(String userId, TaskDifficulty difficulty) {
        return taskDao.countTasksByDifficultyThisMonth(userId, difficulty);
    }

    public int countTasksByPriorityToday(String userId, TaskPriority priority) {
        return taskDao.countTasksByPriorityToday(userId, priority);
    }

    public int countTasksByPriorityThisWeek(String userId, TaskPriority priority) {
        return taskDao.countTasksByPriorityThisWeek(userId, priority);
    }

    public int countTasksByPriorityThisMonth(String userId, TaskPriority priority) {
        return taskDao.countTasksByPriorityThisMonth(userId, priority);
    }

    public int countCompletedTasksByLevel(String userId, int level) {
        return taskDao.countCompletedTasksByLevel(userId, level);
    }

    public int countCreatedTasksInLevel(String userId, int level) {
        return taskDao.countCreatedTasksInLevel(userId, level);
    }

    public int countCompletedTasksCreatedBeforeLevel(String userId, int level) {
        return taskDao.countCompletedTasksCreatedBeforeLevel(userId, level);
    }
}