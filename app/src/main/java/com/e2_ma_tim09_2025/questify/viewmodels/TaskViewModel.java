package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;
import java.util.List;
import java.util.concurrent.Executors;


public class TaskViewModel extends ViewModel {

    private final TaskRepository repository;
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<List<TaskCategory>> categories = new MutableLiveData<>();

    public TaskViewModel(TaskRepository repository) {
        this.repository = repository;
        loadTasks();
        loadCategories();
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<List<TaskCategory>> getCategories() {
        return categories;
    }

    public void loadTasks() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Task> taskList = repository.getAllTasks();
            tasks.postValue(taskList);
        });
    }

    public void insertTask(Task task) {
        repository.insertTask(task);
        loadTasks();
    }

    public void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<TaskCategory> categoryList = repository.getAllCategories();
            categories.postValue(categoryList);
        });
    }

    public void insertCategory(TaskCategory category) {
        repository.insertCategory(category);
        loadCategories();
    }
}