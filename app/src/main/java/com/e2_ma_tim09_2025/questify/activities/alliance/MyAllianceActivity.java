package com.e2_ma_tim09_2025.questify.activities.alliance;

import android.content.Intent;
import android.os.Bundle;
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
import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.viewmodels.MyAllianceViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyAllianceActivity extends AppCompatActivity {
    
    private MyAllianceViewModel viewModel;
    private AllianceMembersAdapter membersAdapter;
    private EligibleUsersAdapter eligibleUsersAdapter;
    
    private TextView allianceNameText;
    private TextView membersCountText;
    private TextView missionStatusText;
    private TextView noAllianceText;
    private RecyclerView membersRecyclerView;
    private RecyclerView eligibleUsersRecyclerView;
    private Button inviteUsersButton;
    private Button deleteAllianceButton;
    private ProgressBar progressBar;
    
    private Alliance currentAlliance;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_alliance);
        
        initViews();
        setupRecyclerViews();
        setupViewModel();
        setupClickListeners();
        
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
        progressBar = findViewById(R.id.progressBar);
        
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
    }
    
    private void displayAlliance(Alliance alliance) {
        allianceNameText.setText(alliance.getName());
        allianceNameText.setVisibility(View.VISIBLE);
        membersCountText.setVisibility(View.VISIBLE);
        missionStatusText.setVisibility(View.VISIBLE);
        membersRecyclerView.setVisibility(View.VISIBLE);
        inviteUsersButton.setVisibility(View.VISIBLE);
        deleteAllianceButton.setVisibility(View.VISIBLE);
        noAllianceText.setVisibility(View.GONE);
        
        // Update mission status
        updateMissionStatus(alliance.isMissionStarted());
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
}
