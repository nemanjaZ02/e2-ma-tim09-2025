package com.e2_ma_tim09_2025.questify.activities.taskCategories;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.bosses.BossMainActivity;
import com.e2_ma_tim09_2025.questify.activities.specialTasks.SpecialTasksMainActivity;
import com.e2_ma_tim09_2025.questify.activities.tasks.TasksMainActivity;
import com.e2_ma_tim09_2025.questify.fragments.taskCategories.TaskCategoriesListFragment;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskCategoryViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TaskCategoriesMainActivity extends AppCompatActivity {

    private TaskCategoryViewModel categoryViewModel;
    private MaterialButton addCategoryButton;
    private MaterialButton logoutButton;
    private MaterialButton profileButton;
    private TextView categoriesTitle;
    private MaterialButton bossButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_categories_main);

        categoryViewModel = new ViewModelProvider(this).get(TaskCategoryViewModel.class);

        categoriesTitle = findViewById(R.id.categoriesTitle);
        addCategoryButton = findViewById(R.id.add_category_button);
        logoutButton = findViewById(R.id.logout_button);
        profileButton = findViewById(R.id.profile_button);
        bossButton = findViewById(R.id.boss_button);

        replaceFragment(new TaskCategoriesListFragment());

        categoriesTitle.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(TaskCategoriesMainActivity.this, v);

            popup.getMenu().add("Quests");
            popup.getMenu().add("Special Tasks");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Quests")) {
                    Intent intent = new Intent(TaskCategoriesMainActivity.this, TasksMainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (title.equals("Special Tasks")) {
                    Intent intent = new Intent(TaskCategoriesMainActivity.this, SpecialTasksMainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            });

            popup.show();
        });

        addCategoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(TaskCategoriesMainActivity.this, AddTaskCategoryActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {});
        profileButton.setOnClickListener(v -> {});

        categoryViewModel.isBossActive().observe(this, isActive -> {
            if (isActive != null && isActive) {
                bossButton.setVisibility(MaterialButton.VISIBLE);
            } else {
                bossButton.setVisibility(MaterialButton.GONE);
            }
        });

        bossButton.setOnClickListener(v -> {
            Intent intent = new Intent(TaskCategoriesMainActivity.this, BossMainActivity.class);
            startActivity(intent);
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
