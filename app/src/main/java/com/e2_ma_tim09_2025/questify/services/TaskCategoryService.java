package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.repositories.TaskCategoryRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class TaskCategoryService {

    private static final String TAG = "TaskCategoryService";
    private final TaskCategoryRepository categoryRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public TaskCategoryService(TaskCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public LiveData<List<TaskCategory>> getAllCategories() {
        return categoryRepository.getAll();
    }

    public LiveData<TaskCategory> getCategoryById(int categoryId) {
        return categoryRepository.getById(categoryId);
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
