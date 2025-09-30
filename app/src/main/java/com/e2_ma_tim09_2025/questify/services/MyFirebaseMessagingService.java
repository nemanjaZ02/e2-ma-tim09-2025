package com.e2_ma_tim09_2025.questify.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.MainActivity;
import com.e2_ma_tim09_2025.questify.receivers.AllianceInviteActionReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessaging";
    private static final String CHANNEL_ID = "alliance_notifications";
    private static final String CHANNEL_NAME = "Alliance Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for alliance invitations and updates";

    @Inject
    FCMTokenService fcmTokenService;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "=== FCM MESSAGE RECEIVED ===");
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message ID: " + remoteMessage.getMessageId());
        Log.d(TAG, "Message Type: " + remoteMessage.getMessageType());
        Log.d(TAG, "Data size: " + remoteMessage.getData().size());
        Log.d(TAG, "Has notification: " + (remoteMessage.getNotification() != null));

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Log.d(TAG, "Calling handleDataMessage...");
            handleDataMessage(remoteMessage.getData());
            Log.d(TAG, "handleDataMessage completed");
        } else {
            Log.d(TAG, "No data payload found");
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Calling handleNotificationMessage...");
            handleNotificationMessage(remoteMessage.getNotification());
            Log.d(TAG, "handleNotificationMessage completed");
        } else {
            Log.d(TAG, "No notification payload found");
        }
        
        Log.d(TAG, "=== FCM MESSAGE PROCESSING COMPLETE ===");
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // Send token to your server if needed
        // For now, we'll let FCMTokenService handle it
        if (fcmTokenService != null) {
            fcmTokenService.onTokenRefresh();
        }
    }

    private void handleDataMessage(java.util.Map<String, String> data) {
        try {
            Log.d(TAG, "=== HANDLING DATA MESSAGE ===");
            String type = data.get("type");
            String title = data.get("title");
            String body = data.get("body");
            String allianceId = data.get("allianceId");
            String fromUserId = data.get("fromUserId");
            String inviteId = data.get("inviteId");
            String fromUsername = data.get("fromUsername");
            String allianceName = data.get("allianceName");
            String persistent = data.get("persistent");

            Log.d(TAG, "Type: " + type);
            Log.d(TAG, "Title: " + title);
            Log.d(TAG, "Body: " + body);
            Log.d(TAG, "Invite ID: " + inviteId);
            Log.d(TAG, "Persistent: " + persistent);

            switch (type) {
                case "alliance_invite":
                    Log.d(TAG, "Creating alliance invite notification with actions");
                    showAllianceInviteNotification(title, body, allianceId, fromUserId, inviteId, fromUsername, allianceName, "true".equals(persistent));
                    break;
                case "alliance_invite_accepted":
                    Log.d(TAG, "Creating alliance invite accepted notification");
                    showAllianceInviteAcceptedNotification(title, body, allianceId, data.get("acceptedBy"), data.get("acceptedByUsername"));
                    break;
                default:
                    Log.d(TAG, "Creating generic notification for type: " + type);
                    showGenericNotification(title, body);
                    break;
            }
            Log.d(TAG, "=== DATA MESSAGE HANDLING COMPLETED ===");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling data message", e);
            // Fallback to generic notification
            try {
                showGenericNotification(data.get("title"), data.get("body"));
            } catch (Exception fallbackError) {
                Log.e(TAG, "❌ Error showing fallback notification", fallbackError);
            }
        }
    }

    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String body = notification.getBody();
        showGenericNotification(title, body);
    }

    private void showAllianceInviteNotification(String title, String body, String allianceId, String fromUserId, String inviteId, String fromUsername, String allianceName, boolean persistent) {
        Log.d(TAG, "=== CREATING ALLIANCE INVITE NOTIFICATION ===");
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Body: " + body);
        Log.d(TAG, "Invite ID: " + inviteId);
        Log.d(TAG, "Persistent: " + persistent);
        
        // Create main intent for notification tap
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notification_type", "alliance_invite");
        intent.putExtra("alliance_id", allianceId);
        intent.putExtra("from_user_id", fromUserId);
        intent.putExtra("invite_id", inviteId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create accept action intent
        Intent acceptIntent = new Intent(this, AllianceInviteActionReceiver.class);
        acceptIntent.setAction("ACCEPT_INVITE");
        acceptIntent.putExtra("invite_id", inviteId);
        acceptIntent.putExtra("alliance_id", allianceId);
        acceptIntent.putExtra("from_user_id", fromUserId);
        acceptIntent.putExtra("from_username", fromUsername);
        acceptIntent.putExtra("alliance_name", allianceName);
        
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, 
                (int) System.currentTimeMillis(), acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create decline action intent
        Intent declineIntent = new Intent(this, AllianceInviteActionReceiver.class);
        declineIntent.setAction("DECLINE_INVITE");
        declineIntent.putExtra("invite_id", inviteId);
        declineIntent.putExtra("alliance_id", allianceId);
        declineIntent.putExtra("from_user_id", fromUserId);
        declineIntent.putExtra("from_username", fromUsername);
        declineIntent.putExtra("alliance_name", allianceName);
        
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(this, 
                (int) (System.currentTimeMillis() + 1), declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Log.d(TAG, "Building notification with actions...");
        Log.d(TAG, "Accept PendingIntent: " + (acceptPendingIntent != null ? "OK" : "NULL"));
        Log.d(TAG, "Decline PendingIntent: " + (declinePendingIntent != null ? "OK" : "NULL"));
        
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title != null ? title : "Alliance Invitation")
                .setContentText(body != null ? body : "You have been invited to join an alliance")
                .setAutoCancel(false) // Never auto-cancel - user must respond
                .setOngoing(true) // Always ongoing - cannot be dismissed by swiping
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX) // Maximum priority
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setColor(0xFF6B35) // Orange color
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
                .setFullScreenIntent(pendingIntent, false) // Don't show full screen
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
                .setOnlyAlertOnce(false) // Alert every time
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(body != null ? body : "You have been invited to join an alliance. Please respond by tapping Accept or Decline."))
                .setNumber(1) // Show as important notification
                .setTimeoutAfter(0) // Never timeout
                .setShowWhen(true) // Show timestamp
                .setWhen(System.currentTimeMillis()) // Current time
                .setLocalOnly(true) // Only show locally, don't sync
                .setDeleteIntent(null) // No delete intent - cannot be dismissed
                .setSilent(false) // Make sure it makes sound
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE); // Immediate foreground behavior
        
        Log.d(TAG, "Adding Accept action...");
        Log.d(TAG, "Accept PendingIntent: " + (acceptPendingIntent != null ? "OK" : "NULL"));
        Log.d(TAG, "Accept icon resource: " + R.drawable.ic_check);
        notificationBuilder.addAction(R.drawable.ic_check, "Accept", acceptPendingIntent);
        
        Log.d(TAG, "Adding Decline action...");
        Log.d(TAG, "Decline PendingIntent: " + (declinePendingIntent != null ? "OK" : "NULL"));
        Log.d(TAG, "Decline icon resource: " + R.drawable.ic_close);
        notificationBuilder.addAction(R.drawable.ic_close, "Decline", declinePendingIntent);
        
        Log.d(TAG, "Notification builder created with actions");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Use inviteId as notification ID to ensure only one invite notification per invite
            int notificationId = inviteId != null ? inviteId.hashCode() : (int) System.currentTimeMillis();
            Log.d(TAG, "📱 Showing notification with ID: " + notificationId);
            
            // Build the notification and check actions
            android.app.Notification notification = notificationBuilder.build();
            Log.d(TAG, "📱 Notification has " + (notification.actions != null ? notification.actions.length : 0) + " actions");
            if (notification.actions != null && notification.actions.length > 0) {
                for (int i = 0; i < notification.actions.length; i++) {
                    Log.d(TAG, "📱 Action " + i + ": " + notification.actions[i].title);
                }
            } else {
                Log.e(TAG, "❌ No actions found in notification!");
            }
            
            notificationManager.notify(notificationId, notification);
            Log.d(TAG, "✅ Notification displayed successfully");
        } else {
            Log.e(TAG, "❌ NotificationManager is null");
        }
    }

    private void showAllianceInviteAcceptedNotification(String title, String body, String allianceId, String acceptedBy, String acceptedByUsername) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notification_type", "alliance_invite_accepted");
        intent.putExtra("alliance_id", allianceId);
        intent.putExtra("accepted_by", acceptedBy);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title != null ? title : "Alliance Invite Accepted")
                .setContentText(body != null ? body : "Your alliance invitation was accepted")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                .setColor(0xFF4CAF50); // Green color

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }


    private void showGenericNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
                .setContentTitle(title != null ? title : "Questify")
                .setContentText(body != null ? body : "You have a new notification")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.setBypassDnd(true); // Bypass Do Not Disturb
            channel.setSound(null, null); // Use default sound
            channel.enableLights(true);
            channel.setLightColor(0xFF6B35); // Orange light
            
            // Make channel non-dismissible
            channel.setShowBadge(true);
            channel.enableLights(true);
            channel.enableVibration(true);
            
            // Set as critical importance for maximum persistence
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            }

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Notification channel created: " + CHANNEL_ID);
            } else {
                Log.e(TAG, "❌ NotificationManager is null when creating channel");
            }
        } else {
            Log.d(TAG, "Android version < O, no channel needed");
        }
    }
}
