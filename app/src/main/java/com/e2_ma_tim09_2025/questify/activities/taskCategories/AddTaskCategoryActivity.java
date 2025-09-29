package com.e2_ma_tim09_2025.questify.activities.taskCategories;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.tasks.AddTaskActivity;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskCategoryViewModel;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddTaskCategoryActivity extends AppCompatActivity {

    private TextInputEditText categoryNameEditText, categoryDescriptionEditText;
    private Button selectColorButton, saveCategoryButton;
    private int selectedColor = Color.parseColor("#5C4033");

    private TaskCategoryViewModel categoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task_category);

        categoryNameEditText = findViewById(R.id.categoryNameEditText);
        categoryDescriptionEditText = findViewById(R.id.categoryDescriptionEditText);
        selectColorButton = findViewById(R.id.selectColorButton);
        saveCategoryButton = findViewById(R.id.saveCategoryButton);

        selectColorButton.setBackgroundColor(selectedColor);

        categoryViewModel = new ViewModelProvider(this).get(TaskCategoryViewModel.class);

        selectColorButton.setOnClickListener(v -> openColorPicker());
        saveCategoryButton.setOnClickListener(v -> addCategory());
    }

    private void openColorPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final var view = inflater.inflate(R.layout.dialog_rgb_picker, null);

        SeekBar redSeek = view.findViewById(R.id.seekRed);
        SeekBar greenSeek = view.findViewById(R.id.seekGreen);
        SeekBar blueSeek = view.findViewById(R.id.seekBlue);
        TextView colorPreview = view.findViewById(R.id.colorPreview);

        redSeek.setProgress(Color.red(selectedColor));
        greenSeek.setProgress(Color.green(selectedColor));
        blueSeek.setProgress(Color.blue(selectedColor));
        colorPreview.setBackgroundColor(selectedColor);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedColor = android.graphics.Color.rgb(redSeek.getProgress(), greenSeek.getProgress(), blueSeek.getProgress());
                colorPreview.setBackgroundColor(selectedColor);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        redSeek.setOnSeekBarChangeListener(listener);
        greenSeek.setOnSeekBarChangeListener(listener);
        blueSeek.setOnSeekBarChangeListener(listener);

        builder.setView(view)
                .setTitle("Select Color")
                .setPositiveButton("OK", (dialog, which) -> selectColorButton.setBackgroundColor(selectedColor))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addCategory() {
        String name = categoryNameEditText.getText().toString().trim();
        String description = categoryDescriptionEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Category name cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        categoryViewModel.isColorUsed(selectedColor).observe(this, isUsed -> {
            if (isUsed) {
                Toast.makeText(AddTaskCategoryActivity.this, "This Color is Already Used!", Toast.LENGTH_SHORT).show();
            } else {
                User currentUser = categoryViewModel.getCurrentUserLiveData().getValue();

                if (currentUser == null) {
                    Toast.makeText(AddTaskCategoryActivity.this, "You are not logged in!", Toast.LENGTH_SHORT).show();
                    return;
                }

                TaskCategory category = new TaskCategory(name, description, selectedColor, currentUser.getId());
                categoryViewModel.insertCategory(category);
                Toast.makeText(AddTaskCategoryActivity.this, "Category saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
