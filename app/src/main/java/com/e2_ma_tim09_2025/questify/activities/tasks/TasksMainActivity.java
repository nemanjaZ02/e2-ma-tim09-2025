package com.e2_ma_tim09_2025.questify.activities.tasks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.MainActivity;
import com.e2_ma_tim09_2025.questify.activities.StatisticsActivity;
import com.e2_ma_tim09_2025.questify.activities.bosses.BossMainActivity;
import com.e2_ma_tim09_2025.questify.activities.specialTasks.SpecialTasksMainActivity;
import com.e2_ma_tim09_2025.questify.activities.taskCategories.TaskCategoriesMainActivity;
import com.e2_ma_tim09_2025.questify.activities.users.LoginActivity;
import com.e2_ma_tim09_2025.questify.activities.users.ProfileActivity;
import com.e2_ma_tim09_2025.questify.fragments.tasks.TasksCalendarFragment;
import com.e2_ma_tim09_2025.questify.fragments.tasks.TasksListFragment;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.services.SpecialMissionService;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.e2_ma_tim09_2025.questify.viewmodels.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.e2_ma_tim09_2025.questify.fragments.tasks.TasksFilterFragment;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class TasksMainActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private UserViewModel userViewModel;
    @Inject SpecialMissionService specialMissionService;
    private MaterialButton addTaskButton;
    private MaterialButton viewChangeButton;
    private MaterialButton filterButton;
    private MaterialButton logoutButton;
    MaterialButton bossButton;
    private TextView tasksTitle;
    private boolean showingCalendar = false;
    private final MediatorLiveData<Boolean> isFilterActive = new MediatorLiveData<>();
    
    // Menu components
    private View sideMenuContainer;
    private View menuOverlay;
    private ImageView hamburgerMenu;
    private ImageView closeMenu;
    private boolean isMenuOpen = false;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_main);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Initialize menu components
        sideMenuContainer = findViewById(R.id.sideMenuContainer);
        menuOverlay = findViewById(R.id.menuOverlay);
        hamburgerMenu = findViewById(R.id.hamburgerMenu);
        closeMenu = findViewById(R.id.closeMenu);
        
        // Ensure menu is hidden by default
        sideMenuContainer.setVisibility(View.GONE);
        menuOverlay.setVisibility(View.GONE);
        
        // Set up gesture detector for swipe-to-close
        setupGestureDetector();
        
        // Set up menu click listeners
        setupMenuListeners();

        tasksTitle = findViewById(R.id.tasksTitle);
        addTaskButton = findViewById(R.id.add_task_button);
        viewChangeButton = findViewById(R.id.toggle_view_button);
        filterButton = findViewById(R.id.filter_button);
        logoutButton = findViewById(R.id.logout_button);
        bossButton = findViewById(R.id.boss_button);

        replaceFragment(new TasksListFragment());

        // Proveri istekle misije pri pokretanju aplikacije
        specialMissionService.checkExpiredMissions(task -> {
            if (task.isSuccessful()) {
                android.util.Log.d("TasksMainActivity", "Expired missions check completed");
            } else {
                android.util.Log.e("TasksMainActivity", "Failed to check expired missions", task.getException());
            }
        });

        isFilterActive.addSource(taskViewModel.getSelectedCategoryIds(), ids -> updateFilterState());
        isFilterActive.addSource(taskViewModel.getSelectedDifficulties(), difficulties -> updateFilterState());
        isFilterActive.addSource(taskViewModel.getSelectedPriorities(), priorities -> updateFilterState());
        isFilterActive.addSource(taskViewModel.getIsRecurringFilter(), isRecurring -> updateFilterState());

        tasksTitle.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(TasksMainActivity.this, v);

            popup.getMenu().clear();
            popup.getMenu().add("Categories");
            popup.getMenu().add("Special Tasks");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Categories")) {
                    Intent intent = new Intent(TasksMainActivity.this, TaskCategoriesMainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (title.equals("Special Tasks")) {
                    Intent intent = new Intent(TasksMainActivity.this, SpecialTasksMainActivity.class);
                    startActivity(intent);
                    finish();
                }
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

            Intent intent = new Intent(TasksMainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // prevent back navigation
            startActivity(intent);
            finish();
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

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (isMenuOpen && e1.getX() < e2.getX()) {
                    // Swipe from left to right (swipe right) - close menu
                    closeMenu();
                    return true;
                }
                return false;
            }
        });
    }

    private void setupMenuListeners() {
        // Hamburger menu click listener
        hamburgerMenu.setOnClickListener(v -> {
            if (!isMenuOpen) {
                openMenu();
            }
        });

        // Close menu click listener
        closeMenu.setOnClickListener(v -> {
            if (isMenuOpen) {
                closeMenu();
            }
        });

        // Overlay click listener (close menu when clicking outside)
        menuOverlay.setOnClickListener(v -> {
            if (isMenuOpen) {
                closeMenu();
            }
        });

        // Add touch listener to side menu for swipe gesture
        sideMenuContainer.setOnTouchListener((v, event) -> {
            if (isMenuOpen) {
                return gestureDetector.onTouchEvent(event);
            }
            return false;
        });

        // Profile menu item click listener
        View profileMenuItem = findViewById(R.id.profileMenuItem);
        profileMenuItem.setOnClickListener(v -> {
            closeMenu(); // Close the side menu
            Intent intent = new Intent(TasksMainActivity.this, com.e2_ma_tim09_2025.questify.activities.users.ProfileActivity.class);
            startActivity(intent);
        });

        // Statistics menu item click listener
        View statisticsMenuItem = findViewById(R.id.statisticsMenuItem);
        statisticsMenuItem.setOnClickListener(v -> {
            closeMenu(); // Close the side menu
            Intent intent = new Intent(TasksMainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        // Shop menu item click listener
        View shopMenuItem = findViewById(R.id.shopMenuItem);
        shopMenuItem.setOnClickListener(v -> {
            closeMenu(); // Close the side menu
            Intent intent = new Intent(TasksMainActivity.this, com.e2_ma_tim09_2025.questify.activities.ShopActivity.class);
            startActivity(intent);
        });

        // Friends menu item click listener
        View friendsMenuItem = findViewById(R.id.friendsMenuItem);
        friendsMenuItem.setOnClickListener(v -> {
            closeMenu(); // Close the side menu
            Intent intent = new Intent(TasksMainActivity.this, com.e2_ma_tim09_2025.questify.activities.users.FriendsActivity.class);
            startActivity(intent);
        });

        // All Users menu item click listener
        View allUsersMenuItem = findViewById(R.id.allUsersMenuItem);
        allUsersMenuItem.setOnClickListener(v -> {
            closeMenu(); // Close the side menu
            Intent intent = new Intent(TasksMainActivity.this, com.e2_ma_tim09_2025.questify.activities.users.AllUsersActivity.class);
            startActivity(intent);
        });

        // Create Alliance menu item click listener
        View createAllianceMenuItem = findViewById(R.id.createAllianceMenuItem);
        createAllianceMenuItem.setOnClickListener(v -> {
            closeMenu(); // Close the side menu
            Intent intent = new Intent(TasksMainActivity.this, com.e2_ma_tim09_2025.questify.activities.alliance.CreateAllianceActivity.class);
            startActivity(intent);
        });

        // My Alliance menu item click listener
        View myAllianceMenuItem = findViewById(R.id.myAllianceMenuItem);
        myAllianceMenuItem.setOnClickListener(v -> {
            closeMenu(); // Close the side menu
            Intent intent = new Intent(TasksMainActivity.this, com.e2_ma_tim09_2025.questify.activities.alliance.MyAllianceActivity.class);
            startActivity(intent);
        });

        // Member Alliance menu item click listener
        View memberAllianceMenuItem = findViewById(R.id.memberAllianceMenuItem);
        memberAllianceMenuItem.setOnClickListener(v -> {
            closeMenu(); // Close the side menu
            Intent intent = new Intent(TasksMainActivity.this, com.e2_ma_tim09_2025.questify.activities.alliance.MemberAllianceActivity.class);
            startActivity(intent);
        });
    }

    private void openMenu() {
        isMenuOpen = true;
        
        // Show menu and overlay
        sideMenuContainer.setVisibility(View.VISIBLE);
        menuOverlay.setVisibility(View.VISIBLE);
        
        // Animate menu slide in
        TranslateAnimation slideIn = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, -1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        );
        slideIn.setDuration(300);
        slideIn.setFillAfter(true);
        
        sideMenuContainer.startAnimation(slideIn);
    }

    private void closeMenu() {
        isMenuOpen = false;
        
        // Animate menu slide out
        TranslateAnimation slideOut = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        );
        slideOut.setDuration(300);
        slideOut.setFillAfter(true);
        
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Hide menu and overlay after animation
                sideMenuContainer.setVisibility(View.GONE);
                menuOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        sideMenuContainer.startAnimation(slideOut);
    }
}
