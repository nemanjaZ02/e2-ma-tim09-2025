package com.e2_ma_tim09_2025.questify.activities.tasks;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TaskCategoriesSpinnerAdapter;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.TaskRecurrence;
import com.e2_ma_tim09_2025.questify.models.enums.RecurrenceUnit;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddTaskActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;
    private TextInputEditText taskNameEditText;
    private TextInputEditText taskDescriptionEditText;
    private Spinner categorySpinner;
    private Spinner difficultySpinner;
    private Spinner prioritySpinner;
    private CheckBox recurrenceCheckBox;
    private LinearLayout recurrenceFields;
    private TextInputEditText recurrenceIntervalEditText;
    private Spinner recurrenceUnitSpinner;
    private Button recurrenceEndDateButton;
    private long recurrenceEndDate = 0;
    private Button finishDateButton;
    private long finishDateMillis = 0;
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
        difficultySpinner = findViewById(R.id.difficultySpinner);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        recurrenceCheckBox = findViewById(R.id.recurrenceCheckBox);
        recurrenceFields = findViewById(R.id.recurrenceFields);
        recurrenceIntervalEditText = findViewById(R.id.recurrenceIntervalEditText);
        recurrenceUnitSpinner = findViewById(R.id.recurrenceUnitSpinner);
        recurrenceEndDateButton = findViewById(R.id.recurrenceEndDateButton);
        finishDateButton = findViewById(R.id.finishDateButton);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        recurrenceCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                recurrenceFields.setVisibility(VISIBLE);
                recurrenceEndDateButton.setVisibility(VISIBLE);
            } else {
                recurrenceFields.setVisibility(GONE);
                recurrenceEndDateButton.setVisibility(GONE);
            }
        });

        taskViewModel.getCategories().observe(this, categories -> {
            taskCategories = categories;
            TaskCategoriesSpinnerAdapter categoryAdapter = new TaskCategoriesSpinnerAdapter(this, categories);
            categorySpinner.setAdapter(categoryAdapter);
        });

        TaskDifficulty[] difficulties = TaskDifficulty.values();
        List<String> difficultyNames = new ArrayList<>();
        for (TaskDifficulty difficulty : difficulties) {
            difficultyNames.add(difficulty.name().replace("_", " "));
        }

        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, difficultyNames);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);

        TaskPriority[] priorities = TaskPriority.values();
        List<String> priorityNames = new ArrayList<>();
        for (TaskPriority priority : priorities) {
            priorityNames.add(priority.name().replace("_", " "));
        }

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, priorityNames);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        RecurrenceUnit[] units = RecurrenceUnit.values();
        List<String> unitNames = new ArrayList<>();
        for (RecurrenceUnit unit : units) {
            unitNames.add(unit.name().replace("_", " ").concat("S"));
        }

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitNames);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceUnitSpinner.setAdapter(unitAdapter);

        recurrenceEndDateButton.setOnClickListener(v -> showDateTimeDialog(recurrenceEndDateButton, false));
        finishDateButton.setOnClickListener(v -> showDateTimeDialog(finishDateButton, true));

        saveTaskButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString().trim();
            String taskDescription = taskDescriptionEditText.getText().toString().trim();
            int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();
            int categoryId = taskCategories.get(selectedCategoryPosition).getId();
            int selectedPosition = difficultySpinner.getSelectedItemPosition();
            TaskDifficulty selectedDifficulty = TaskDifficulty.values()[selectedPosition];
            int selectedPriorityPosition = prioritySpinner.getSelectedItemPosition();
            TaskPriority selectedPriority = TaskPriority.values()[selectedPriorityPosition];
            TaskRecurrence recurrence = null;

            if (recurrenceCheckBox.isChecked()) {
                String intervalText = recurrenceIntervalEditText.getText().toString().trim();
                if (intervalText.isEmpty()) {
                    Toast.makeText(AddTaskActivity.this, "Please enter recurrence interval!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long endDate = recurrenceEndDate;
                if (recurrenceEndDate <= 0) {
                    Toast.makeText(AddTaskActivity.this, "Please select a recurrence end date!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int interval = Integer.parseInt(intervalText);
                int selectedUnitPosition = recurrenceUnitSpinner.getSelectedItemPosition();
                RecurrenceUnit selectedUnit = RecurrenceUnit.values()[selectedUnitPosition];
                long now = System.currentTimeMillis();

                recurrence = new TaskRecurrence(true, endDate, now, selectedUnit, interval);
            }

            if (taskName.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Quest title cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (taskDescription.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Quest description cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCategoryPosition < 0) {
                Toast.makeText(AddTaskActivity.this, "Please select a quest category!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (finishDateMillis <= 0) {
                Toast.makeText(AddTaskActivity.this, "Please select a deadline!", Toast.LENGTH_SHORT).show();
                return;
            }

            Task newTask = new Task(taskName, categoryId, taskDescription, selectedDifficulty, selectedPriority, recurrence, System.currentTimeMillis(), finishDateMillis, finishDateMillis - System.currentTimeMillis(), TaskStatus.ACTIVE);

            taskViewModel.insertTask(newTask);

            Toast.makeText(AddTaskActivity.this, "New quest accepted!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showDateTimeDialog(Button button, boolean isFinishDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);

                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    new TimePickerDialog(this, (timeView, selectedHour, selectedMinute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);

                        if (isFinishDate) {
                            finishDateMillis = calendar.getTimeInMillis();
                            button.setTextColor(Color.parseColor("#5C4033"));
                            button.setText("Complete until " + (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear + " " + selectedHour + ":" + selectedMinute);
                        } else {
                            recurrenceEndDate = calendar.getTimeInMillis();
                            button.setTextColor(Color.parseColor("#5C4033"));
                            button.setText("Recur until " + (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear + " " + selectedHour + ":" + selectedMinute);
                        }

                    }, hour, minute, true).show(); // `true` za 24-časovni format
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}