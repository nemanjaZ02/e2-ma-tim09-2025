package com.e2_ma_tim09_2025.questify.fragments.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.e2_ma_tim09_2025.questify.activities.tasks.TaskDetailsActivity;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TasksRecyclerViewAdapter;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TasksListFragment extends Fragment {

    private TaskViewModel taskViewModel;
    private TasksRecyclerViewAdapter taskAdapter;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateUiRunnable = new Runnable() {
        @Override
        public void run() {
            if (taskAdapter != null) taskAdapter.notifyDataSetChanged();
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

        taskAdapter.setOnTaskClickListener(task -> {
            Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
            intent.putExtra("taskId", task.getId());
            startActivity(intent);
        });

        taskViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> taskAdapter.setTaskCategories(categories));
        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> taskAdapter.setTasks(tasks));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (taskViewModel != null) taskViewModel.startStatusUpdater();
        uiHandler.post(updateUiRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (taskViewModel != null) taskViewModel.stopStatusUpdater();
        uiHandler.removeCallbacks(updateUiRunnable);
    }
}
