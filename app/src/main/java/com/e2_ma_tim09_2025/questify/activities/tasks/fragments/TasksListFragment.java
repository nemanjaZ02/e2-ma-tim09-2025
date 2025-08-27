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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.tasks.TasksRecyclerViewAdapter;
import com.e2_ma_tim09_2025.questify.viewmodels.TaskViewModel;

@dagger.hilt.android.AndroidEntryPoint
public class TasksListFragment extends Fragment {

    private static final String TAG = "TasksListFragment";
    private TaskViewModel taskViewModel;
    private TasksRecyclerViewAdapter taskAdapter;

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
        taskAdapter = new TasksRecyclerViewAdapter();
        recyclerView.setAdapter(taskAdapter);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            Log.d(TAG, "Task list updated! Total tasks: " + tasks.size());
            taskAdapter.setTasks(tasks);
        });
    }
}
