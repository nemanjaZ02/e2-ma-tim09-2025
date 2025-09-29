package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.TaskCategoryService;
import com.e2_ma_tim09_2025.questify.services.UserService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TaskCategoryViewModel extends ViewModel {

    private final TaskCategoryService categoryService;
    private final LiveData<List<TaskCategory>> allCategories;
    private final UserService userService;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    @Inject
    public TaskCategoryViewModel(TaskCategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;

        fetchCurrentUser();

        MediatorLiveData<List<TaskCategory>> categoriesMediator = new MediatorLiveData<>();
        this.allCategories = categoriesMediator;

        currentUser.observeForever(user -> {
            if (user != null) {
                LiveData<List<TaskCategory>> userTaskCategories = categoryService.getTaskCategoriesByUserLiveData(user.getId());
                categoriesMediator.addSource(userTaskCategories, tasks -> {
                    categoriesMediator.setValue(tasks);
                });
            }
        });
    }

    public void fetchCurrentUser() {
        String uid = userService.getCurrentUserId();
        if (uid != null) {
            userService.getUser(uid, task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    User user = task.getResult().toObject(User.class);
                    currentUser.postValue(user);
                } else {
                    currentUser.postValue(null);
                }
            });
        } else {
            currentUser.postValue(null);
        }
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUser;
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
