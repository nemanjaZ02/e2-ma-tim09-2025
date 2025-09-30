package com.e2_ma_tim09_2025.questify.services;


import android.util.Log;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.models.AllianceInvite;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.AllianceInviteStatus;
import com.e2_ma_tim09_2025.questify.repositories.AllianceInviteRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import javax.inject.Inject;

public class AllianceInviteService {

    private final AllianceInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Inject
    public AllianceInviteService(AllianceInviteRepository inviteRepository, 
                                UserRepository userRepository,
                                NotificationService notificationService) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Send a new invite to a user.
     *
     * @param invite   AllianceInvite object containing sender, receiver, allianceId
     * @param listener OnCompleteListener<Void> to handle success/failure
     */
    public void sendInvite(@NonNull AllianceInvite invite, @NonNull OnCompleteListener<Void> listener) {
        if (invite.getFromUserId() == null || invite.getToUserId() == null || invite.getAllianceId() == null) {
            Log.e("AllianceInviteService", "Invalid invite data");
            // Ne pozivamo listener sa Task.forException, već samo logujemo i izlazimo
            return;
        }

        // First, get the sender's username for the notification
        userRepository.getUser(invite.getFromUserId(), fromUserTask -> {
            if (!fromUserTask.isSuccessful() || fromUserTask.getResult() == null || !fromUserTask.getResult().exists()) {
                Log.e("AllianceInviteService", "Failed to get sender user data");
                // Continue with invite creation even if we can't get sender data
                createInviteAndSendNotification(invite, null, listener);
                return;
            }

            User fromUser = fromUserTask.getResult().toObject(User.class);
            String fromUsername = fromUser != null ? fromUser.getUsername() : "Unknown User";
            
            createInviteAndSendNotification(invite, fromUsername, listener);
        });
    }

    private void createInviteAndSendNotification(@NonNull AllianceInvite invite, String fromUsername, @NonNull OnCompleteListener<Void> listener) {
        Log.d("AllianceInviteService", "=== STARTING ALLIANCE INVITE PROCESS ===");
        Log.d("AllianceInviteService", "From User ID: " + invite.getFromUserId());
        Log.d("AllianceInviteService", "To User ID: " + invite.getToUserId());
        Log.d("AllianceInviteService", "Alliance ID: " + invite.getAllianceId());
        Log.d("AllianceInviteService", "From Username: " + fromUsername);
        
        // Create the invite in Firestore
        inviteRepository.sendInvite(invite, inviteTask -> {
            if (inviteTask.isSuccessful()) {
                Log.d("AllianceInviteService", "✅ Invite created successfully in Firestore");
                Log.d("AllianceInviteService", "🚀 Starting push notification process...");
                
                // Send push notification
                String allianceName = "Alliance"; // You might want to get the actual alliance name
                Log.d("AllianceInviteService", "📱 Sending notification to user: " + invite.getToUserId());
                Log.d("AllianceInviteService", "📱 From user: " + fromUsername);
                Log.d("AllianceInviteService", "📱 Alliance: " + allianceName);
                
                notificationService.sendAllianceInviteNotification(
                        invite.getToUserId(),
                        fromUsername != null ? fromUsername : "Someone",
                        allianceName,
                        invite.getId(),
                        invite.getAllianceId(),
                        invite.getFromUserId(),
                        new NotificationService.NotificationCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("AllianceInviteService", "✅ Push notification sent successfully!");
                                Log.d("AllianceInviteService", "=== ALLIANCE INVITE PROCESS COMPLETED SUCCESSFULLY ===");
                                
                                // Also create a local notification as fallback
                                createLocalNotification(invite, fromUsername, allianceName);
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("AllianceInviteService", "❌ Failed to send push notification: " + error);
                                Log.e("AllianceInviteService", "=== ALLIANCE INVITE PROCESS COMPLETED WITH NOTIFICATION FAILURE ===");
                                
                                // Create local notification as fallback even if server notification fails
                                createLocalNotification(invite, fromUsername, allianceName);
                            }
                        }
                );
            } else {
                Log.e("AllianceInviteService", "❌ Failed to create invite in Firestore");
                Log.e("AllianceInviteService", "Error: " + (inviteTask.getException() != null ? inviteTask.getException().getMessage() : "Unknown error"));
            }
            
            // Call the original listener regardless of notification success
            listener.onComplete(inviteTask);
        });
    }

    /**
     * Accept an invite: mark invite as accepted.
     *
     * @param inviteId Invite ID
     * @param listener OnCompleteListener<Void> to handle success/failure
     */
    public void acceptInvite(@NonNull String inviteId, @NonNull OnCompleteListener<Void> listener) {
        inviteRepository.updateInviteStatus(inviteId, AllianceInviteStatus.ACCEPTED, listener);
    }

    public void declineInvite(@NonNull String inviteId, @NonNull OnCompleteListener<Void> listener) {
        inviteRepository.updateInviteStatus(inviteId, AllianceInviteStatus.REJECTED, listener);
    }


    public void getInvitesForUser(@NonNull String userId, @NonNull OnCompleteListener taskListener) {
        inviteRepository.getInvitesForUser(userId, taskListener);
    }

    /**
     * Create a local notification with Accept/Decline actions as fallback
     */
    private void createLocalNotification(AllianceInvite invite, String fromUsername, String allianceName) {
        Log.d("AllianceInviteService", "🔔 Creating local notification as fallback");
        
        // This will be implemented to create a notification directly on the device
        // For now, just log that we would create it
        Log.d("AllianceInviteService", "📱 Would create local notification for invite: " + invite.getId());
        Log.d("AllianceInviteService", "📱 From: " + fromUsername + " to alliance: " + allianceName);
    }
}
