package com.e2_ma_tim09_2025.questify.activities.tasks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TasksRecyclerViewAdapter;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksMainActivity extends AppCompatActivity {

    private static final String TAG = "TasksMain";
    private TaskViewModel taskViewModel;
    private RecyclerView recyclerViewTasks;
    private TasksRecyclerViewAdapter taskAdapter;
    private MaterialButton addTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_main);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TasksRecyclerViewAdapter();
        recyclerViewTasks.setAdapter(taskAdapter);

        addTaskButton = findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(TasksMainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // For testing purposes add categories
        taskViewModel.getCategories().observe(this, categories -> {
            if (categories.isEmpty()) {
                taskViewModel.insertCategory(new TaskCategory("Adventure", "AdventureDesc", Color.parseColor("#FF6B6B")));
                taskViewModel.insertCategory(new TaskCategory("Puzzle", "PuzzleDesc", Color.parseColor("#6BCB77")));
                taskViewModel.insertCategory(new TaskCategory("Health", "HealthDesc", Color.parseColor("#4D96FF")));
            }
        });

        taskViewModel.getTasks().observe(this, tasks -> {
            Log.d(TAG, "Task list updated! Total tasks: " + tasks.size());
            taskAdapter.setTasks(tasks);
        });
    }
}