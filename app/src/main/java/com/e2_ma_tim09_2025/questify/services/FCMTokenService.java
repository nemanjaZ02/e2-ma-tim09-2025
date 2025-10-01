package com.e2_ma_tim09_2025.questify.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class FCMTokenService {
    private static final String TAG = "FCMTokenService";
    private static final String PREFS_NAME = "fcm_token_prefs";
    private static final String KEY_LAST_USER_ID = "last_user_id";
    private static final String KEY_DEVICE_TOKEN = "device_token";

    private final Context context;
    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;

    @Inject
    public FCMTokenService(@ApplicationContext Context context, UserRepository userRepository, FirebaseAuth firebaseAuth) {
        this.context = context;
        this.userRepository = userRepository;
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Generate and store FCM token for the current user
     * Handles edge cases for same device, different users
     */
    public void generateAndStoreToken(OnCompleteListener<Void> listener) {
        Log.d(TAG, "=== FCM TOKEN GENERATION STARTED ===");
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "❌ No authenticated user found");
            if (listener != null) {
                // Create a failed task using Tasks.forException and cast to Task<Void>
                com.google.android.gms.tasks.Tasks.<Void>forException(new Exception("No authenticated user"))
                    .addOnCompleteListener(listener);
            }
            return;
        }
        
        String currentUserId = currentUser.getUid();
        Log.d(TAG, "✅ Authenticated user found: " + currentUserId);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastUserId = prefs.getString(KEY_LAST_USER_ID, null);
        String storedToken = prefs.getString(KEY_DEVICE_TOKEN, null);
        
        Log.d(TAG, "Last user ID from prefs: " + lastUserId);
        Log.d(TAG, "Stored token from prefs: " + (storedToken != null ? "EXISTS" : "NULL"));

        // Check if this is a different user on the same device
        if (lastUserId != null && !lastUserId.equals(currentUserId)) {
            Log.d(TAG, "🔄 Different user logged in on same device. Clearing old token.");
            clearTokenForUser(lastUserId);
        }

        // Generate new token
        Log.d(TAG, "🔑 Requesting FCM token from Firebase...");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "❌ Failed to get FCM token", task.getException());
                        if (listener != null) {
                            com.google.android.gms.tasks.Tasks.<Void>forException(task.getException())
                                .addOnCompleteListener(listener);
                        }
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "✅ Generated FCM token: " + token);

                    // Store token in SharedPreferences
                    Log.d(TAG, "💾 Storing token in SharedPreferences...");
                    prefs.edit()
                            .putString(KEY_LAST_USER_ID, currentUserId)
                            .putString(KEY_DEVICE_TOKEN, token)
                            .apply();
                    Log.d(TAG, "✅ Token stored in SharedPreferences");

                    // Update user's FCM tokens in Firestore
                    Log.d(TAG, "🔥 Updating user's FCM tokens in Firestore...");
                    updateUserFCMTokens(currentUserId, token, listener);
                });
    }

    /**
     * Update user's FCM tokens in Firestore
     * Adds new token if not already present
     */
    private void updateUserFCMTokens(String userId, String newToken, OnCompleteListener<Void> listener) {
        Log.d(TAG, "=== UPDATING USER FCM TOKENS ===");
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "New Token: " + newToken);
        
        userRepository.getUser(userId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                Log.e(TAG, "❌ Failed to get user data for FCM token update");
                if (listener != null) {
                    com.google.android.gms.tasks.Tasks.<Void>forException(new Exception("Failed to get user data"))
                        .addOnCompleteListener(listener);
                }
                return;
            }

            Log.d(TAG, "✅ User data retrieved successfully");
            User user = task.getResult().toObject(User.class);
            if (user == null) {
                Log.e(TAG, "❌ User object is null");
                if (listener != null) {
                    com.google.android.gms.tasks.Tasks.<Void>forException(new Exception("User object is null"))
                        .addOnCompleteListener(listener);
                }
                return;
            }

            List<String> fcmTokens = user.getFcmTokens();
            if (fcmTokens == null) {
                fcmTokens = new ArrayList<>();
                Log.d(TAG, "📝 Created new FCM tokens list");
            } else {
                Log.d(TAG, "📝 Existing FCM tokens count: " + fcmTokens.size());
            }

            // Add token if not already present
            if (!fcmTokens.contains(newToken)) {
                fcmTokens.add(newToken);
                user.setFcmTokens(fcmTokens);
                Log.d(TAG, "➕ Added new FCM token to user. Total tokens: " + fcmTokens.size());

                userRepository.updateUser(user, updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d(TAG, "✅ FCM token added to user successfully: " + userId);
                        Log.d(TAG, "=== FCM TOKEN GENERATION COMPLETED SUCCESSFULLY ===");
                    } else {
                        Log.e(TAG, "❌ Failed to update user FCM tokens", updateTask.getException());
                    }
                    if (listener != null) {
                        listener.onComplete(updateTask);
                    }
                });
            } else {
                Log.d(TAG, "ℹ️ FCM token already exists for user: " + userId);
                Log.d(TAG, "=== FCM TOKEN GENERATION COMPLETED (TOKEN ALREADY EXISTS) ===");
                if (listener != null) {
                    com.google.android.gms.tasks.Tasks.<Void>forResult(null)
                        .addOnCompleteListener(listener);
                }
            }
        });
    }

    /**
     * Clear FCM token for a specific user (when different user logs in on same device)
     */
    private void clearTokenForUser(String userId) {
        userRepository.getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    String storedToken = prefs.getString(KEY_DEVICE_TOKEN, null);
                    
                    if (storedToken != null) {
                        List<String> fcmTokens = user.getFcmTokens();
                        if (fcmTokens != null && fcmTokens.contains(storedToken)) {
                            fcmTokens.remove(storedToken);
                            user.setFcmTokens(fcmTokens);
                            
                            userRepository.updateUser(user, updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Log.d(TAG, "FCM token removed for user: " + userId);
                                } else {
                                    Log.e(TAG, "Failed to remove FCM token for user: " + userId);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * Get current FCM token for this device
     */
    public void getCurrentToken(OnCompleteListener<String> listener) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(listener);
    }

    /**
     * Refresh FCM token (call when token is refreshed)
     */
    public void onTokenRefresh() {
        Log.d(TAG, "FCM token refreshed");
        generateAndStoreToken(null);
    }

    /**
     * Clean up tokens for current user (call on logout)
     */
    public void cleanupTokensForCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String storedToken = prefs.getString(KEY_DEVICE_TOKEN, null);
            
            if (storedToken != null) {
                clearTokenForUser(currentUser.getUid());
            }
            
            // Clear SharedPreferences
            prefs.edit().clear().apply();
        }
    }
}
