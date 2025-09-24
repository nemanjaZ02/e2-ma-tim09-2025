package com.e2_ma_tim09_2025.questify.activities.taskCategories;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskCategoryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TaskCategoryDetailsActivity extends AppCompatActivity {

    private TaskCategoryViewModel categoryViewModel;
    private TaskCategory currentCategory;

    private TextInputEditText categoryNameEditText, categoryDescriptionEditText;
    private MaterialButton selectColorButton;
    private ImageButton editNameButton, editDescriptionButton, editColorButton;

    private LinearLayout categoryNameEditButtonsLayout, categoryDescriptionEditButtonsLayout, categoryColorEditButtonsLayout;
    private MaterialButton acceptCategoryNameEditButton, cancelCategoryNameEditButton;
    private MaterialButton acceptCategoryDescriptionEditButton, cancelCategoryDescriptionEditButton;
    private MaterialButton acceptCategoryColorEditButton, cancelCategoryColorEditButton;
    private MaterialButton deleteButton;

    private int selectedColor;
    private int backupColor;
    private String backupName, backupDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_category_details);

        categoryViewModel = new ViewModelProvider(this).get(TaskCategoryViewModel.class);

        categoryNameEditText = findViewById(R.id.categoryNameEditText);
        categoryDescriptionEditText = findViewById(R.id.categoryDescriptionEditText);

        selectColorButton = findViewById(R.id.selectColorButton);
        editNameButton = findViewById(R.id.editCategoryNameButton);
        editDescriptionButton = findViewById(R.id.editCategoryDescriptionButton);
        editColorButton = findViewById(R.id.editCategoryColorButton);

        categoryNameEditButtonsLayout = findViewById(R.id.categoryNameEditButtonsLayout);
        categoryDescriptionEditButtonsLayout = findViewById(R.id.categoryDescriptionEditButtonsLayout);
        categoryColorEditButtonsLayout = findViewById(R.id.categoryColorEditButtonsLayout);

        acceptCategoryNameEditButton = findViewById(R.id.acceptCategoryNameEditButton);
        cancelCategoryNameEditButton = findViewById(R.id.cancelCategoryNameEditButton);

        acceptCategoryDescriptionEditButton = findViewById(R.id.acceptCategoryDescriptionEditButton);
        cancelCategoryDescriptionEditButton = findViewById(R.id.cancelCategoryDescriptionEditButton);

        acceptCategoryColorEditButton = findViewById(R.id.acceptCategoryColorEditButton);
        cancelCategoryColorEditButton = findViewById(R.id.cancelCategoryColorEditButton);

        deleteButton = findViewById(R.id.delete_category_button);

        int categoryId = getIntent().getIntExtra("categoryId", -1);
        categoryViewModel.getCategoryById(categoryId).observe(this, category -> {
            if (category != null) {
                currentCategory = category;
                populateUI(category);
            }
        });

        editNameButton.setOnClickListener(v -> startNameEditing());
        editDescriptionButton.setOnClickListener(v -> startDescriptionEditing());
        editColorButton.setOnClickListener(v -> startColorEditing());

        acceptCategoryNameEditButton.setOnClickListener(v -> saveNameEdits());
        cancelCategoryNameEditButton.setOnClickListener(v -> cancelNameEdits());

        acceptCategoryDescriptionEditButton.setOnClickListener(v -> saveDescriptionEdits());
        cancelCategoryDescriptionEditButton.setOnClickListener(v -> cancelDescriptionEdits());

        acceptCategoryColorEditButton.setOnClickListener(v -> saveColorEdits());
        cancelCategoryColorEditButton.setOnClickListener(v -> cancelColorEdits());

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void populateUI(TaskCategory category) {
        categoryNameEditText.setText(category.getName());
        categoryDescriptionEditText.setText(category.getDescription());
        selectedColor = category.getColor();
        selectColorButton.setBackgroundColor(selectedColor);

        stopEditingMode();
    }

    private void startEditingMode() {
        editNameButton.setVisibility(View.GONE);
        editDescriptionButton.setVisibility(View.GONE);
        editColorButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
    }

    private void stopEditingMode() {
        editNameButton.setVisibility(View.VISIBLE);
        editDescriptionButton.setVisibility(View.VISIBLE);
        editColorButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
        categoryNameEditButtonsLayout.setVisibility(View.GONE);
        categoryDescriptionEditButtonsLayout.setVisibility(View.GONE);
        categoryColorEditButtonsLayout.setVisibility(View.GONE);
        categoryNameEditText.setEnabled(false);
        categoryDescriptionEditText.setEnabled(false);
        selectColorButton.setEnabled(true);
    }

    private void startNameEditing() {
        startEditingMode();
        backupName = categoryNameEditText.getText().toString();
        categoryNameEditText.setEnabled(true);
        categoryNameEditText.requestFocus();
        categoryNameEditButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void saveNameEdits() {
        if (currentCategory != null) {
            String newName = categoryNameEditText.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Category name cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            currentCategory.setName(newName);
            categoryViewModel.updateCategory(currentCategory);
            Toast.makeText(this, "Category name updated!", Toast.LENGTH_SHORT).show();
        }
        stopEditingMode();
    }

    private void cancelNameEdits() {
        categoryNameEditText.setText(backupName);
        stopEditingMode();
    }

    private void startDescriptionEditing() {
        startEditingMode();
        backupDescription = categoryDescriptionEditText.getText().toString();
        categoryDescriptionEditText.setEnabled(true);
        categoryDescriptionEditText.requestFocus();
        categoryDescriptionEditButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void saveDescriptionEdits() {
        if (currentCategory != null) {
            currentCategory.setDescription(categoryDescriptionEditText.getText().toString().trim());
            categoryViewModel.updateCategory(currentCategory);
            Toast.makeText(this, "Category description updated!", Toast.LENGTH_SHORT).show();
        }
        stopEditingMode();
    }

    private void cancelDescriptionEdits() {
        categoryDescriptionEditText.setText(backupDescription);
        stopEditingMode();
    }

    private void startColorEditing() {
        startEditingMode();
        backupColor = selectedColor;
        openColorPicker();
        categoryColorEditButtonsLayout.setVisibility(View.VISIBLE);
    }

    private void saveColorEdits() {
        if (currentCategory != null) {
            if (selectedColor == currentCategory.getColor()) {
                currentCategory.setColor(selectedColor);
                categoryViewModel.updateCategory(currentCategory);
                Toast.makeText(this, "Color updated!", Toast.LENGTH_SHORT).show();
                stopEditingMode();
                return;
            }

            categoryViewModel.isColorUsed(selectedColor).observe(this, isUsed -> {
                if (isUsed) {
                    Toast.makeText(TaskCategoryDetailsActivity.this, "This Color is Already Used!", Toast.LENGTH_SHORT).show();
                } else {
                    currentCategory.setColor(selectedColor);
                    categoryViewModel.updateCategory(currentCategory);
                    Toast.makeText(TaskCategoryDetailsActivity.this, "Color updated!", Toast.LENGTH_SHORT).show();
                    stopEditingMode();
                }
            });
        }
    }

    private void cancelColorEdits() {
        selectedColor = backupColor;
        selectColorButton.setBackgroundColor(selectedColor);
        stopEditingMode();
    }

    private void openColorPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_rgb_picker, null);

        SeekBar redSeek = view.findViewById(R.id.seekRed);
        SeekBar greenSeek = view.findViewById(R.id.seekGreen);
        SeekBar blueSeek = view.findViewById(R.id.seekBlue);
        View colorPreview = view.findViewById(R.id.colorPreview);

        redSeek.setProgress(Color.red(selectedColor));
        greenSeek.setProgress(Color.green(selectedColor));
        blueSeek.setProgress(Color.blue(selectedColor));
        colorPreview.setBackgroundColor(selectedColor);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedColor = Color.rgb(redSeek.getProgress(), greenSeek.getProgress(), blueSeek.getProgress());
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
                .setPositiveButton("OK", (dialog, which) -> {
                    selectColorButton.setBackgroundColor(selectedColor);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    selectedColor = backupColor;
                    selectColorButton.setBackgroundColor(selectedColor);
                })
                .show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    categoryViewModel.deleteCategory(currentCategory);
                    Toast.makeText(this, "Category deleted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}