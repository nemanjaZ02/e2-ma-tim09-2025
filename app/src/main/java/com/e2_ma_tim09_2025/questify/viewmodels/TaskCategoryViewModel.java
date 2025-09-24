package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.services.TaskCategoryService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TaskCategoryViewModel extends ViewModel {

    private final TaskCategoryService categoryService;
    private final LiveData<List<TaskCategory>> allCategories;

    @Inject
    public TaskCategoryViewModel(TaskCategoryService categoryService) {
        this.categoryService = categoryService;
        this.allCategories = categoryService.getAllCategories();
    }

    public LiveData<List<TaskCategory>> getAllCategories() {
        return allCategories;
    }

    public LiveData<TaskCategory> getCategoryById(int categoryId) {
        return categoryService.getCategoryById(categoryId);
    }

    public void insertCategory(TaskCategory category) {
        categoryService.insertCategory(category);
    }

    public void updateCategory(TaskCategory category) {
        categoryService.updateCategory(category);
    }

    public void deleteCategory(TaskCategory category) {
        categoryService.deleteCategory(category);
    }

    public LiveData<Boolean> isColorUsed(int color) {
        return Transformations.map(allCategories, categories -> {
            if (categories != null) {
                for (TaskCategory category : categories) {
                    if (category.getColor() == color) {
                        return true;
                    }
                }
            }
            return false;
        });
    }
}
