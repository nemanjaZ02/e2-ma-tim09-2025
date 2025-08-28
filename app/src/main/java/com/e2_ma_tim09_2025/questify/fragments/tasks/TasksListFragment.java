package com.e2_ma_tim09_2025.questify.fragments.tasks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TasksRecyclerViewAdapter;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

@dagger.hilt.android.AndroidEntryPoint
public class TasksListFragment extends Fragment {

    private static final String TAG = "TasksListFragment";
    private TaskViewModel taskViewModel;
    private TasksRecyclerViewAdapter taskAdapter;

    private final android.os.Handler uiHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable updateUiRunnable = new Runnable() {
        @Override
        public void run() {
            if (taskAdapter != null) {
                taskAdapter.notifyDataSetChanged();
            }
            uiHandler.postDelayed(this, 60_000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskAdapter = new TasksRecyclerViewAdapter();
        recyclerView.setAdapter(taskAdapter);

        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            Log.d(TAG, "Categories loaded. Updating adapter. Total: " + categories.size());
            taskAdapter.setTaskCategories(categories);
        });

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            Log.d(TAG, "Task list updated! Total tasks: " + tasks.size());
            taskAdapter.setTasks(tasks);
            taskAdapter.notifyDataSetChanged();
        });
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
