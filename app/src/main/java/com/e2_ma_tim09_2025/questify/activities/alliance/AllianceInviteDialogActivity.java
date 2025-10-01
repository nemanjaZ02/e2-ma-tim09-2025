package com.e2_ma_tim09_2025.questify.activities.alliance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.AllianceConflictResult;
import com.e2_ma_tim09_2025.questify.services.AllianceInviteService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.e2_ma_tim09_2025.questify.viewmodels.AllianceInviteDialogViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AllianceInviteDialogActivity extends AppCompatActivity {
    
    private AllianceInviteDialogViewModel viewModel;
    private AllianceInviteService allianceInviteService;
    private UserService userService;
    
    private TextView titleText;
    private TextView messageText;
    private MaterialButton acceptButton;
    private MaterialButton declineButton;
    private MaterialButton cancelButton;
    
    private String inviteId;
    private String allianceId;
    private String fromUserId;
    private String fromUsername;
    private String allianceName;
    private String action; // "accept" or "decline"
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_invite_dialog);
        
        initViews();
        getIntentData();
        setupViewModel();
        
        if (action != null) {
            handleInvitationAction();
        } else {
            finish();
        }
    }
    
    private void initViews() {
        titleText = findViewById(R.id.textViewDialogTitle);
        messageText = findViewById(R.id.textViewDialogMessage);
        acceptButton = findViewById(R.id.buttonAccept);
        declineButton = findViewById(R.id.buttonDecline);
        cancelButton = findViewById(R.id.buttonCancel);
        
        cancelButton.setOnClickListener(v -> finish());
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        inviteId = intent.getStringExtra("invite_id");
        allianceId = intent.getStringExtra("alliance_id");
        fromUserId = intent.getStringExtra("from_user_id");
        fromUsername = intent.getStringExtra("from_username");
        allianceName = intent.getStringExtra("alliance_name");
        
        // Determine action from notification type
        String notificationType = intent.getStringExtra("notification_type");
        if ("alliance_invite_accept".equals(notificationType)) {
            action = "accept";
        } else if ("alliance_invite_decline".equals(notificationType)) {
            action = "decline";
        }
    }
    
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AllianceInviteDialogViewModel.class);
        allianceInviteService = viewModel.getAllianceInviteService();
        userService = viewModel.getUserService();
    }
    
    private void handleInvitationAction() {
        if ("accept".equals(action)) {
            handleAcceptInvitation();
        } else if ("decline".equals(action)) {
            handleDeclineInvitation();
        }
    }
    
    private void handleAcceptInvitation() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            showError("User not logged in");
            return;
        }
        
        titleText.setText("Accept Alliance Invitation");
        messageText.setText("You have been invited to join the alliance \"" + allianceName + "\" by " + fromUsername + ".");
        
        // Check for conflicts
        allianceInviteService.acceptInviteWithConflictResolution(inviteId, currentUserId, new OnCompleteListener<AllianceConflictResult>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<AllianceConflictResult> task) {
                if (task.isSuccessful()) {
                    AllianceConflictResult result = task.getResult();
                    if (result != null) {
                        if (result.isCanAccept()) {
                            if (result.isNeedsConfirmation()) {
                                // Show confirmation dialog for leaving current alliance
                                showAllianceSwitchConfirmation(result);
                            } else {
                                // Direct acceptance
                                showDirectAcceptConfirmation();
                            }
                        } else {
                            // Cannot accept due to mission
                            showMissionBlockedDialog(result.getReason());
                        }
                    } else {
                        showError("Failed to process invitation");
                    }
                } else {
                    showError("Failed to process invitation: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            }
        });
    }
    
    private void handleDeclineInvitation() {
        titleText.setText("Decline Alliance Invitation");
        messageText.setText("Are you sure you want to decline the invitation to join the alliance \"" + allianceName + "\"?");
        
        declineButton.setOnClickListener(v -> {
            allianceInviteService.declineInvite(inviteId, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showError("Failed to decline invitation");
                }
            });
        });
    }
    
    private void showDirectAcceptConfirmation() {
        messageText.setText("You have been invited to join the alliance \"" + allianceName + "\" by " + fromUsername + ".\n\nDo you want to accept this invitation?");
        
        acceptButton.setOnClickListener(v -> {
            allianceInviteService.acceptInvite(inviteId, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Invitation accepted successfully!", Toast.LENGTH_SHORT).show();
                    // Redirect to Member Alliance page
                    Intent intent = new Intent(this, MemberAllianceActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    showError("Failed to accept invitation");
                }
            });
        });
    }
    
    private void showAllianceSwitchConfirmation(AllianceConflictResult result) {
        String currentAllianceName = result.getCurrentAlliance() != null ? result.getCurrentAlliance().getName() : "your current alliance";
        
        messageText.setText("⚠️ ALLIANCE CONFLICT DETECTED\n\n" +
                "You are currently a member of the alliance \"" + currentAllianceName + "\".\n\n" +
                "To accept the invitation to join \"" + allianceName + "\", you will be:\n" +
                "• REMOVED from \"" + currentAllianceName + "\"\n" +
                "• ADDED to \"" + allianceName + "\"\n\n" +
                "⚠️ You can only be in ONE alliance at a time.\n\n" +
                "Do you want to leave \"" + currentAllianceName + "\" and join \"" + allianceName + "\"?");
        
        acceptButton.setText("Leave & Join");
        acceptButton.setOnClickListener(v -> {
            // Show final confirmation dialog
            showFinalConfirmationDialog(currentAllianceName, result);
        });
    }
    
    private void showFinalConfirmationDialog(String currentAllianceName, AllianceConflictResult result) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Final Confirmation")
                .setMessage("Are you absolutely sure you want to:\n\n" +
                        "✅ LEAVE \"" + currentAllianceName + "\"\n" +
                        "✅ JOIN \"" + allianceName + "\"\n\n" +
                        "This action cannot be undone!")
                .setPositiveButton("Yes, Leave & Join", (dialog, which) -> {
                    // Process alliance switch
                    allianceInviteService.processAllianceSwitch(
                        userService.getCurrentUserId(),
                        result.getNewAllianceId(),
                        result.getInviteId(),
                        task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Successfully left \"" + currentAllianceName + "\" and joined \"" + allianceName + "\"!", Toast.LENGTH_LONG).show();
                                // Redirect to Member Alliance page
                                Intent intent = new Intent(this, MemberAllianceActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                showError("Failed to switch alliances: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                            }
                        }
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showMissionBlockedDialog(String reason) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cannot Accept Invitation")
                .setMessage(reason)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}
