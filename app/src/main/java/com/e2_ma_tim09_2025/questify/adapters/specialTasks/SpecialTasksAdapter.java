package com.e2_ma_tim09_2025.questify.adapters.specialTasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.SpecialTask;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;

import java.util.ArrayList;
import java.util.List;

public class SpecialTasksAdapter extends RecyclerView.Adapter<SpecialTasksAdapter.SpecialTaskViewHolder> {

    private List<SpecialTask> specialTasks;
    private SpecialMission specialMission;

    public SpecialTasksAdapter(List<SpecialTask> specialTasks) {
        this.specialTasks = specialTasks != null ? specialTasks : new ArrayList<>();
    }

    public void setSpecialMission(SpecialMission specialMission) {
        this.specialMission = specialMission;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpecialTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_special_task, parent, false);
        return new SpecialTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialTaskViewHolder holder, int position) {
        SpecialTask task = specialTasks.get(position);
        holder.bind(task, specialMission);
    }

    @Override
    public int getItemCount() {
        return specialTasks.size();
    }

    public void setSpecialTasks(List<SpecialTask> specialTasks) {
        this.specialTasks = specialTasks != null ? specialTasks : new ArrayList<>();
        // Sortiraj taskove - INACTIVE na dno
        sortTasks();
        notifyDataSetChanged();
    }
    
    private void sortTasks() {
        if (specialTasks == null) return;
        
        specialTasks.sort((task1, task2) -> {
            // INACTIVE taskovi idu na dno
            boolean task1Inactive = task1.getStatus().toString().equals("INACTIVE");
            boolean task2Inactive = task2.getStatus().toString().equals("INACTIVE");
            
            if (task1Inactive && !task2Inactive) {
                return 1; // task1 ide na dno
            } else if (!task1Inactive && task2Inactive) {
                return -1; // task2 ide na dno
            } else {
                // Ako su oba INACTIVE ili oba nisu INACTIVE, sortiraj po tipu
                return task1.getTaskType().toString().compareTo(task2.getTaskType().toString());
            }
        });
    }

    static class SpecialTaskViewHolder extends RecyclerView.ViewHolder {
        private TextView taskTitleText;
        private TextView taskDescriptionText;
        private TextView progressText;
        private ProgressBar progressBar;
        private TextView statusText;
        private TextView timeRemainingText;

        public SpecialTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitleText = itemView.findViewById(R.id.textViewTaskTitle);
            taskDescriptionText = itemView.findViewById(R.id.textViewTaskDescription);
            progressText = itemView.findViewById(R.id.textViewProgress);
            progressBar = itemView.findViewById(R.id.progressBarTask);
            statusText = itemView.findViewById(R.id.textViewStatus);
            timeRemainingText = itemView.findViewById(R.id.textViewTimeRemaining);
        }

        public void bind(SpecialTask task, SpecialMission specialMission) {
            // Set task title and description based on type
            String title = getTaskTitle(task.getTaskType());
            String description = getTaskDescription(task.getTaskType());
            
            taskTitleText.setText(title);
            taskDescriptionText.setText(description);
            
            // Set progress
            int currentCount = task.getCurrentCount();
            int maxCount = task.getMaxCount();
            int progress = maxCount > 0 ? (currentCount * 100) / maxCount : 0;
            
            progressText.setText(currentCount + "/" + maxCount);
            progressBar.setProgress(progress);
            
            // Set time remaining (calculate fresh each time)
            if (specialMission != null) {
                long timeRemaining = calculateTimeRemaining(specialMission);
                String timeText = formatTimeRemaining(timeRemaining);
                timeRemainingText.setText(timeText);
                
                // Change color based on time remaining
                if (timeRemaining < 24 * 60 * 60 * 1000L) { // Less than 1 day
                    timeRemainingText.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                } else if (timeRemaining < 3 * 24 * 60 * 60 * 1000L) { // Less than 3 days
                    timeRemainingText.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                } else {
                    timeRemainingText.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                }
            } else {
                timeRemainingText.setText("Unknown");
                timeRemainingText.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }
            
            // Set status
            if (task.getStatus().toString().equals("COMPLETED")) {
                statusText.setText("COMPLETED");
                statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else if (task.getStatus().toString().equals("ACTIVE")) {
                statusText.setText("ACTIVE");
                statusText.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
            } else {
                statusText.setText("INACTIVE");
                statusText.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }
        }

        private String getTaskTitle(SpecialTaskType taskType) {
            switch (taskType) {
                case SHOP_PURCHASE:
                    return "Shop Purchase";
                case BOSS_ATTACK:
                    return "Boss Attack";
                case TASK_COMPLETION_EASY_NORMAL:
                    return "Complete Easy/Normal Tasks";
                case TASK_COMPLETION_OTHER:
                    return "Complete Other Tasks";
                case NO_UNRESOLVED_TASKS:
                    return "No Unresolved Tasks";
                case ALLIANCE_MESSAGE_DAILY:
                    return "Daily Alliance Message";
                default:
                    return "Unknown Task";
            }
        }

        private String getTaskDescription(SpecialTaskType taskType) {
            switch (taskType) {
                case SHOP_PURCHASE:
                    return "Purchase items from the shop (max 5 times)";
                case BOSS_ATTACK:
                    return "Successfully attack regular bosses (max 10 times)";
                case TASK_COMPLETION_EASY_NORMAL:
                    return "Complete very easy, easy, normal or important tasks (max 10 times)";
                case TASK_COMPLETION_OTHER:
                    return "Complete other types of tasks (max 6 times)";
                case NO_UNRESOLVED_TASKS:
                    return "Have no unresolved tasks during the special mission (1 time)";
                case ALLIANCE_MESSAGE_DAILY:
                    return "Send a message in alliance chat (counts per day, max 14 days)";
                default:
                    return "Complete this special task";
            }
        }

        private long calculateTimeRemaining(SpecialMission specialMission) {
            long currentTime = System.currentTimeMillis();
            long endTime = specialMission.getEndTime();
            return Math.max(0, endTime - currentTime);
        }

        private String formatTimeRemaining(long timeRemainingMs) {
            if (timeRemainingMs <= 0) {
                return "Expired";
            }

            long totalSeconds = timeRemainingMs / 1000;
            long days = totalSeconds / (24 * 60 * 60);
            long hours = (totalSeconds % (24 * 60 * 60)) / (60 * 60);
            long minutes = (totalSeconds % (60 * 60)) / 60;

            if (days > 0) {
                return String.format("%dd %dh %dm", days, hours, minutes);
            } else if (hours > 0) {
                return String.format("%dh %dm", hours, minutes);
            } else {
                return String.format("%dm", minutes);
            }
        }
    }
}
