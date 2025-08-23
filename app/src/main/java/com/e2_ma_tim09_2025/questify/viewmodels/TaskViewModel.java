package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;

import java.util.List;

public class TaskViewModel extends ViewModel {

    private final TaskRepository repository;
    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<TaskCategory>> allCategories;

    public TaskViewModel(TaskRepository repository) {
        this.repository = repository;
        this.allTasks = repository.getAllTasks();
        this.allCategories = repository.getAllCategories();
    }

    public LiveData<List<Task>> getTasks() {
        return allTasks;
    }

    public LiveData<List<TaskCategory>> getCategories() {
        return allCategories;
    }

    public void insertTask(Task task) {
        repository.insertTask(task);
    }

    public void insertCategory(TaskCategory category) {
        repository.insertCategory(category);
    }
}