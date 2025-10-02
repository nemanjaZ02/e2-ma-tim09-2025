package com.e2_ma_tim09_2025.questify.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.e2_ma_tim09_2025.questify.models.AllianceConflictResult;
import com.e2_ma_tim09_2025.questify.services.AllianceInviteService;
import com.e2_ma_tim09_2025.questify.services.NotificationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AllianceInviteActionReceiver extends BroadcastReceiver {
    private static final String TAG = "AllianceInviteAction";
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    AllianceInviteService allianceInviteService;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "=== ALLIANCE INVITE ACTION RECEIVED ===");
        Log.d(TAG, "Action: " + action);
        Log.d(TAG, "Intent extras: " + intent.getExtras());
        
        if (action == null) {
            Log.e(TAG, "Action is null");
            return;
        }
        
        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found");
            return;
        }
        
        String userId = currentUser.getUid();
        String inviteId = intent.getStringExtra("invite_id");
        String allianceId = intent.getStringExtra("alliance_id");
        String fromUserId = intent.getStringExtra("from_user_id");
        String fromUsername = intent.getStringExtra("from_username");
        String allianceName = intent.getStringExtra("alliance_name");
        
        Log.d(TAG, "Processing action for user: " + userId);
        Log.d(TAG, "Invite ID: " + inviteId);
        Log.d(TAG, "Alliance ID: " + allianceId);
        Log.d(TAG, "From User ID: " + fromUserId);
        Log.d(TAG, "From Username: " + fromUsername);
        Log.d(TAG, "Alliance Name: " + allianceName);
        
        switch (action) {
            case "ACCEPT_INVITE":
                handleAcceptInvite(context, userId, inviteId, allianceId, fromUserId);
                break;
            case "DECLINE_INVITE":
                handleDeclineInvite(context, userId, inviteId, allianceId, fromUserId);
                break;
            default:
                Log.e(TAG, "Unknown action: " + action);
        }
    }
    
    private void handleAcceptInvite(Context context, String userId, String inviteId, String allianceId, String fromUserId) {
        Log.d(TAG, "Handling accept invite with conflict resolution");
        
        // Use conflict resolution logic
        allianceInviteService.acceptInviteWithConflictResolution(inviteId, userId, new OnCompleteListener<AllianceConflictResult>() {
            @Override
            public void onComplete(com.google.android.gms.tasks.Task<AllianceConflictResult> task) {
                if (task.isSuccessful()) {
                    AllianceConflictResult result = task.getResult();
                    if (result != null) {
                        if (result.isCanAccept()) {
                            if (result.isNeedsConfirmation()) {
                                // User needs to confirm leaving current alliance
                                Log.w(TAG, "⚠️ User needs to confirm leaving current alliance");
                                // For now, we'll show a simple message - in a real app you'd show a dialog
                                Log.w(TAG, "Cannot accept invitation: " + result.getReason());
                                // TODO: Show confirmation dialog to user
                            } else {
                                // Direct acceptance successful
                                Log.d(TAG, "✅ Alliance invite accepted successfully");
                                cancelNotification(context, inviteId);
                            }
                        } else {
                            // Cannot accept due to mission or other constraints
                            Log.w(TAG, "❌ Cannot accept invitation: " + result.getReason());
                            // TODO: Show error message to user
                        }
                    } else {
                        Log.e(TAG, "❌ Null result from conflict resolution");
                    }
                } else {
                    Log.e(TAG, "❌ Failed to process alliance invite: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                }
            }
        });
    }
    
    private void handleDeclineInvite(Context context, String userId, String inviteId, String allianceId, String fromUserId) {
        Log.d(TAG, "Handling decline invite");
        
        // Send response to server
        notificationService.sendAllianceInviteResponse("decline", inviteId, userId, allianceId, fromUserId, new NotificationService.NotificationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Alliance invite declined successfully");
                // Cancel the notification
                cancelNotification(context, inviteId);
            }
            
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Failed to decline alliance invite: " + error);
            }
        });
    }
    
    private void cancelNotification(Context context, String inviteId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && inviteId != null) {
            int notificationId = inviteId.hashCode();
            notificationManager.cancel(notificationId);
            Log.d(TAG, "📱 Cancelled notification with ID: " + notificationId);
        }
    }
}
