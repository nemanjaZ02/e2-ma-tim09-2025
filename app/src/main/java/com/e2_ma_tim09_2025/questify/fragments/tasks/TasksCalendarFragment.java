package com.e2_ma_tim09_2025.questify.fragments.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.tasks.TaskDetailsActivity;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TasksCalendarViewAdapter;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TasksRecyclerViewAdapter;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.kizitonwose.calendar.view.CalendarView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksCalendarFragment extends Fragment {

    private static final String TAG = "TasksCalendarFragment";
    private TaskViewModel taskViewModel;
    private TasksCalendarViewAdapter calendarAdapter;
    private CalendarView calendarView;
    private TextView monthYearText;
    private ImageButton prevMonthButton;
    private ImageButton nextMonthButton;
    private RecyclerView tasksMonthRecyclerView;
    private TasksRecyclerViewAdapter tasksAdapter;
    private YearMonth currentMonth;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateUiRunnable = new Runnable() {
        @Override
        public void run() {
            if (tasksAdapter != null) {
                tasksAdapter.notifyDataSetChanged();
            }
            if (calendarView != null) {
                calendarView.notifyCalendarChanged();
            }
            uiHandler.postDelayed(this, 60_000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        monthYearText = view.findViewById(R.id.monthYearText);
        prevMonthButton = view.findViewById(R.id.prevMonthButton);
        nextMonthButton = view.findViewById(R.id.nextMonthButton);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        tasksMonthRecyclerView = view.findViewById(R.id.tasksMonthRecyclerView);
        tasksAdapter = new TasksRecyclerViewAdapter();
        tasksAdapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
            intent.putExtra("taskId", task.getId());
            startActivity(intent);
        });

        tasksMonthRecyclerView.setAdapter(tasksAdapter);
        tasksMonthRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        calendarAdapter = new TasksCalendarViewAdapter(
                getContext(),
                taskViewModel.getTasks().getValue() != null
                        ? taskViewModel.getTasks().getValue()
                        : new ArrayList<>(),
                taskViewModel.getCategories().getValue() != null
                        ? taskViewModel.getCategories().getValue()
                        : new ArrayList<>()
        );

        currentMonth = YearMonth.now();
        calendarView.setup(
                currentMonth.minusMonths(12),
                currentMonth.plusMonths(12),
                java.time.DayOfWeek.MONDAY
        );
        calendarView.scrollToMonth(currentMonth);
        calendarView.setDayBinder(calendarAdapter);

        calendarView.setMonthScrollListener(calendarMonth -> {
            currentMonth = calendarMonth.getYearMonth();
            String monthText = currentMonth.getMonth().name().substring(0, 1)
                    + currentMonth.getMonth().name().substring(1).toLowerCase()
                    + " " + currentMonth.getYear();
            monthYearText.setText(monthText);
            updateTasksForMonth(currentMonth);
            return null;
        });

        prevMonthButton.setOnClickListener(v -> {
            YearMonth prev = currentMonth.minusMonths(1);
            calendarView.smoothScrollToMonth(prev);
        });

        nextMonthButton.setOnClickListener(v -> {
            YearMonth next = currentMonth.plusMonths(1);
            calendarView.smoothScrollToMonth(next);
        });

        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            Log.d(TAG, "Categories loaded. Updating adapter. Total: " + categories.size());
            tasksAdapter.setTaskCategories(categories);
        });

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            calendarAdapter.setTasks(tasks);
            updateTasksForMonth(currentMonth);

            calendarView.notifyCalendarChanged();
            tasksAdapter.notifyDataSetChanged();
            Log.d(TAG, "Calendar updated! Tasks: " + tasks.size());
        });
    }

    private void updateTasksForMonth(YearMonth month) {
        List<Task> allTasks = taskViewModel.getTasks().getValue();
        if (allTasks != null) {
            List<Task> monthTasks = filterTasksForMonth(allTasks, month);
            tasksAdapter.setTasks(monthTasks);
        }
    }

    private List<Task> filterTasksForMonth(List<Task> allTasks, YearMonth month) {
        List<Task> result = new ArrayList<>();
        for (Task task : allTasks) {
            LocalDate date = Instant.ofEpochMilli(task.getFinishDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (YearMonth.from(date).equals(month)) {
                result.add(task);
            }
        }
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (taskViewModel != null) {
            taskViewModel.startStatusUpdater();
        }
        uiHandler.post(updateUiRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (taskViewModel != null) {
            taskViewModel.stopStatusUpdater();
        }
        uiHandler.removeCallbacks(updateUiRunnable);
    }
}
