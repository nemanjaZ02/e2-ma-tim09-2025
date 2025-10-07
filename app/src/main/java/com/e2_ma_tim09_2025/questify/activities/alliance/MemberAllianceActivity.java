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
import com.e2_ma_tim09_2025.questify.adapters.alliance.MemberProgressAdapter;
import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.MemberProgress;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialMissionStatus;
import com.e2_ma_tim09_2025.questify.services.SpecialTaskService;
import com.e2_ma_tim09_2025.questify.viewmodels.MemberAllianceViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MemberAllianceActivity extends AppCompatActivity {
    
    private MemberAllianceViewModel viewModel;
    @Inject SpecialTaskService specialTaskService;
    private AllianceMembersAdapter membersAdapter;
    private MemberProgressAdapter memberProgressAdapter;
    
    private TextView allianceNameText;
    private TextView leaderNameText;
    private TextView membersCountText;
    private TextView missionStatusText;
    private TextView noAllianceText;
    private RecyclerView membersRecyclerView;
    private ProgressBar progressBar;
    
    // Special Mission Progress UI
    private View specialMissionProgressSection;
    private TextView textViewBossHealth;
    private ProgressBar progressBarBossHealth;
    private TextView textViewMissionNumber;
    private TextView textViewTimeRemaining;
    private RecyclerView recyclerViewMembersProgress;
    
    private Alliance currentAlliance;
    private android.os.Handler refreshHandler;
    private Runnable refreshRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_alliance);
        
        initViews();
        setupRecyclerView();
        setupViewModel();
        setupRefreshHandler();
        
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
        
        // Special Mission Progress UI
        specialMissionProgressSection = findViewById(R.id.specialMissionProgressSection);
        textViewBossHealth = findViewById(R.id.textViewBossHealth);
        progressBarBossHealth = findViewById(R.id.progressBarBossHealth);
        textViewMissionNumber = findViewById(R.id.textViewMissionNumber);
        textViewTimeRemaining = findViewById(R.id.textViewTimeRemaining);
        recyclerViewMembersProgress = findViewById(R.id.recyclerViewMembersProgress);
        
        // Back button
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        membersAdapter = new AllianceMembersAdapter(new ArrayList<>());
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setAdapter(membersAdapter);
        
        // Members Progress RecyclerView
        memberProgressAdapter = new MemberProgressAdapter();
        recyclerViewMembersProgress.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMembersProgress.setAdapter(memberProgressAdapter);
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
        
        // Observe special mission
        viewModel.getSpecialMission().observe(this, specialMission -> {
            if (specialMission != null) {
                // Hide progress section if mission is inactive or expired
                if (specialMission.getStatus() == SpecialMissionStatus.INACTIVE || 
                    specialMission.getStatus() == SpecialMissionStatus.EXPIRED) {
                    specialMissionProgressSection.setVisibility(View.GONE);
                } else {
                    // Mission is active - show progress
                    updateSpecialMissionProgress(specialMission);
                }
            } else {
                specialMissionProgressSection.setVisibility(View.GONE);
            }
        });
    }
    
    private void displayAlliance(Alliance alliance) {
        currentAlliance = alliance;
        allianceNameText.setText(alliance.getName());
        allianceNameText.setVisibility(View.VISIBLE);
        leaderNameText.setVisibility(View.VISIBLE);
        membersCountText.setVisibility(View.VISIBLE);
        missionStatusText.setVisibility(View.VISIBLE);
        membersRecyclerView.setVisibility(View.VISIBLE);
        noAllianceText.setVisibility(View.GONE);
        
        // Load special mission
        viewModel.loadSpecialMission(alliance.getId());
        
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
    
    private void updateSpecialMissionProgress(SpecialMission specialMission) {
        if (specialMission == null || currentAlliance == null) {
            specialMissionProgressSection.setVisibility(View.GONE);
            return;
        }
        
        // Hide progress section if mission is inactive or expired
        if (specialMission.getStatus() == SpecialMissionStatus.INACTIVE || 
            specialMission.getStatus() == SpecialMissionStatus.EXPIRED) {
            specialMissionProgressSection.setVisibility(View.GONE);
            return;
        }
        
        // Show progress section
        specialMissionProgressSection.setVisibility(View.VISIBLE);
        
        // Update mission number
        if (specialMission.getMissionNumber() == 0) {
            textViewMissionNumber.setText("Mission: Not Started");
        } else {
            textViewMissionNumber.setText("Mission #" + specialMission.getMissionNumber());
        }
        
        // Update time remaining
        long timeRemaining = specialMission.getTimeRemaining();
        String timeText = formatTimeRemaining(timeRemaining);
        textViewTimeRemaining.setText("Time: " + timeText);
        
        // Update boss health
        int currentHealth = specialMission.getBossCurrentHealth();
        int maxHealth = specialMission.getBossMaxHealth();
        double healthPercentage = specialMission.getBossHealthPercentage();
        
        textViewBossHealth.setText("Boss Health: " + currentHealth + "/" + maxHealth);
        progressBarBossHealth.setProgress((int) healthPercentage);
        
        // Update members progress
        updateMembersProgress(specialMission);
    }
    
    private String formatTimeRemaining(long timeRemainingMs) {
        if (timeRemainingMs <= 0) {
            return "Expired";
        }
        
        long days = timeRemainingMs / (24 * 60 * 60 * 1000);
        long hours = (timeRemainingMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (timeRemainingMs % (60 * 60 * 1000)) / (60 * 1000);
        
        return days + "d " + hours + "h " + minutes + "m";
    }
    
    private void updateMembersProgress(SpecialMission specialMission) {
        if (currentAlliance == null || currentAlliance.getMemberIds() == null) {
            memberProgressAdapter.setMemberProgressList(new ArrayList<>());
            return;
        }
        
        // Dohvati stvarni napredak iz baze podataka
        specialTaskService.getMembersProgress(currentAlliance.getId(), new com.google.android.gms.tasks.OnCompleteListener<List<MemberProgress>>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<List<MemberProgress>> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    android.util.Log.d("MemberAllianceActivity", "Napredak uspešno dohvaćen za " + task.getResult().size() + " članova");
                    memberProgressAdapter.setMemberProgressList(task.getResult());
                } else {
                    android.util.Log.e("MemberAllianceActivity", "Greška pri dohvatanju napretka", task.getException());
                    // Fallback na praznu listu
                    memberProgressAdapter.setMemberProgressList(new ArrayList<>());
                }
            }
        });
    }
    
    private void setupRefreshHandler() {
        refreshHandler = new android.os.Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Osveži napredak svakih 5 sekundi ako je misija aktivna
                if (currentAlliance != null && viewModel.getSpecialMission().getValue() != null) {
                    SpecialMission mission = viewModel.getSpecialMission().getValue();
                    if (mission != null && mission.isActive()) {
                        updateSpecialMissionProgress(mission);
                    }
                }
                
                // Ponovi za 5 sekundi
                refreshHandler.postDelayed(this, 5000);
            }
        };
        
        // Pokreni refresh
        refreshHandler.post(refreshRunnable);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}
