package com.e2_ma_tim09_2025.questify.repositories;

import androidx.lifecycle.LiveData;
import com.e2_ma_tim09_2025.questify.dao.TaskCategoryDao;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class TaskCategoryRepository {

    private final TaskCategoryDao categoryDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public TaskCategoryRepository(TaskCategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public LiveData<List<TaskCategory>> getAll() {
        return categoryDao.getAll();
    }
    public List<TaskCategory> getAll2() {
        return categoryDao.getAll2();
    }

    public void insert(TaskCategory category) {
        executor.execute(() -> categoryDao.insert(category));
    }

    public void update(TaskCategory category) {
        executor.execute(() -> categoryDao.update(category));
    }

    public void delete(TaskCategory category) {
        executor.execute(() -> categoryDao.delete(category));
    }

    public LiveData<TaskCategory> getById(int categoryId) {
        return categoryDao.getById(categoryId);
    }

    public List<TaskCategory> getTaskCategoriesByUser(String userId) {
        return categoryDao.getTaskCategoriesByUser(userId);
    }

    public LiveData<List<TaskCategory>> getTaskCategoriesByUserLiveData(String userId) {
        return categoryDao.getTaskCategoriesByUserLiveData(userId);
    }
}