package com.e2_ma_tim09_2025.questify.adapters.tasks;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TasksCalendarViewAdapter implements MonthDayBinder<TasksCalendarViewAdapter.DayViewContainer> {

    private Map<Integer, Integer> categoryColors;
    private List<Task> tasks;
    private Context context;

    public TasksCalendarViewAdapter(Context context, List<Task> tasks, List<TaskCategory> categories) {
        this.context = context;
        this.tasks = tasks;
        categoryColors = new HashMap<>();
        for (TaskCategory cat : categories) {
            categoryColors.put(cat.getId(), cat.getColor());
        }
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

        if (calendarDay.getPosition() == DayPosition.MonthDate) {
            container.dayText.setTextColor(Color.parseColor("#5C4033"));
            container.dayText.setAlpha(1.0f);
            container.indicatorsLayout.removeAllViews();
            container.indicatorsLayout.setVisibility(View.GONE);

            Set<Integer> colorsForDay = new HashSet<>();

            for (Task task : tasks) {
                LocalDate taskDate = convertTimestampToLocalDate(task.getFinishDate());
                if (taskDate.equals(date)) {
                    Integer color = categoryColors.get(task.getCategoryId());
                    if (color != null) {
                        colorsForDay.add(color);
                    }
                }
            }

            if (!colorsForDay.isEmpty()) {
                container.indicatorsLayout.setVisibility(View.VISIBLE);
                for (Integer color : colorsForDay) {
                    View dot = new View(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
                    params.setMargins(4, 0, 4, 0);
                    dot.setLayoutParams(params);
                    dot.setBackgroundResource(R.drawable.ic_task_indicator);
                    dot.getBackground().setTint(color);
                    container.indicatorsLayout.addView(dot);
                }
            }

        } else {
            container.dayText.setTextColor(Color.LTGRAY);
            container.dayText.setAlpha(0.5f);
            container.indicatorsLayout.setVisibility(View.GONE);
        }
    }

    public static class DayViewContainer extends ViewContainer {
        public TextView dayText;
        public LinearLayout indicatorsLayout;

        public DayViewContainer(View view) {
            super(view);
            dayText = view.findViewById(R.id.calendar_day_text);
            indicatorsLayout = view.findViewById(R.id.calendar_day_task_indicators);
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
