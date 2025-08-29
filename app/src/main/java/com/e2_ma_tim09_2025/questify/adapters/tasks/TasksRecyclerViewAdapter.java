package com.e2_ma_tim09_2025.questify.adapters.tasks;

import android.graphics.Color;
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
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TasksRecyclerViewAdapter extends RecyclerView.Adapter<TasksRecyclerViewAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private final Map<Integer, Integer> categoryColors;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public TasksRecyclerViewAdapter() {
        this.categoryColors = new HashMap<>();
    }

    public void setTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    public void setTaskCategories(List<TaskCategory> categories) {
        this.categoryColors.clear();
        for (TaskCategory cat : categories) {
            this.categoryColors.put(cat.getId(), cat.getColor());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskName.setText(task.getName());
        holder.taskDescription.setText(task.getDescription());

        Integer color = categoryColors.get(task.getCategoryId());
        if (color != null) {
            Drawable indicatorDrawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_task_indicator);
            if (indicatorDrawable != null) {
                indicatorDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                holder.categoryColorIndicator.setImageDrawable(indicatorDrawable);
            }
        }

        switch (task.getStatus()) {
            case ACTIVE:
                holder.taskStatusIndicator.setVisibility(View.GONE);
                holder.taskTimeRemaining.setVisibility(View.VISIBLE);
                long remainingTime = task.getFinishDate() - System.currentTimeMillis();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime);
                long hours = TimeUnit.MILLISECONDS.toHours(remainingTime);
                long days = TimeUnit.MILLISECONDS.toDays(remainingTime);
                long weeks = days / 7;
                long months = days / 30;
                String timeText;
                if (months > 0) timeText = months + " month" + (months > 1 ? "s" : "") + " remaining";
                else if (weeks > 0) timeText = weeks + " week" + (weeks > 1 ? "s" : "") + " remaining";
                else if (days > 0) timeText = days + " day" + (days > 1 ? "s" : "") + " remaining";
                else if (hours > 0) timeText = hours + " hour" + (hours > 1 ? "s" : "") + " remaining";
                else timeText = minutes + " minute" + (minutes > 1 ? "s" : "") + " remaining";
                holder.taskTimeRemaining.setText(timeText);
                break;
            case COMPLETED:
                holder.taskStatusIndicator.setVisibility(View.VISIBLE);
                holder.taskTimeRemaining.setVisibility(View.GONE);
                holder.taskStatusIndicator.setImageResource(R.drawable.ic_task_status_completed);
                holder.taskStatusIndicator.setColorFilter(Color.parseColor("#4CAF50"), PorterDuff.Mode.SRC_IN);
                break;
            case NOT_COMPLETED:
                holder.taskStatusIndicator.setVisibility(View.VISIBLE);
                holder.taskTimeRemaining.setVisibility(View.GONE);
                holder.taskStatusIndicator.setImageResource(R.drawable.ic_task_status_notcompleted);
                holder.taskStatusIndicator.setColorFilter(Color.parseColor("#D32F2F"), PorterDuff.Mode.SRC_IN);
                break;
            case PAUSED:
                holder.taskStatusIndicator.setVisibility(View.VISIBLE);
                holder.taskTimeRemaining.setVisibility(View.GONE);
                holder.taskStatusIndicator.setImageResource(R.drawable.ic_task_status_paused);
                holder.taskStatusIndicator.setColorFilter(Color.parseColor("#FFC107"), PorterDuff.Mode.SRC_IN);
                break;
            case CANCELLED:
                holder.taskStatusIndicator.setVisibility(View.VISIBLE);
                holder.taskTimeRemaining.setVisibility(View.GONE);
                holder.taskStatusIndicator.setImageResource(R.drawable.ic_task_status_cancelled);
                holder.taskStatusIndicator.setColorFilter(Color.parseColor("#757575"), PorterDuff.Mode.SRC_IN);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView taskName, taskDescription, taskTimeRemaining;
        public ImageView categoryColorIndicator, taskStatusIndicator;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.task_name);
            taskDescription = itemView.findViewById(R.id.task_description);
            taskTimeRemaining = itemView.findViewById(R.id.task_time_remaining);
            categoryColorIndicator = itemView.findViewById(R.id.category_color_indicator);
            taskStatusIndicator = itemView.findViewById(R.id.task_status_indicator);
        }
    }
}
