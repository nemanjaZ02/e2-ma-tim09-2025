package com.e2_ma_tim09_2025.questify.activities.Tours;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.db.AppDatabase;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.repositories.TaskCategoryRepository;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;
import com.e2_ma_tim09_2025.questify.services.TaskService; // UVEZI TaskService
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TextInputEditText taskNameEditText;
    private TextInputEditText taskDescriptionEditText;
    private Spinner categorySpinner;
    private Button saveTaskButton;
    private List<TaskCategory> taskCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        taskNameEditText = findViewById(R.id.taskNameEditText);
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveTaskButton = findViewById(R.id.saveTaskButton);

        AppDatabase db = AppDatabase.getDatabase(this);
        TaskRepository taskRepository = new TaskRepository(db.taskDao());
        TaskCategoryRepository categoryRepository = new TaskCategoryRepository(db.taskCategoryDao());
        TaskService taskService = new TaskService(taskRepository, categoryRepository);

        taskViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new TaskViewModel(taskService);
            }
        }).get(TaskViewModel.class);

        taskViewModel.getCategories().observe(this, categories -> {
            taskCategories = categories;
            List<String> categoryNames = new ArrayList<>();
            for (TaskCategory category : categories) {
                categoryNames.add(category.getName());
            }
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);
        });

        saveTaskButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString().trim();
            String taskDescription = taskDescriptionEditText.getText().toString().trim();
            int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();

            if (taskName.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Quest title cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            int categoryId = taskCategories.get(selectedCategoryPosition).getId();

            Task newTask = new Task(taskName, categoryId, taskDescription, null, null, null, 0, false);

            taskViewModel.insertTask(newTask);

            Toast.makeText(AddTaskActivity.this, "New quest accepted!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}