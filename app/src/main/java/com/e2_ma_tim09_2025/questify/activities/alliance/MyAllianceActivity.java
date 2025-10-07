package com.e2_ma_tim09_2025.questify.activities.alliance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.adapters.alliance.AllianceMembersAdapter;
import com.e2_ma_tim09_2025.questify.adapters.alliance.EligibleUsersAdapter;
import com.e2_ma_tim09_2025.questify.adapters.alliance.MemberProgressAdapter;
import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.MemberProgress;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialMissionStatus;
import com.e2_ma_tim09_2025.questify.services.SpecialTaskService;
import com.e2_ma_tim09_2025.questify.viewmodels.MyAllianceViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyAllianceActivity extends AppCompatActivity {
    
    private MyAllianceViewModel viewModel;
    @Inject SpecialTaskService specialTaskService;
    private AllianceMembersAdapter membersAdapter;
    private EligibleUsersAdapter eligibleUsersAdapter;
    private MemberProgressAdapter memberProgressAdapter;
    
    private TextView allianceNameText;
    private TextView membersCountText;
    private TextView missionStatusText;
    private TextView noAllianceText;
    private RecyclerView membersRecyclerView;
    private RecyclerView eligibleUsersRecyclerView;
    private Button inviteUsersButton;
    private Button deleteAllianceButton;
    private Button startSpecialMissionButton;
    private Button allianceChatButton;
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
        setContentView(R.layout.activity_my_alliance);
        
        initViews();
        setupRecyclerViews();
        setupViewModel();
        setupClickListeners();
        setupRefreshHandler();
        
        // Load alliances
        viewModel.loadAlliances();
    }
    
    private void initViews() {
        allianceNameText = findViewById(R.id.textViewAllianceName);
        membersCountText = findViewById(R.id.textViewMembersCount);
        missionStatusText = findViewById(R.id.textViewMissionStatus);
        noAllianceText = findViewById(R.id.textViewNoAlliance);
        membersRecyclerView = findViewById(R.id.recyclerViewMembers);
        eligibleUsersRecyclerView = findViewById(R.id.recyclerViewEligibleUsers);
        inviteUsersButton = findViewById(R.id.buttonInviteUsers);
        deleteAllianceButton = findViewById(R.id.buttonDeleteAlliance);
        startSpecialMissionButton = findViewById(R.id.buttonStartSpecialMission);
        allianceChatButton = findViewById(R.id.buttonAllianceChat);
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
    
    private void setupRecyclerViews() {
        // Members RecyclerView
        membersAdapter = new AllianceMembersAdapter(new ArrayList<>());
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setAdapter(membersAdapter);
        
        // Eligible Users RecyclerView
        eligibleUsersAdapter = new EligibleUsersAdapter(new ArrayList<>(), new EligibleUsersAdapter.OnUserInviteClickListener() {
            @Override
            public void onInviteClick(User user) {
                showInviteConfirmationDialog(user);
            }
        });
        eligibleUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eligibleUsersRecyclerView.setAdapter(eligibleUsersAdapter);
        
        // Members Progress RecyclerView
        memberProgressAdapter = new MemberProgressAdapter();
        recyclerViewMembersProgress.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMembersProgress.setAdapter(memberProgressAdapter);
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MyAllianceViewModel.class);
        
        // Observe alliances
        viewModel.getAlliances().observe(this, alliances -> {
            if (alliances != null && !alliances.isEmpty()) {
                currentAlliance = alliances.get(0); // Show first alliance (user can only be leader of one)
                displayAlliance(currentAlliance);
                viewModel.loadAllianceMembers(currentAlliance.getId());
            } else {
                displayNoAlliance();
            }
        });
        
        // Observe members
        viewModel.getMembers().observe(this, members -> {
            if (members != null) {
                membersAdapter.setMembers(members);
                updateMembersCount(members.size());
            }
        });
        
        // Observe eligible users
        viewModel.getEligibleUsers().observe(this, users -> {
            if (users != null) {
                eligibleUsersAdapter.setUsers(users);
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
        
        // Observe invitation sent
        viewModel.getInvitationSent().observe(this, sent -> {
            if (sent) {
                Toast.makeText(this, "Invitation sent successfully!", Toast.LENGTH_SHORT).show();
                viewModel.clearInvitationSent();
            }
        });
        
        // Observe alliance deleted
        viewModel.getAllianceDeleted().observe(this, deleted -> {
            if (deleted) {
                Toast.makeText(this, "Alliance deleted successfully!", Toast.LENGTH_SHORT).show();
                viewModel.clearAllianceDeleted();
                // Refresh the page to show "no alliance" state
                viewModel.loadAlliances();
            }
        });
        
        // Observe special mission
        viewModel.getSpecialMission().observe(this, specialMission -> {
            if (specialMission != null) {
                // Hide progress section if mission is inactive or expired
                if (specialMission.getStatus() == SpecialMissionStatus.INACTIVE || 
                    specialMission.getStatus() == SpecialMissionStatus.EXPIRED) {
                    specialMissionProgressSection.setVisibility(View.GONE);
                    if (currentAlliance != null) {
                        viewModel.checkCanCreateSpecialMission(currentAlliance.getId());
                    }
                } else {
                    // Mission is active - show progress
                    startSpecialMissionButton.setVisibility(View.GONE);
                    updateSpecialMissionProgress(specialMission);
                }
            } else {
                specialMissionProgressSection.setVisibility(View.GONE);
                if (currentAlliance != null) {
                    viewModel.checkCanCreateSpecialMission(currentAlliance.getId());
                }
            }
        });
        
        // Observe can create special mission
        viewModel.getCanCreateSpecialMission().observe(this, canCreate -> {
            if (canCreate != null) {
                startSpecialMissionButton.setVisibility(canCreate ? View.VISIBLE : View.GONE);
            }
        });
        
        // Observe special mission created
        viewModel.getSpecialMissionCreated().observe(this, created -> {
            if (created) {
                Toast.makeText(this, "Special mission started successfully!", Toast.LENGTH_SHORT).show();
                viewModel.clearSpecialMissionCreated();
                // Reload special mission data
                if (currentAlliance != null) {
                    viewModel.loadSpecialMission(currentAlliance.getId());
                }
            }
        });
    }
    
    private void setupClickListeners() {
        inviteUsersButton.setOnClickListener(v -> {
            if (currentAlliance != null) {
                viewModel.loadEligibleUsers(currentAlliance.getId());
                eligibleUsersRecyclerView.setVisibility(View.VISIBLE);
            }
        });
        
        deleteAllianceButton.setOnClickListener(v -> {
            if (currentAlliance != null) {
                showDeleteAllianceConfirmationDialog();
            }
        });
        
        startSpecialMissionButton.setOnClickListener(v -> {
            if (currentAlliance != null) {
                showStartSpecialMissionConfirmationDialog();
            }
        });
        
        allianceChatButton.setOnClickListener(v -> {
            if (currentAlliance != null) {
                openAllianceChat();
            }
        });
    }
    
    private void displayAlliance(Alliance alliance) {
        allianceNameText.setText(alliance.getName());
        allianceNameText.setVisibility(View.VISIBLE);
        membersCountText.setVisibility(View.VISIBLE);
        missionStatusText.setVisibility(View.VISIBLE);
        membersRecyclerView.setVisibility(View.VISIBLE);
        inviteUsersButton.setVisibility(View.VISIBLE);
        deleteAllianceButton.setVisibility(View.VISIBLE);
        startSpecialMissionButton.setVisibility(View.VISIBLE);
        allianceChatButton.setVisibility(View.VISIBLE);
        noAllianceText.setVisibility(View.GONE);
        
        // Update mission status
        updateMissionStatus(alliance.isMissionStarted());
        
        // Load special mission data
        viewModel.loadSpecialMission(alliance.getId());
        
        // Check if can create special mission
        viewModel.checkCanCreateSpecialMission(alliance.getId());
    }
    
    private void displayNoAlliance() {
        allianceNameText.setVisibility(View.GONE);
        membersCountText.setVisibility(View.GONE);
        missionStatusText.setVisibility(View.GONE);
        membersRecyclerView.setVisibility(View.GONE);
        inviteUsersButton.setVisibility(View.GONE);
        deleteAllianceButton.setVisibility(View.GONE);
        eligibleUsersRecyclerView.setVisibility(View.GONE);
        noAllianceText.setVisibility(View.VISIBLE);
    }
    
    private void updateMembersCount(int count) {
        membersCountText.setText("Members: " + count);
    }
    
    private void updateMissionStatus(boolean isMissionActive) {
        if (isMissionActive) {
            missionStatusText.setText("Mission Status: 🔴 ACTIVE");
            missionStatusText.setTextColor(0xFFD32F2F); // Red color
            // Disable delete button when mission is active
            deleteAllianceButton.setEnabled(false);
            deleteAllianceButton.setAlpha(0.5f);
        } else {
            missionStatusText.setText("Mission Status: 🟢 Inactive");
            missionStatusText.setTextColor(0xFF8B4513); // Brown color
            // Enable delete button when mission is inactive
            deleteAllianceButton.setEnabled(true);
            deleteAllianceButton.setAlpha(1.0f);
        }
    }
    
    private void showInviteConfirmationDialog(User user) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Send Invitation")
                .setMessage("Send alliance invitation to " + user.getUsername() + "?")
                .setPositiveButton("Send", (dialog, which) -> {
                    if (currentAlliance != null) {
                        viewModel.sendInvitation(currentAlliance.getId(), user.getId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void openAllianceChat() {
        if (currentAlliance == null) return;
        
        Intent intent = new Intent(this, AllianceChatActivity.class);
        intent.putExtra("allianceId", currentAlliance.getId());
        intent.putExtra("allianceName", currentAlliance.getName());
        startActivity(intent);
    }
    
    private void showDeleteAllianceConfirmationDialog() {
        if (currentAlliance == null) return;
        
        // Check if mission is active
        if (currentAlliance.isMissionStarted()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cannot Delete Alliance")
                    .setMessage("The alliance \"" + currentAlliance.getName() + "\" cannot be deleted because a mission is currently active.\n\nPlease complete or cancel the current mission first.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Alliance")
                .setMessage("Are you sure you want to delete the alliance \"" + currentAlliance.getName() + "\"?\n\nThis will:\n• Remove all members from the alliance\n• Delete all pending invitations\n• This action cannot be undone")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteAlliance(currentAlliance.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showStartSpecialMissionConfirmationDialog() {
        if (currentAlliance == null) return;
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Start Special Mission")
                .setMessage("Are you sure you want to start a special mission for alliance \"" + currentAlliance.getName() + "\"?\n\nThis will:\n• Create a special boss with " + (currentAlliance.getMemberIds().size() * 100) + " HP\n• Assign 6 special tasks to each member\n• Mission lasts for 2 weeks\n• Cannot be cancelled once started")
                .setPositiveButton("Start Mission", (dialog, which) -> {
                    viewModel.createSpecialMission(currentAlliance.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                    Log.d("MyAllianceActivity", "Napredak uspešno dohvaćen za " + task.getResult().size() + " članova");
                    memberProgressAdapter.setMemberProgressList(task.getResult());
                } else {
                    Log.e("MyAllianceActivity", "Greška pri dohvatanju napretka", task.getException());
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
