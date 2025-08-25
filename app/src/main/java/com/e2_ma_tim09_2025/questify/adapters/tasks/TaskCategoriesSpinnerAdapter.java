package com.e2_ma_tim09_2025.questify.adapters.tasks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.e2_ma_tim09_2025.questify.models.TaskCategory;

import java.util.List;

public class TaskCategoriesSpinnerAdapter extends ArrayAdapter<TaskCategory> {

    public TaskCategoriesSpinnerAdapter(@NonNull Context context, @NonNull List<TaskCategory> categories) {
        super(context, android.R.layout.simple_spinner_item, categories);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        TaskCategory category = getItem(position);
        if (category != null) {
            label.setText(category.getName());
            label.setTextColor(category.getColor());
        }
        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        TaskCategory category = getItem(position);
        if (category != null) {
            label.setText(category.getName());
            label.setTextColor(category.getColor());
        }
        return label;
    }
}
