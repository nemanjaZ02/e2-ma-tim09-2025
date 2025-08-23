package com.e2_ma_tim09_2025.questify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.TasksAdapter;
import com.e2_ma_tim09_2025.questify.db.AppDatabase;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.google.android.material.button.MaterialButton;

public class TasksMainActivity extends AppCompatActivity {

    private static final String TAG = "TasksMain";
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerViewTasks;
    private TasksAdapter taskAdapter;
    private MaterialButton addTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_main);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TasksAdapter();
        recyclerViewTasks.setAdapter(taskAdapter);

        addTaskButton = findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(TasksMainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        AppDatabase db = AppDatabase.getDatabase(this);
        TaskRepository repository = new TaskRepository(db.taskDao(), db.taskCategoryDao());

        taskViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new TaskViewModel(repository);
            }
        }).get(TaskViewModel.class);

        taskViewModel.getTasks().observe(this, tasks -> {
            Log.d(TAG, "Task list updated! Total tasks: " + tasks.size());
            taskAdapter.setTasks(tasks);
        });
    }
}