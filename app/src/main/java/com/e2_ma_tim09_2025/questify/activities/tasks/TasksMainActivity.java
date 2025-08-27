package com.e2_ma_tim09_2025.questify.activities.tasks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import fragments.tasks.TasksCalendarFragment;
import fragments.tasks.TasksListFragment;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksMainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private MaterialButton addTaskButton;
    private MaterialButton viewChangeButton;
    private boolean showingCalendar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_main);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        addTaskButton = findViewById(R.id.add_task_button);
        viewChangeButton = findViewById(R.id.toggle_view_button);

        replaceFragment(new TasksListFragment());

        viewChangeButton.setOnClickListener(v -> {
            Fragment fragment;
            if (showingCalendar) {
                fragment = new TasksListFragment();
                viewChangeButton.setIconResource(R.drawable.ic_calendar);
            } else {
                fragment = new TasksCalendarFragment();
                viewChangeButton.setIconResource(R.drawable.ic_list);
            }
            showingCalendar = !showingCalendar;
            replaceFragment(fragment);
        });

        addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(TasksMainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Za test kategorije
        taskViewModel.getCategories().observe(this, categories -> {
            if (categories.isEmpty()) {
                taskViewModel.insertCategory(new TaskCategory("Adventure", "AdventureDesc", Color.parseColor("#FF6B6B")));
                taskViewModel.insertCategory(new TaskCategory("Puzzle", "PuzzleDesc", Color.parseColor("#6BCB77")));
                taskViewModel.insertCategory(new TaskCategory("Health", "HealthDesc", Color.parseColor("#4D96FF")));
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
