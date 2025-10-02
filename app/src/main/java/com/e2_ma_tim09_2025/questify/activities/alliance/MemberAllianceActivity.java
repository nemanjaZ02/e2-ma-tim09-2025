package com.e2_ma_tim09_2025.questify.activities.alliance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.alliance.AllianceMembersAdapter;
import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.viewmodels.MemberAllianceViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MemberAllianceActivity extends AppCompatActivity {
    
    private MemberAllianceViewModel viewModel;
    private AllianceMembersAdapter membersAdapter;
    
    private TextView allianceNameText;
    private TextView leaderNameText;
    private TextView membersCountText;
    private TextView missionStatusText;
    private TextView noAllianceText;
    private RecyclerView membersRecyclerView;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_alliance);
        
        initViews();
        setupRecyclerView();
        setupViewModel();
        
        // Load user's alliance
        viewModel.loadUserAlliance();
    }
    
    private void initViews() {
        allianceNameText = findViewById(R.id.textViewAllianceName);
        leaderNameText = findViewById(R.id.textViewLeaderName);
        membersCountText = findViewById(R.id.textViewMembersCount);
        missionStatusText = findViewById(R.id.textViewMissionStatus);
        noAllianceText = findViewById(R.id.textViewNoAlliance);
        membersRecyclerView = findViewById(R.id.recyclerViewMembers);
        progressBar = findViewById(R.id.progressBar);
        
        // Back button
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        membersAdapter = new AllianceMembersAdapter(new ArrayList<>());
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setAdapter(membersAdapter);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MemberAllianceViewModel.class);
        
        // Observe alliance
        viewModel.getAlliance().observe(this, alliance -> {
            if (alliance != null) {
                displayAlliance(alliance);
            } else {
                displayNoAlliance();
            }
        });
        
        // Observe leader
        viewModel.getLeader().observe(this, leader -> {
            if (leader != null) {
                leaderNameText.setText("Leader: " + leader.getUsername());
            }
        });
        
        // Observe members
        viewModel.getMembers().observe(this, members -> {
            if (members != null) {
                membersAdapter.setMembers(members);
                updateMembersCount(members.size());
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                // Show error as no alliance message
                noAllianceText.setText(errorMessage);
                displayNoAlliance();
                viewModel.clearError();
            }
        });
    }
    
    private void displayAlliance(Alliance alliance) {
        allianceNameText.setText(alliance.getName());
        allianceNameText.setVisibility(View.VISIBLE);
        leaderNameText.setVisibility(View.VISIBLE);
        membersCountText.setVisibility(View.VISIBLE);
        missionStatusText.setVisibility(View.VISIBLE);
        membersRecyclerView.setVisibility(View.VISIBLE);
        noAllianceText.setVisibility(View.GONE);
        
        // Update mission status
        updateMissionStatus(alliance.isMissionStarted());
    }
    
    private void displayNoAlliance() {
        allianceNameText.setVisibility(View.GONE);
        leaderNameText.setVisibility(View.GONE);
        membersCountText.setVisibility(View.GONE);
        missionStatusText.setVisibility(View.GONE);
        membersRecyclerView.setVisibility(View.GONE);
        noAllianceText.setVisibility(View.VISIBLE);
    }
    
    private void updateMembersCount(int count) {
        membersCountText.setText("Members: " + count);
    }
    
    private void updateMissionStatus(boolean isMissionActive) {
        if (isMissionActive) {
            missionStatusText.setText("Mission Status: 🔴 ACTIVE");
            missionStatusText.setTextColor(0xFFD32F2F); // Red color
        } else {
            missionStatusText.setText("Mission Status: 🟢 Inactive");
            missionStatusText.setTextColor(0xFF8B4513); // Brown color
        }
    }
}
