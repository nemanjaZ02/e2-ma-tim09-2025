package com.e2_ma_tim09_2025.questify.activities.tasks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.MainActivity;
import com.e2_ma_tim09_2025.questify.activities.bosses.BossMainActivity;
import com.e2_ma_tim09_2025.questify.activities.taskCategories.TaskCategoriesMainActivity;
import com.e2_ma_tim09_2025.questify.activities.users.ProfileActivity;
import com.e2_ma_tim09_2025.questify.fragments.tasks.TasksCalendarFragment;
import com.e2_ma_tim09_2025.questify.fragments.tasks.TasksListFragment;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.e2_ma_tim09_2025.questify.viewmodels.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.e2_ma_tim09_2025.questify.fragments.tasks.TasksFilterFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksMainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private UserViewModel userViewModel;
    private MaterialButton addTaskButton;
    private MaterialButton viewChangeButton;
    private MaterialButton filterButton;
    private MaterialButton logoutButton;
    private MaterialButton profileButton;
    MaterialButton bossButton;
    private TextView tasksTitle;
    private boolean showingCalendar = false;
    private final MediatorLiveData<Boolean> isFilterActive = new MediatorLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_main);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        tasksTitle = findViewById(R.id.tasksTitle);
        addTaskButton = findViewById(R.id.add_task_button);
        viewChangeButton = findViewById(R.id.toggle_view_button);
        filterButton = findViewById(R.id.filter_button);
        logoutButton = findViewById(R.id.logout_button);
        profileButton = findViewById(R.id.profile_button);
        bossButton = findViewById(R.id.boss_button);

        replaceFragment(new TasksListFragment());

        isFilterActive.addSource(taskViewModel.getSelectedCategoryIds(), ids -> updateFilterState());
        isFilterActive.addSource(taskViewModel.getSelectedDifficulties(), difficulties -> updateFilterState());
        isFilterActive.addSource(taskViewModel.getSelectedPriorities(), priorities -> updateFilterState());
        isFilterActive.addSource(taskViewModel.getIsRecurringFilter(), isRecurring -> updateFilterState());

        tasksTitle.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(TasksMainActivity.this, v);

            popup.getMenu().clear();
            popup.getMenu().add("Categories");

            popup.setOnMenuItemClickListener(item -> {
                Intent intent = new Intent(TasksMainActivity.this, TaskCategoriesMainActivity.class);
                startActivity(intent);
                return true;
            });

            popup.show();
        });

        isFilterActive.observe(this, isActive -> {
            if (isActive) {
                filterButton.setIconResource(R.drawable.ic_filter_on);
            } else {
                filterButton.setIconResource(R.drawable.ic_filter_off);
            }
        });

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

        filterButton.setOnClickListener(v -> {
            TasksFilterFragment filterFragment = new TasksFilterFragment();
            filterFragment.show(getSupportFragmentManager(), TasksFilterFragment.TAG);
        });

        logoutButton.setOnClickListener(v -> {
            userViewModel.logout();

            Intent intent = new Intent(TasksMainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // prevent back navigation
            startActivity(intent);
            finish();
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(TasksMainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        taskViewModel.isBossActive().observe(this, isActive -> {
            if (isActive != null && isActive) {
                bossButton.setVisibility(MaterialButton.VISIBLE);
            } else {
                bossButton.setVisibility(MaterialButton.GONE);
            }
        });

        bossButton.setOnClickListener(v -> {
            Intent intent = new Intent(TasksMainActivity.this, BossMainActivity.class);
            startActivity(intent);
        });
    }

    private void updateFilterState() {
        boolean anyFilterSelected =
                !taskViewModel.getSelectedCategoryIds().getValue().isEmpty() ||
                        !taskViewModel.getSelectedDifficulties().getValue().isEmpty() ||
                        !taskViewModel.getSelectedPriorities().getValue().isEmpty() ||
                        taskViewModel.getIsRecurringFilter().getValue() != null;

        isFilterActive.setValue(anyFilterSelected);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
