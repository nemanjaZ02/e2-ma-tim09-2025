package com.e2_ma_tim09_2025.questify.services;


import android.util.Log;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.AllianceConflictResult;
import com.e2_ma_tim09_2025.questify.models.AllianceInvite;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.AllianceInviteStatus;
import com.e2_ma_tim09_2025.questify.repositories.AllianceInviteRepository;
import com.e2_ma_tim09_2025.questify.repositories.AllianceRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import javax.inject.Inject;

public class AllianceInviteService {

    private final AllianceInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final AllianceRepository allianceRepository;
    private final NotificationService notificationService;

    @Inject
    public AllianceInviteService(AllianceInviteRepository inviteRepository, 
                                UserRepository userRepository,
                                AllianceRepository allianceRepository,
                                NotificationService notificationService) {
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.allianceRepository = allianceRepository;
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

    /**
     * Accept an invite with conflict resolution for users already in alliances
     *
     * @param inviteId Invite ID
     * @param userId User ID accepting the invite
     * @param listener OnCompleteListener<AllianceConflictResult> to handle the result
     */
    public void acceptInviteWithConflictResolution(@NonNull String inviteId, @NonNull String userId, @NonNull OnCompleteListener<AllianceConflictResult> listener) {
        // First get the invite details
        inviteRepository.getInvite(inviteId, inviteTask -> {
            if (!inviteTask.isSuccessful() || inviteTask.getResult() == null || !inviteTask.getResult().exists()) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Invitation not found")));
                return;
            }

            AllianceInvite invite = inviteTask.getResult().toObject(AllianceInvite.class);
            if (invite == null) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Failed to parse invitation data")));
                return;
            }

            // Check if user is already in an alliance
            getUserCurrentAlliance(userId, currentAllianceTask -> {
                if (!currentAllianceTask.isSuccessful()) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(currentAllianceTask.getException()));
                    return;
                }

                Alliance currentAlliance = currentAllianceTask.getResult();
                
                if (currentAlliance == null) {
                    // User is not in any alliance, can accept directly
                    acceptInvitationDirectly(invite, userId, listener);
                } else {
                    // User is already in an alliance, need to check mission status
                    if (currentAlliance.isMissionStarted()) {
                        // Cannot leave alliance during active mission
                        AllianceConflictResult result = new AllianceConflictResult();
                        result.setCanAccept(false);
                        result.setReason("Cannot accept invitation because you are currently in an alliance with an active mission. Please wait for the mission to complete.");
                        result.setCurrentAlliance(currentAlliance);
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(result));
                    } else {
                        // Can leave current alliance, but need user confirmation
                        AllianceConflictResult result = new AllianceConflictResult();
                        result.setCanAccept(true);
                        result.setNeedsConfirmation(true);
                        result.setCurrentAlliance(currentAlliance);
                        result.setNewAllianceId(invite.getAllianceId());
                        result.setInviteId(inviteId);
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(result));
                    }
                }
            });
        });
    }

    /**
     * Process alliance switch after user confirmation
     *
     * @param userId User ID
     * @param newAllianceId New alliance ID
     * @param inviteId Invite ID
     * @param listener OnCompleteListener<Void> to handle success/failure
     */
    public void processAllianceSwitch(@NonNull String userId, @NonNull String newAllianceId, @NonNull String inviteId, @NonNull OnCompleteListener<Void> listener) {
        // First remove user from current alliance
        removeUserFromAlliance(userId, removeTask -> {
            if (removeTask.isSuccessful()) {
                // Now accept the new invitation
                inviteRepository.getInvite(inviteId, inviteTask -> {
                    if (inviteTask.isSuccessful() && inviteTask.getResult() != null && inviteTask.getResult().exists()) {
                        AllianceInvite invite = inviteTask.getResult().toObject(AllianceInvite.class);
                        if (invite != null) {
                            acceptInvitationDirectly(invite, userId, resultTask -> {
                                if (resultTask.isSuccessful()) {
                                    listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                                } else {
                                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(resultTask.getException()));
                                }
                            });
                        } else {
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                new Exception("Failed to parse invitation data")));
                        }
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            new Exception("Invitation not found")));
                    }
                });
            } else {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(removeTask.getException()));
            }
        });
    }

    public void declineInvite(@NonNull String inviteId, @NonNull OnCompleteListener<Void> listener) {
        inviteRepository.updateInviteStatus(inviteId, AllianceInviteStatus.REJECTED, listener);
    }

    public void getInvitesForUser(@NonNull String userId, @NonNull OnCompleteListener taskListener) {
        inviteRepository.getInvitesForUser(userId, taskListener);
    }

    /**
     * Check if user is currently in an alliance and get alliance details
     */
    private void getUserCurrentAlliance(String userId, OnCompleteListener<Alliance> listener) {
        userRepository.getUser(userId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("User not found")));
                return;
            }

            User user = task.getResult().toObject(User.class);
            if (user == null || user.getAllianceId() == null || user.getAllianceId().isEmpty()) {
                // User is not in any alliance
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                return;
            }

            // Get the alliance details
            allianceRepository.getAlliance(user.getAllianceId(), allianceTask -> {
                if (!allianceTask.isSuccessful() || allianceTask.getResult() == null || !allianceTask.getResult().exists()) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                        new Exception("Current alliance not found")));
                    return;
                }

                Alliance alliance = allianceTask.getResult().toObject(Alliance.class);
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(alliance));
            });
        });
    }

    /**
     * Remove user from their current alliance
     */
    private void removeUserFromAlliance(String userId, OnCompleteListener<Void> listener) {
        getUserCurrentAlliance(userId, task -> {
            if (!task.isSuccessful()) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(task.getException()));
                return;
            }

            Alliance currentAlliance = task.getResult();
            if (currentAlliance == null) {
                // User is not in any alliance
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                return;
            }

            // Check if user is the leader
            if (userId.equals(currentAlliance.getLeaderId())) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Cannot remove alliance leader. Leader must delete the alliance or transfer leadership first.")));
                return;
            }

            // Remove user from alliance members list
            java.util.List<String> memberIds = new java.util.ArrayList<>(currentAlliance.getMemberIds());
            memberIds.remove(userId);
            currentAlliance.setMemberIds(memberIds);

            // Update alliance in Firestore
            allianceRepository.updateAlliance(currentAlliance, updateTask -> {
                if (updateTask.isSuccessful()) {
                    // Remove allianceId from user document
                    userRepository.getUser(userId, userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().exists()) {
                            User user = userTask.getResult().toObject(User.class);
                            if (user != null) {
                                user.setAllianceId(null);
                                userRepository.updateUser(user, userUpdateTask -> {
                                    listener.onComplete(userUpdateTask);
                                });
                            } else {
                                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                    new Exception("Failed to get user data")));
                            }
                        } else {
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                new Exception("Failed to get user data")));
                        }
                    });
                } else {
                    listener.onComplete(updateTask);
                }
            });
        });
    }

    /**
     * Accept invitation directly (user is not in any alliance)
     */
    private void acceptInvitationDirectly(AllianceInvite invite, String userId, OnCompleteListener<AllianceConflictResult> listener) {
        // Get the target alliance
        allianceRepository.getAlliance(invite.getAllianceId(), allianceTask -> {
            if (!allianceTask.isSuccessful() || allianceTask.getResult() == null || !allianceTask.getResult().exists()) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Target alliance not found")));
                return;
            }

            Alliance targetAlliance = allianceTask.getResult().toObject(Alliance.class);
            if (targetAlliance == null) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Failed to parse target alliance data")));
                return;
            }

            // Add user to target alliance
            java.util.List<String> memberIds = new java.util.ArrayList<>(targetAlliance.getMemberIds());
            if (!memberIds.contains(userId)) {
                memberIds.add(userId);
                targetAlliance.setMemberIds(memberIds);
            }

            // Update alliance and user
            allianceRepository.updateAlliance(targetAlliance, updateTask -> {
                if (updateTask.isSuccessful()) {
                    // Update user's allianceId
                    userRepository.getUser(userId, userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().exists()) {
                            User user = userTask.getResult().toObject(User.class);
                            if (user != null) {
                                user.setAllianceId(invite.getAllianceId());
                                userRepository.updateUser(user, userUpdateTask -> {
                                    if (userUpdateTask.isSuccessful()) {
                                        // Mark invite as accepted
                                        inviteRepository.updateInviteStatus(invite.getId(), AllianceInviteStatus.ACCEPTED, statusTask -> {
                                            AllianceConflictResult result = new AllianceConflictResult();
                                            result.setCanAccept(true);
                                            result.setNeedsConfirmation(false);
                                            result.setSuccess(true);
                                            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(result));
                                        });
                                    } else {
                                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(userUpdateTask.getException()));
                                    }
                                });
                            } else {
                                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                    new Exception("Failed to get user data")));
                            }
                        } else {
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                new Exception("Failed to get user data")));
                        }
                    });
                } else {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(updateTask.getException()));
                }
            });
        });
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
