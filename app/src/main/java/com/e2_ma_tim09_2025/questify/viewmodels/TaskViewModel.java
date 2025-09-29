package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.e2_ma_tim09_2025.questify.services.BossService;
import com.e2_ma_tim09_2025.questify.services.TaskCategoryService;
import com.e2_ma_tim09_2025.questify.services.TaskService;
import com.e2_ma_tim09_2025.questify.services.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TaskViewModel extends ViewModel {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskCategoryService categoryService;
    private final BossService bossService;
    private final LiveData<Boss> boss;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private LiveData<List<Task>> allTasks;
    private LiveData<List<TaskCategory>> allCategories;
    private final MutableLiveData<Set<String>> selectedCategoryIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Set<TaskDifficulty>> selectedDifficulties = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Set<TaskPriority>> selectedPriorities = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Boolean> isRecurringFilter = new MutableLiveData<>();
    private final MediatorLiveData<List<Task>> filteredTasks = new MediatorLiveData<>();

    @Inject
    public TaskViewModel(TaskService taskService, UserService userService, TaskCategoryService categoryService, BossService bossService) {
        this.taskService = taskService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.bossService = bossService;

        fetchCurrentUser();

        MediatorLiveData<List<Task>> tasksMediator = new MediatorLiveData<>();
        this.allTasks = tasksMediator;
        MediatorLiveData<List<TaskCategory>> categoriesMediator = new MediatorLiveData<>();
        this.allCategories = categoriesMediator;
        MediatorLiveData<Boss> bossMediator = new MediatorLiveData<>();
        this.boss = bossMediator;

        currentUser.observeForever(user -> {
            if (user != null) {
                LiveData<List<Task>> userTasks = taskService.getTasksByUserLiveData(user.getId());
                LiveData<List<TaskCategory>> userCategories = categoryService.getTaskCategoriesByUserLiveData(user.getId());
                LiveData<Boss> userBoss = bossService.getBoss(user.getId());
                tasksMediator.addSource(userTasks, tasks -> {
                    tasksMediator.setValue(tasks);
                    applyFilters();
                });
                categoriesMediator.addSource(userCategories, categories -> {
                    categoriesMediator.setValue(categories);
                });
                bossMediator.addSource(userBoss, bossVal -> {
                    bossMediator.setValue(bossVal);
                });
            }
        });

        filteredTasks.addSource(allTasks, tasks -> applyFilters());
        filteredTasks.addSource(selectedCategoryIds, newFilter -> applyFilters());
        filteredTasks.addSource(selectedDifficulties, newFilter -> applyFilters());
        filteredTasks.addSource(selectedPriorities, newFilter -> applyFilters());
        filteredTasks.addSource(isRecurringFilter, newFilter -> applyFilters());

        applyFilters();
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

    public LiveData<Boolean> isBossActive() {
        return Transformations.map(boss, bossVal -> {
            if (bossVal == null) return false;
            return bossVal.getStatus() == BossStatus.ACTIVE;
        });
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUser;
    }

    private void applyFilters() {
        List<Task> currentTasks = allTasks.getValue();
        if (currentTasks == null) {
            return;
        }

        Set<String> catIds = selectedCategoryIds.getValue();
        Set<TaskDifficulty> difficulties = selectedDifficulties.getValue();
        Set<TaskPriority> priorities = selectedPriorities.getValue();
        Boolean isRecurring = isRecurringFilter.getValue();

        List<Task> filteredList = currentTasks.stream()
                .filter(task -> {
                    boolean categoryMatches = catIds == null || catIds.isEmpty() || catIds.contains(String.valueOf(task.getCategoryId()));
                    boolean difficultyMatches = difficulties == null || difficulties.isEmpty() || difficulties.contains(task.getDifficulty());
                    boolean priorityMatches = priorities == null || priorities.isEmpty() || priorities.contains(task.getPriority());
                    boolean recurringMatches = isRecurring == null || (isRecurring == true && task.getRecurrence() != null) || (isRecurring == false && task.getRecurrence() == null);

                    return categoryMatches && difficultyMatches && priorityMatches && recurringMatches;
                })
                .collect(Collectors.toList());

        filteredTasks.setValue(filteredList);
    }
    public void addCategoryFilter(String categoryId) {
        Set<String> current = new HashSet<>(selectedCategoryIds.getValue());
        current.add(categoryId);
        selectedCategoryIds.setValue(current);
    }
    public void addDifficultyFilter(TaskDifficulty difficulty) {
        Set<TaskDifficulty> current = new HashSet<>(selectedDifficulties.getValue());
        current.add(difficulty);
        selectedDifficulties.setValue(current);
    }
    public void removeDifficultyFilter(TaskDifficulty difficulty) {
        Set<TaskDifficulty> current = new HashSet<>(selectedDifficulties.getValue());
        current.remove(difficulty);
        selectedDifficulties.setValue(current);
    }
    public void addPriorityFilter(TaskPriority priority) {
        Set<TaskPriority> current = new HashSet<>(selectedPriorities.getValue());
        current.add(priority);
        selectedPriorities.setValue(current);
    }
    public void removePriorityFilter(TaskPriority priority) {
        Set<TaskPriority> current = new HashSet<>(selectedPriorities.getValue());
        current.remove(priority);
        selectedPriorities.setValue(current);
    }
    public void setRecurringFilter(Boolean isRecurring) {
        isRecurringFilter.setValue(isRecurring);
    }
    public void removeCategoryFilter(String categoryId) {
        Set<String> current = new HashSet<>(selectedCategoryIds.getValue());
        current.remove(categoryId);
        selectedCategoryIds.setValue(current);
    }
    public void clearAllFilters() {
        selectedCategoryIds.setValue(new HashSet<>());
        selectedDifficulties.setValue(new HashSet<>());
        selectedPriorities.setValue(new HashSet<>());
        isRecurringFilter.setValue(null);
    }
    public LiveData<Set<String>> getSelectedCategoryIds() {
        return selectedCategoryIds;
    }
    public LiveData<Set<TaskDifficulty>> getSelectedDifficulties() {
        return selectedDifficulties;
    }
    public LiveData<Set<TaskPriority>> getSelectedPriorities() {
        return selectedPriorities;
    }
    public LiveData<Boolean> getIsRecurringFilter() {
        return isRecurringFilter;
    }
    public LiveData<List<Task>> getTasks() {
        return allTasks;
    }
    public void insertCategory(TaskCategory category) {
        taskService.insertCategory(category);
    }
    public LiveData<List<TaskCategory>> getCategories() {
        return allCategories;
    }
    public void insertTask(Task task) {
        taskService.insertTask(task);
    }
    public void startStatusUpdater() {
        taskService.startStatusUpdater();
    }
    public void stopStatusUpdater() {
        taskService.stopStatusUpdater();
    }
    public void updateTask(Task task) {
        taskService.updateTask(task);
    }
    public void deleteTask(Task task) {
        taskService.deleteTask(task);
    }
    public void completeTask(Task task) { taskService.completeTask(task); }
    public void cancelTask(Task task) { taskService.cancelTask(task); }
    public void pauseTask(Task task) {
        taskService.pauseTask(task);
    }
    public void unpauseTask(Task task) {
        taskService.unpauseTask(task);
    }
    public LiveData<Task> getTaskById(int taskId) {
        return taskService.getTaskById(taskId);
    }
    public LiveData<TaskCategory> getTaskCategoryById(int categoryId) {
        return taskService.getTaskCategoryById(categoryId);
    }
    public LiveData<List<Task>> getFilteredTasks() {
        return filteredTasks;
    }
}