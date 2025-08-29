package com.e2_ma_tim09_2025.questify.activities.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.enums.RecurrenceUnit;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TaskDetailsActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private Task currentTask;

    private TextInputEditText taskNameEditText, taskDescriptionEditText, recurrenceIntervalEditText;
    private Spinner difficultySpinner, prioritySpinner, recurrenceUnitSpinner;;
    private TextView categoryTextView;
    private CheckBox recurrenceCheckBox;
    private View recurrenceFields;
    private MaterialButton finishDateButton, recurrenceEndDateButton;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private String backupTaskName, backupTaskDescription;
    private TaskDifficulty backupDifficulty;
    private TaskPriority backupPriority;
    private ImageButton editTaskNameButton, editTaskDescriptionButton, editDifficultyButton, editPriorityButton, editFinishDateButton;
    // Task Name Editing
    private LinearLayout taskNameEditButtonsLayout;
    private MaterialButton acceptTaskNameEditButton, cancelTaskNameEditButton;
    // Task Description Editing
    private LinearLayout taskDescriptionEditButtonsLayout;
    private MaterialButton acceptTaskDescriptionEditButton, cancelTaskDescriptionEditButton;
    // Difficulty Editing
    private LinearLayout difficultyEditButtonsLayout;
    private MaterialButton acceptDifficultyEditButton, cancelDifficultyEditButton;
    // Priority Editing
    private LinearLayout priorityEditButtonsLayout;
    private MaterialButton acceptPriorityEditButton, cancelPriorityEditButton;
    private LinearLayout taskActionButtonsLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        taskNameEditText = findViewById(R.id.taskNameEditText);
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText);
        categoryTextView = findViewById(R.id.categoryTextView);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        finishDateButton = findViewById(R.id.finishDateButton);
        recurrenceCheckBox = findViewById(R.id.recurrenceCheckBox);
        recurrenceFields = findViewById(R.id.recurrenceFields);
        recurrenceEndDateButton = findViewById(R.id.recurrenceEndDateButton);
        recurrenceIntervalEditText = findViewById(R.id.recurrenceIntervalEditText);
        recurrenceUnitSpinner = findViewById(R.id.recurrenceUnitSpinner);
        editTaskNameButton = findViewById(R.id.editTaskNameButton);
        editTaskDescriptionButton = findViewById(R.id.editTaskDescriptionButton);
        editDifficultyButton = findViewById(R.id.editDifficultyButton);
        editPriorityButton = findViewById(R.id.editPriorityButton);
        editFinishDateButton = findViewById(R.id.editFinishDateButton);
        taskNameEditButtonsLayout = findViewById(R.id.taskNameEditButtonsLayout);
        acceptTaskNameEditButton = findViewById(R.id.acceptTaskNameEditButton);
        cancelTaskNameEditButton = findViewById(R.id.cancelTaskNameEditButton);
        taskDescriptionEditButtonsLayout = findViewById(R.id.taskDescriptionEditButtonsLayout);
        acceptTaskDescriptionEditButton = findViewById(R.id.acceptTaskDescriptionEditButton);
        cancelTaskDescriptionEditButton = findViewById(R.id.cancelTaskDescriptionEditButton);
        difficultyEditButtonsLayout = findViewById(R.id.difficultyEditButtonsLayout);
        acceptDifficultyEditButton = findViewById(R.id.acceptDifficultyEditButton);
        cancelDifficultyEditButton = findViewById(R.id.cancelDifficultyEditButton);
        priorityEditButtonsLayout = findViewById(R.id.priorityEditButtonsLayout);
        acceptPriorityEditButton = findViewById(R.id.acceptPriorityEditButton);
        cancelPriorityEditButton = findViewById(R.id.cancelPriorityEditButton);
        taskActionButtonsLayout = findViewById(R.id.taskActionButtonsLayout);

        int taskId = getIntent().getIntExtra("taskId", -1);
        taskViewModel.getTaskById(taskId).observe(this, task -> {
            if (task != null) {
                currentTask = task;
                populateUI(task);
            }
        });

        editTaskNameButton.setOnClickListener(v -> startTaskNameEditing());
        acceptTaskNameEditButton.setOnClickListener(v -> saveTaskNameEdits());
        cancelTaskNameEditButton.setOnClickListener(v -> cancelTaskNameEdits());

        editTaskDescriptionButton.setOnClickListener(v -> startTaskDescriptionEditing());
        acceptTaskDescriptionEditButton.setOnClickListener(v -> saveTaskDescriptionEdits());
        cancelTaskDescriptionEditButton.setOnClickListener(v -> cancelTaskDescriptionEdits());

        editDifficultyButton.setOnClickListener(v -> startDifficultyEditing());
        acceptDifficultyEditButton.setOnClickListener(v -> saveDifficultyEdits());
        cancelDifficultyEditButton.setOnClickListener(v -> cancelDifficultyEdits());

        editPriorityButton.setOnClickListener(v -> startPriorityEditing());
        acceptPriorityEditButton.setOnClickListener(v -> savePriorityEdits());
        cancelPriorityEditButton.setOnClickListener(v -> cancelPriorityEdits());

        editFinishDateButton.setOnClickListener(v -> showDatePicker(finishDateButton));
        finishDateButton.setEnabled(false);

        finishDateButton.setOnClickListener(v -> showDatePicker(finishDateButton));

        recurrenceCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recurrenceFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private void populateUI(Task task) {
        taskNameEditText.setText(task.getName());
        taskDescriptionEditText.setText(task.getDescription());

        taskViewModel.getTaskCategoryById(task.getCategoryId()).observe(this, category -> {
            if (category != null) {
                categoryTextView.setText(category.getName());
                categoryTextView.setTextColor(category.getColor());
            } else {
                categoryTextView.setText("Unknown");
            }
        });

        // Difficulty Spinner setup
        List<String> difficultyNames = new ArrayList<>();
        for (TaskDifficulty difficulty : TaskDifficulty.values()) {
            difficultyNames.add(difficulty.name().replace("_", " "));
        }
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, difficultyNames);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
        difficultySpinner.setSelection(difficultyAdapter.getPosition(task.getDifficulty().name().replace("_", " ")));
        difficultySpinner.setEnabled(false);

        // Priority Spinner setup
        List<String> priorityNames = new ArrayList<>();
        for (TaskPriority priority : TaskPriority.values()) {
            priorityNames.add(priority.name().replace("_", " "));
        }
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, priorityNames);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);
        prioritySpinner.setSelection(priorityAdapter.getPosition(task.getPriority().name().replace("_", " ")));
        prioritySpinner.setEnabled(false);

        calendar.setTimeInMillis(task.getFinishDate());
        finishDateButton.setText(dateFormat.format(calendar.getTime()));

        if (task.getRecurrence() != null && task.getRecurrence().isRecurring()) {
            recurrenceCheckBox.setChecked(true);
            recurrenceFields.setVisibility(View.VISIBLE);

            int interval = task.getRecurrence().getInterval();
            recurrenceIntervalEditText.setText(String.valueOf(interval));

            calendar.setTimeInMillis(task.getRecurrence().getEndDate());
            recurrenceEndDateButton.setText(dateFormat.format(calendar.getTime()));

            // Recurrence unit spinner setup
            List<String> unitNames = new ArrayList<>();
            for (RecurrenceUnit unit : RecurrenceUnit.values()) {
                unitNames.add(unit.name());
            }
            ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, unitNames);
            unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            recurrenceUnitSpinner.setAdapter(unitAdapter);
            recurrenceUnitSpinner.setSelection(unitAdapter.getPosition(task.getRecurrence().getUnit().name().concat("s")));
            recurrenceUnitSpinner.setEnabled(false);
        }
    }

    private void hideTaskActionButtons() {
        if (taskActionButtonsLayout != null) {
            taskActionButtonsLayout.setVisibility(View.GONE);
        }
    }

    private void showTaskActionButtons() {
        if (taskActionButtonsLayout != null) {
            taskActionButtonsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideAllEditButtons() {
        editTaskNameButton.setVisibility(View.GONE);
        editTaskDescriptionButton.setVisibility(View.GONE);
        editDifficultyButton.setVisibility(View.GONE);
        editPriorityButton.setVisibility(View.GONE);
        editFinishDateButton.setVisibility(View.GONE);
    }

    private void showAllEditButtons() {
        editTaskNameButton.setVisibility(View.VISIBLE);
        editTaskDescriptionButton.setVisibility(View.VISIBLE);
        editDifficultyButton.setVisibility(View.VISIBLE);
        editPriorityButton.setVisibility(View.VISIBLE);
        editFinishDateButton.setVisibility(View.VISIBLE);
    }

    private void startTaskNameEditing() {
        hideTaskActionButtons();
        hideAllEditButtons();
        backupTaskName = taskNameEditText.getText().toString();
        taskNameEditText.setEnabled(true);
        editTaskNameButton.setVisibility(View.GONE);
        taskNameEditButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void saveTaskNameEdits() {
        if (currentTask != null) {
            currentTask.setName(taskNameEditText.getText().toString());
            taskViewModel.updateTask(currentTask);
        }
        stopTaskNameEditing();
    }

    private void cancelTaskNameEdits() {
        taskNameEditText.setText(backupTaskName);
        stopTaskNameEditing();
    }

    private void stopTaskNameEditing() {
        taskNameEditText.setEnabled(false);
        editTaskNameButton.setVisibility(View.VISIBLE);
        taskNameEditButtonsLayout.setVisibility(View.GONE);
        showTaskActionButtons();
        showAllEditButtons();
    }

    private void startTaskDescriptionEditing() {
        hideAllEditButtons();
        hideTaskActionButtons();
        backupTaskDescription = taskDescriptionEditText.getText().toString();
        taskDescriptionEditText.setEnabled(true);
        editTaskDescriptionButton.setVisibility(View.GONE);
        taskDescriptionEditButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void saveTaskDescriptionEdits() {
        if (currentTask != null) {
            currentTask.setDescription(taskDescriptionEditText.getText().toString());
            taskViewModel.updateTask(currentTask);
        }
        stopTaskDescriptionEditing();
    }

    private void cancelTaskDescriptionEdits() {
        taskDescriptionEditText.setText(backupTaskDescription);
        stopTaskDescriptionEditing();
    }

    private void stopTaskDescriptionEditing() {
        taskDescriptionEditText.setEnabled(false);
        editTaskDescriptionButton.setVisibility(View.VISIBLE);
        taskDescriptionEditButtonsLayout.setVisibility(View.GONE);
        showTaskActionButtons();
        showAllEditButtons();
    }

    private void startDifficultyEditing() {
        hideTaskActionButtons();
        hideAllEditButtons();
        backupDifficulty = currentTask.getDifficulty();
        difficultySpinner.setEnabled(true);
        editDifficultyButton.setVisibility(View.GONE);
        difficultyEditButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void saveDifficultyEdits() {
        if (currentTask != null) {
            String selectedDifficulty = difficultySpinner.getSelectedItem().toString().replace(" ", "_").toUpperCase();
            currentTask.setDifficulty(TaskDifficulty.valueOf(selectedDifficulty));
            taskViewModel.updateTask(currentTask);
        }
        stopDifficultyEditing();
    }

    private void cancelDifficultyEdits() {
        difficultySpinner.setSelection(((ArrayAdapter<String>) difficultySpinner.getAdapter()).getPosition(backupDifficulty.name().replace("_", " ")));
        stopDifficultyEditing();
    }

    private void stopDifficultyEditing() {
        difficultySpinner.setEnabled(false);
        editDifficultyButton.setVisibility(View.VISIBLE);
        difficultyEditButtonsLayout.setVisibility(View.GONE);
        showTaskActionButtons();
        showAllEditButtons();
    }

    private void startPriorityEditing() {
        hideAllEditButtons();
        hideTaskActionButtons();
        backupPriority = currentTask.getPriority();
        prioritySpinner.setEnabled(true);
        editPriorityButton.setVisibility(View.GONE);
        priorityEditButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void savePriorityEdits() {
        if (currentTask != null) {
            String selectedPriority = prioritySpinner.getSelectedItem().toString().replace(" ", "_").toUpperCase();
            currentTask.setPriority(TaskPriority.valueOf(selectedPriority));
            taskViewModel.updateTask(currentTask);
        }
        stopPriorityEditing();
    }

    private void cancelPriorityEdits() {
        prioritySpinner.setSelection(((ArrayAdapter<String>) prioritySpinner.getAdapter()).getPosition(backupPriority.name().replace("_", " ")));
        stopPriorityEditing();
    }

    private void stopPriorityEditing() {
        prioritySpinner.setEnabled(false);
        editPriorityButton.setVisibility(View.VISIBLE);
        priorityEditButtonsLayout.setVisibility(View.GONE);
        showTaskActionButtons();
        showAllEditButtons();
    }

    private void showDatePicker(MaterialButton button) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, y, m, d) -> {
            calendar.set(y, m, d);
            button.setText(dateFormat.format(calendar.getTime()));

            if (currentTask != null) {
                currentTask.setFinishDate(calendar.getTimeInMillis());
                taskViewModel.updateTask(currentTask);
                Toast.makeText(this, "Deadline updated", Toast.LENGTH_SHORT).show();
            }
        }, year, month, day);

        datePicker.show();
    }
}
