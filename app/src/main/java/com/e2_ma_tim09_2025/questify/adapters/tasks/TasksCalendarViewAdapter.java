package com.e2_ma_tim09_2025.questify.adapters.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class TasksCalendarViewAdapter implements MonthDayBinder<TasksCalendarViewAdapter.DayViewContainer> {

    private List<Task> tasks;

    public TasksCalendarViewAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public DayViewContainer create(@NonNull View view) {
        return new DayViewContainer(view);
    }

    @Override
    public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay calendarDay) {
        LocalDate date = calendarDay.getDate();
        container.dayText.setText(String.valueOf(date.getDayOfMonth()));

        container.taskIndicator.setVisibility(View.GONE);

        for (Task task : tasks) {
            LocalDate taskDate = convertTimestampToLocalDate(task.getFinishDate());
            if (taskDate.equals(date)) {
                container.taskIndicator.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    public static class DayViewContainer extends ViewContainer {
        public TextView dayText;
        public View taskIndicator;

        public DayViewContainer(View view) {
            super(view);
            dayText = view.findViewById(R.id.calendar_day_text);
            taskIndicator = view.findViewById(R.id.calendar_day_task_indicator);
        }
    }

    private LocalDate convertTimestampToLocalDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
