package com.e2_ma_tim09_2025.questify.activities.specialTasks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.bosses.BossMainActivity;
import com.e2_ma_tim09_2025.questify.activities.taskCategories.TaskCategoriesMainActivity;
import com.e2_ma_tim09_2025.questify.activities.tasks.TasksMainActivity;
import com.e2_ma_tim09_2025.questify.activities.users.ProfileActivity;
import com.e2_ma_tim09_2025.questify.fragments.specialTasks.SpecialTasksListFragment;
import com.e2_ma_tim09_2025.questify.viewmodels.SpecialTasksViewModel;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SpecialTasksMainActivity extends AppCompatActivity {

    private SpecialTasksViewModel specialTasksViewModel;
    private MaterialButton logoutButton;
    private MaterialButton profileButton;
    private MaterialButton bossButton;
    private TextView specialTasksTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_tasks_main);

        specialTasksViewModel = new ViewModelProvider(this).get(SpecialTasksViewModel.class);

        specialTasksTitle = findViewById(R.id.specialTasksTitle);
        logoutButton = findViewById(R.id.logout_button);
        profileButton = findViewById(R.id.profile_button);
        bossButton = findViewById(R.id.boss_button);

        replaceFragment(new SpecialTasksListFragment());
        
        // Load special tasks to initialize alliance data
        specialTasksViewModel.loadSpecialTasks();

        specialTasksTitle.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(SpecialTasksMainActivity.this, v);

            popup.getMenu().add("Quests");
            popup.getMenu().add("Categories");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Quests")) {
                    Intent intent = new Intent(SpecialTasksMainActivity.this, TasksMainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (title.equals("Categories")) {
                    Intent intent = new Intent(SpecialTasksMainActivity.this, TaskCategoriesMainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            });

            popup.show();
        });

        logoutButton.setOnClickListener(v -> {
            specialTasksViewModel.logout();
            Intent intent = new Intent(SpecialTasksMainActivity.this, com.e2_ma_tim09_2025.questify.activities.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(SpecialTasksMainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        specialTasksViewModel.isBossActive().observe(this, isActive -> {
            if (isActive != null && isActive) {
                bossButton.setVisibility(com.google.android.material.button.MaterialButton.VISIBLE);
            } else {
                bossButton.setVisibility(com.google.android.material.button.MaterialButton.GONE);
            }
        });

        bossButton.setOnClickListener(v -> {
            Intent intent = new Intent(SpecialTasksMainActivity.this, BossMainActivity.class);
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
