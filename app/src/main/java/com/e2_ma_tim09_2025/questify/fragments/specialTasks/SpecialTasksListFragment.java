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
    private RecyclerView recyclerViewMyAlliance;
    private RecyclerView recyclerViewMemberAlliance;
    private ProgressBar progressBar;
    private TextView noTasksText;
    private TextView myAllianceTitle;
    private TextView memberAllianceTitle;
    private SpecialTasksAdapter myAllianceAdapter;
    private SpecialTasksAdapter memberAllianceAdapter;
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
        recyclerViewMyAlliance = view.findViewById(R.id.recyclerViewMyAllianceTasks);
        recyclerViewMemberAlliance = view.findViewById(R.id.recyclerViewMemberAllianceTasks);
        progressBar = view.findViewById(R.id.progressBar);
        noTasksText = view.findViewById(R.id.textViewNoTasks);
        myAllianceTitle = view.findViewById(R.id.textViewMyAllianceTitle);
        memberAllianceTitle = view.findViewById(R.id.textViewMemberAllianceTitle);
    }

    private void setupRecyclerView() {
        // My Alliance RecyclerView
        myAllianceAdapter = new SpecialTasksAdapter(new ArrayList<>());
        recyclerViewMyAlliance.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMyAlliance.setAdapter(myAllianceAdapter);
        
        // Member Alliance RecyclerView
        memberAllianceAdapter = new SpecialTasksAdapter(new ArrayList<>());
        recyclerViewMemberAlliance.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMemberAlliance.setAdapter(memberAllianceAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SpecialTasksViewModel.class);
        
        // Observe My Alliance tasks
        viewModel.getMyAllianceTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                myAllianceAdapter.setSpecialTasks(tasks);
                updateMyAllianceUI(tasks);
            }
        });
        
        // Observe Member Alliance tasks
        viewModel.getMemberAllianceTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                memberAllianceAdapter.setSpecialTasks(tasks);
                updateMemberAllianceUI(tasks);
            }
        });
        
        // Observe My Alliance special mission
        viewModel.getMyAllianceSpecialMission().observe(getViewLifecycleOwner(), mission -> {
            if (mission != null) {
                myAllianceAdapter.setSpecialMission(mission);
            }
        });
        
        // Observe Member Alliance special mission
        viewModel.getMemberAllianceSpecialMission().observe(getViewLifecycleOwner(), mission -> {
            if (mission != null) {
                memberAllianceAdapter.setSpecialMission(mission);
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
                viewModel.loadAllSpecialTasks();
            }
        });
    }

    private void updateMyAllianceUI(List<SpecialTask> tasks) {
        if (tasks.isEmpty()) {
            recyclerViewMyAlliance.setVisibility(View.GONE);
            myAllianceTitle.setVisibility(View.GONE);
        } else {
            recyclerViewMyAlliance.setVisibility(View.VISIBLE);
            myAllianceTitle.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateMemberAllianceUI(List<SpecialTask> tasks) {
        if (tasks.isEmpty()) {
            recyclerViewMemberAlliance.setVisibility(View.GONE);
            memberAllianceTitle.setVisibility(View.GONE);
        } else {
            recyclerViewMemberAlliance.setVisibility(View.VISIBLE);
            memberAllianceTitle.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateUI(List<SpecialTask> tasks) {
        if (tasks.isEmpty()) {
            recyclerViewMyAlliance.setVisibility(View.GONE);
            recyclerViewMemberAlliance.setVisibility(View.GONE);
            myAllianceTitle.setVisibility(View.GONE);
            memberAllianceTitle.setVisibility(View.GONE);
            noTasksText.setVisibility(View.VISIBLE);
        } else {
            noTasksText.setVisibility(View.GONE);
        }
    }

    private void setupTimeUpdate() {
        timeUpdateHandler = new Handler(Looper.getMainLooper());
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Ažuriraj adaptere da refreshuju vreme
                if (myAllianceAdapter != null) {
                    myAllianceAdapter.notifyDataSetChanged();
                }
                if (memberAllianceAdapter != null) {
                    memberAllianceAdapter.notifyDataSetChanged();
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
        // onResume je uklonjen jer se loadSpecialTasks() poziva u observer-u
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
