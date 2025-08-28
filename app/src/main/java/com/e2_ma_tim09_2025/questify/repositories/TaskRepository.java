package com.e2_ma_tim09_2025.questify.repositories;

import androidx.lifecycle.LiveData;
import com.e2_ma_tim09_2025.questify.dao.TaskDao;
import com.e2_ma_tim09_2025.questify.models.Task;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    public List<Task> getActiveTasks() {
        return taskDao.getActiveTasks();
    }
}