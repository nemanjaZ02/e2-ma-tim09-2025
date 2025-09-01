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
            taskDao.update(task);
        });
    }
    public void cancel(Task task) {
        executor.execute(() -> {
            task.setStatus(TaskStatus.CANCELLED);
            taskDao.update(task);
        });
    }
    public void pause(Task task, long remainingTime) {
        executor.execute(() -> {
            task.setStatus(TaskStatus.PAUSED);
            task.setRemainingTime(remainingTime);
            taskDao.update(task);
        });
    }
    public void unpause(Task task, long newFinishDate) {
        executor.execute(() -> {
            task.setStatus(TaskStatus.ACTIVE);
            task.setFinishDate(newFinishDate);
            taskDao.update(task);
        });
    }
}