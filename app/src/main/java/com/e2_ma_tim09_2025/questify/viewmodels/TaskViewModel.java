package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.services.TaskService;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TaskViewModel extends ViewModel {

    private final TaskService taskService;
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<TaskCategory>> allCategories;

    @Inject
    public TaskViewModel(TaskService taskService) {
        this.taskService = taskService;
        this.allTasks = taskService.getAllTasks();
        this.allCategories = taskService.getAllCategories();
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
    public LiveData<Task> getTaskById(int taskId) {
        return taskService.getTaskById(taskId);
    }
    public LiveData<TaskCategory> getTaskCategoryById(int categoryId) {
        return taskService.getTaskCategoryById(categoryId);
    }
}