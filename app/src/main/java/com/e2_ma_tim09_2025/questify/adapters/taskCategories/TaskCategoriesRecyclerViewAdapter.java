package com.e2_ma_tim09_2025.questify.adapters.taskCategories;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;

import java.util.List;

public class TaskCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<TaskCategoriesRecyclerViewAdapter.TaskCategoryViewHolder> {

    private List<TaskCategory> categories;

    public void setCategories(List<TaskCategory> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_category, parent, false);
        return new TaskCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskCategoryViewHolder holder, int position) {
        TaskCategory category = categories.get(position);
        holder.categoryName.setText(category.getName());
        holder.categoryDescription.setText(category.getDescription());

        Drawable indicatorDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_task_indicator);
        if (indicatorDrawable != null) {
            indicatorDrawable.setColorFilter(category.getColor(), PorterDuff.Mode.SRC_IN);
            holder.categoryColorIndicator.setImageDrawable(indicatorDrawable);
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public static class TaskCategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView categoryName, categoryDescription;
        public ImageView categoryColorIndicator;

        public TaskCategoryViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryDescription = itemView.findViewById(R.id.category_description);
            categoryColorIndicator = itemView.findViewById(R.id.category_color_indicator);
        }
    }
}
