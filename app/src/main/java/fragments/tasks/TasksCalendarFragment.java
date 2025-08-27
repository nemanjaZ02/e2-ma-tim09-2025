package fragments.tasks;

import android.os.Bundle;
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

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TasksCalendarViewAdapter;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;
import com.kizitonwose.calendar.view.CalendarView;

import java.time.YearMonth;
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

        calendarAdapter = new TasksCalendarViewAdapter(
                taskViewModel.getTasks().getValue() != null
                        ? taskViewModel.getTasks().getValue()
                        : new ArrayList<>()
        );

        YearMonth currentMonth = YearMonth.now();
        calendarView.setup(
                currentMonth.minusMonths(12),
                currentMonth.plusMonths(12),
                java.time.DayOfWeek.MONDAY
        );
        calendarView.scrollToMonth(currentMonth);

        calendarView.setDayBinder(calendarAdapter);

        calendarView.setMonthScrollListener(calendarMonth -> {
            YearMonth visibleMonth = calendarMonth.getYearMonth();
            String monthYear = visibleMonth.getMonth().name().substring(0,1)
                    + visibleMonth.getMonth().name().substring(1).toLowerCase()
                    + " " + visibleMonth.getYear();
            monthYearText.setText(monthYear);
            return null;
        });

        prevMonthButton.setOnClickListener(v -> {
            calendarView.smoothScrollToMonth(calendarView.findFirstVisibleMonth().getYearMonth().minusMonths(1));
        });

        nextMonthButton.setOnClickListener(v -> {
            calendarView.smoothScrollToMonth(calendarView.findFirstVisibleMonth().getYearMonth().plusMonths(1));
        });

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            Log.d(TAG, "Calendar updated! Tasks: " + tasks.size());
            calendarAdapter.setTasks(tasks);
            calendarView.notifyCalendarChanged();
        });
    }
}