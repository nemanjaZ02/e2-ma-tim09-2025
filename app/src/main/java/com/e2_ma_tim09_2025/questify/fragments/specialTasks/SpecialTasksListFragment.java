package com.e2_ma_tim09_2025.questify.fragments.specialTasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.specialTasks.SpecialTasksAdapter;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.SpecialTask;
import com.e2_ma_tim09_2025.questify.viewmodels.SpecialTasksViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SpecialTasksListFragment extends Fragment {

    private SpecialTasksViewModel viewModel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView noTasksText;
    private SpecialTasksAdapter adapter;
    private Handler timeUpdateHandler;
    private Runnable timeUpdateRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_special_tasks_list, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupTimeUpdate();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewSpecialTasks);
        progressBar = view.findViewById(R.id.progressBar);
        noTasksText = view.findViewById(R.id.textViewNoTasks);
    }

    private void setupRecyclerView() {
        adapter = new SpecialTasksAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SpecialTasksViewModel.class);
        
        // Observe special tasks
        viewModel.getSpecialTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                adapter.setSpecialTasks(tasks);
                updateUI(tasks);
            }
        });
        
        // Observe special mission
        viewModel.getSpecialMission().observe(getViewLifecycleOwner(), mission -> {
            if (mission != null) {
                adapter.setSpecialMission(mission);
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
        
        // Observe current user and load special tasks when user is loaded
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                viewModel.loadSpecialTasks();
            }
        });
    }

    private void updateUI(List<SpecialTask> tasks) {
        if (tasks.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noTasksText.setVisibility(View.VISIBLE);
            noTasksText.setText("No special tasks available.\nJoin an alliance and wait for a special mission to start!");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noTasksText.setVisibility(View.GONE);
        }
    }

    private void setupTimeUpdate() {
        timeUpdateHandler = new Handler(Looper.getMainLooper());
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Ažuriraj adapter da refreshuje vreme
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                // Ponovi za 30 sekundi
                timeUpdateHandler.postDelayed(this, 30000); // 30 sekundi
            }
        };
        // Pokreni timer
        timeUpdateHandler.post(timeUpdateRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refreshuj podatke kada se korisnik vrati na ekran
        if (viewModel != null) {
            viewModel.loadSpecialTasks();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Zaustavi timer kada se fragment uništi
        if (timeUpdateHandler != null && timeUpdateRunnable != null) {
            timeUpdateHandler.removeCallbacks(timeUpdateRunnable);
        }
    }
}
