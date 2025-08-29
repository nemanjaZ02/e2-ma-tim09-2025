package com.e2_ma_tim09_2025.questify.fragments.tasks;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksFilterFragment extends BottomSheetDialogFragment {

    public static final String TAG = "TasksFilterFragment";
    private TaskViewModel taskViewModel;
    private LinearLayout categoryFilterLayout;
    private TextView categoriesTitle;
    private LinearLayout recurrenceCheckboxLayout;
    private TextView recurrenceTitle;
    private LinearLayout difficultyFilterLayout;
    private TextView difficultyTitle;
    private LinearLayout priorityFilterLayout;
    private TextView priorityTitle;
    MaterialButton applyFilterButton, clearFilterButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks_filter, container, false);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        categoryFilterLayout = view.findViewById(R.id.category_filter_layout);
        categoriesTitle = view.findViewById(R.id.categories_title);
        recurrenceCheckboxLayout = view.findViewById(R.id.recurrence_checkbox_layout);
        recurrenceTitle = view.findViewById(R.id.recurrence_title);
        difficultyFilterLayout = view.findViewById(R.id.difficulty_filter_layout);
        difficultyTitle = view.findViewById(R.id.difficulty_title);
        priorityFilterLayout = view.findViewById(R.id.priority_filter_layout);
        priorityTitle = view.findViewById(R.id.priority_title);

        applyFilterButton = view.findViewById(R.id.apply_filter_button);
        clearFilterButton = view.findViewById(R.id.clear_filter_button);

        categoriesTitle.setOnClickListener(v -> toggleVisibility(categoryFilterLayout));
        recurrenceTitle.setOnClickListener(v -> toggleVisibility(recurrenceCheckboxLayout));
        difficultyTitle.setOnClickListener(v -> toggleVisibility(difficultyFilterLayout));
        priorityTitle.setOnClickListener(v -> toggleVisibility(priorityFilterLayout));

        fetchCategoriesAndPopulateUI();
        populateRecurrenceUI();
        populateDifficultyUI();
        populatePriorityUI();

        applyFilterButton.setOnClickListener(v -> {
            dismiss();
        });

        clearFilterButton.setOnClickListener(v -> {
            taskViewModel.clearAllFilters();
            dismiss();
        });

        return view;
    }

    private void toggleVisibility(View view) {
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void fetchCategoriesAndPopulateUI() {
        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryFilterLayout.removeAllViews();

            int brownDarkColor = ContextCompat.getColor(requireContext(), R.color.brown_dark);

            for (TaskCategory category : categories) {
                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setText(category.getName());
                checkBox.setTextColor(brownDarkColor);
                checkBox.setTag(String.valueOf(category.getId()));

                Set<String> selectedCategories = taskViewModel.getSelectedCategoryIds().getValue();
                if (selectedCategories != null && selectedCategories.contains(String.valueOf(category.getId()))) {
                    checkBox.setChecked(true);
                }

                int categoryColor = category.getColor();
                checkBox.setButtonTintList(ColorStateList.valueOf(categoryColor));

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    String categoryId = (String) buttonView.getTag();
                    if (isChecked) {
                        taskViewModel.addCategoryFilter(categoryId);
                    } else {
                        taskViewModel.removeCategoryFilter(categoryId);
                    }
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                int upMarginInPx = (int) (8 * getResources().getDisplayMetrics().density);
                int leftMarginInPx = (int) (4 * getResources().getDisplayMetrics().density);
                params.setMargins(leftMarginInPx, upMarginInPx, 0, 0);

                checkBox.setLayoutParams(params);
                categoryFilterLayout.addView(checkBox);
            }
        });
    }
    private void populateRecurrenceUI() {
        recurrenceCheckboxLayout.removeAllViews();

        int brownDarkColor = ContextCompat.getColor(requireContext(), R.color.brown_dark);
        int buttonTint = android.graphics.Color.parseColor("#8B4513");

        Boolean isRecurring = taskViewModel.getIsRecurringFilter().getValue();

        CheckBox recurringCheckBox = new CheckBox(getContext());
        if (isRecurring != null && isRecurring) {
            recurringCheckBox.setChecked(true);
        }

        recurringCheckBox.setText("Recurring");
        recurringCheckBox.setTextColor(brownDarkColor);
        recurringCheckBox.setTag("recurring");
        recurringCheckBox.setButtonTintList(ColorStateList.valueOf(buttonTint));

        CheckBox nonRecurringCheckBox = new CheckBox(getContext());
        if (isRecurring != null && !isRecurring) {
            nonRecurringCheckBox.setChecked(true);
        }
        nonRecurringCheckBox.setText("Non-recurring");
        nonRecurringCheckBox.setTextColor(brownDarkColor);
        nonRecurringCheckBox.setTag("non_recurring");
        nonRecurringCheckBox.setButtonTintList(ColorStateList.valueOf(buttonTint));

        recurringCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                nonRecurringCheckBox.setChecked(false);
                taskViewModel.setRecurringFilter(true);
            } else if (!nonRecurringCheckBox.isChecked()) {
                taskViewModel.setRecurringFilter(null);
            }
        });

        nonRecurringCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                recurringCheckBox.setChecked(false);
                taskViewModel.setRecurringFilter(false);
            } else if (!recurringCheckBox.isChecked()) {
                taskViewModel.setRecurringFilter(null);
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int upMarginInPx = (int) (8 * getResources().getDisplayMetrics().density);
        int leftMarginInPx = (int) (4 * getResources().getDisplayMetrics().density);
        params.setMargins(leftMarginInPx, upMarginInPx, 0, 0);

        recurringCheckBox.setLayoutParams(params);
        nonRecurringCheckBox.setLayoutParams(params);

        recurrenceCheckboxLayout.addView(recurringCheckBox);
        recurrenceCheckboxLayout.addView(nonRecurringCheckBox);
    }
    private void populateDifficultyUI() {
        difficultyFilterLayout.removeAllViews();

        int brownDarkColor = ContextCompat.getColor(requireContext(), R.color.brown_dark);
        int buttonTint = android.graphics.Color.parseColor("#8B4513");

        for (TaskDifficulty difficulty : TaskDifficulty.values()) {
            CheckBox checkBox = new CheckBox(getContext());
            String formattedName = difficulty.name().replace("_", " ");
            formattedName = formattedName.substring(0, 1).toUpperCase() + formattedName.substring(1).toLowerCase();
            checkBox.setText(formattedName);

            checkBox.setTextColor(brownDarkColor);
            checkBox.setTag(difficulty.name());

            Set<TaskDifficulty> selectedDifficulties = taskViewModel.getSelectedDifficulties().getValue();
            if (selectedDifficulties != null && selectedDifficulties.contains(difficulty)) {
                checkBox.setChecked(true);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                TaskDifficulty selectedDifficulty = TaskDifficulty.valueOf((String) buttonView.getTag());
                if (isChecked) {
                    taskViewModel.addDifficultyFilter(selectedDifficulty);
                } else {
                    taskViewModel.removeDifficultyFilter(selectedDifficulty);
                }
            });

            checkBox.setButtonTintList(ColorStateList.valueOf(buttonTint));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            int upMarginInPx = (int) (8 * getResources().getDisplayMetrics().density);
            int leftMarginInPx = (int) (4 * getResources().getDisplayMetrics().density);
            params.setMargins(leftMarginInPx, upMarginInPx, 0, 0);

            checkBox.setLayoutParams(params);

            difficultyFilterLayout.addView(checkBox);
        }
    }
    private void populatePriorityUI() {
        priorityFilterLayout.removeAllViews();

        int brownDarkColor = ContextCompat.getColor(requireContext(), R.color.brown_dark);
        int buttonTint = android.graphics.Color.parseColor("#8B4513");

        for (TaskPriority priority : TaskPriority.values()) {
            CheckBox checkBox = new CheckBox(getContext());
            String formattedName = priority.name().replace("_", " ");
            formattedName = formattedName.substring(0, 1).toUpperCase() + formattedName.substring(1).toLowerCase();
            checkBox.setText(formattedName);

            checkBox.setTextColor(brownDarkColor);
            checkBox.setTag(priority.name());

            Set<TaskPriority> selectedPriorities = taskViewModel.getSelectedPriorities().getValue();
            if (selectedPriorities != null && selectedPriorities.contains(priority)) {
                checkBox.setChecked(true);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                TaskPriority selectedPriority = TaskPriority.valueOf((String) buttonView.getTag());
                if (isChecked) {
                    taskViewModel.addPriorityFilter(selectedPriority);
                } else {
                    taskViewModel.removePriorityFilter(selectedPriority);
                }
            });

            checkBox.setButtonTintList(ColorStateList.valueOf(buttonTint));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            int upMarginInPx = (int) (8 * getResources().getDisplayMetrics().density);
            int leftMarginInPx = (int) (4 * getResources().getDisplayMetrics().density);
            params.setMargins(leftMarginInPx, upMarginInPx, 0, 0);

            checkBox.setLayoutParams(params);

            priorityFilterLayout.addView(checkBox);
        }
    }
}