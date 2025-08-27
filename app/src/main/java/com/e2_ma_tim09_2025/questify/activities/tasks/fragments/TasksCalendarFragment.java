package com.e2_ma_tim09_2025.questify.activities.tasks.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TasksCalendarViewAdapter;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.kizitonwose.calendar.view.CalendarView;

import java.time.YearMonth;
import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksCalendarFragment extends Fragment {

    private static final String TAG = "TasksCalendarFragment";
    private TaskViewModel taskViewModel;
    private TasksCalendarViewAdapter calendarAdapter;
    private CalendarView calendarView;

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
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        calendarAdapter = new TasksCalendarViewAdapter(
                taskViewModel.getTasks().getValue() != null
                        ? taskViewModel.getTasks().getValue()
                        : new ArrayList<>()
        );

        calendarView.setDayBinder(calendarAdapter);
        calendarView.setup(
                java.time.YearMonth.now().minusMonths(12),
                java.time.YearMonth.now().plusMonths(12),
                java.time.DayOfWeek.MONDAY
        );

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            Log.d(TAG, "Calendar updated! Tasks: " + tasks.size());
            calendarAdapter.setTasks(tasks);
            calendarView.notifyCalendarChanged();
        });
    }
}
