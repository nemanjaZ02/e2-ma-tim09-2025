package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String SERVER_URL = "http://192.168.1.57:5007"; // Your computer's IP address
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient httpClient;
    private final Gson gson;

    @Inject
    public NotificationService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }

    /**
     * Send alliance invite response (Accept/Decline)
     */
    public void sendAllianceInviteResponse(String action, String inviteId, String userId, String allianceId, String fromUserId, NotificationCallback callback) {
        Log.d(TAG, "=== SENDING ALLIANCE INVITE RESPONSE ===");
        Log.d(TAG, "Action: " + action);
        Log.d(TAG, "Invite ID: " + inviteId);
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "Alliance ID: " + allianceId);
        Log.d(TAG, "From User ID: " + fromUserId);
        Log.d(TAG, "Server URL: " + SERVER_URL);
        
        // Create request payload
        AllianceInviteResponseRequest request = new AllianceInviteResponseRequest(action, inviteId, userId, allianceId, fromUserId);
        String json = gson.toJson(request);
        
        Log.d(TAG, "Request JSON: " + json);
        
        RequestBody body = RequestBody.create(json, JSON);
        Request httpRequest = new Request.Builder()
                .url(SERVER_URL + "/alliance-invite-response")
                .post(body)
                .build();
        
        Log.d(TAG, "🚀 Making HTTP request to server...");
        
        httpClient.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "❌ Failed to send alliance invite response: " + e.getMessage());
                if (callback != null) {
                    callback.onError("Network error: " + e.getMessage());
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "📡 HTTP response received");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());
                
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Response body: " + responseBody);
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Alliance invite response sent successfully");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    Log.e(TAG, "❌ Failed to send alliance invite response: HTTP " + response.code() + ": " + response.message());
                    Log.e(TAG, "Response body: " + responseBody);
                    if (callback != null) {
                        callback.onError("HTTP " + response.code() + ": " + response.message());
                    }
                }
            }
        });
    }

    /**
     * Send alliance invitation notification
     */
    public void sendAllianceInviteNotification(String toUserId, String fromUsername, String allianceName, String inviteId, String allianceId, String fromUserId, NotificationCallback callback) {
        Log.d(TAG, "=== SENDING ALLIANCE INVITE NOTIFICATION ===");
        Log.d(TAG, "To User ID: " + toUserId);
        Log.d(TAG, "From Username: " + fromUsername);
        Log.d(TAG, "Alliance Name: " + allianceName);
        Log.d(TAG, "Invite ID: " + inviteId);
        Log.d(TAG, "Alliance ID: " + allianceId);
        Log.d(TAG, "From User ID: " + fromUserId);
        Log.d(TAG, "Server URL: " + SERVER_URL);
        
        AllianceInviteNotificationRequest request = new AllianceInviteNotificationRequest(
                toUserId,
                fromUsername,
                allianceName,
                inviteId,
                allianceId,
                fromUserId
        );

        sendNotification("alliance-invite", request, callback);
    }

    /**
     * Generic method to send notifications
     */
    private void sendNotification(String type, Object data, NotificationCallback callback) {
        Log.d(TAG, "=== SEND NOTIFICATION METHOD ===");
        Log.d(TAG, "Type: " + type);
        
        NotificationRequest request = new NotificationRequest(type, data);
        String json = gson.toJson(request);
        Log.d(TAG, "Request JSON: " + json);

        RequestBody body = RequestBody.create(json, JSON);
        String fullUrl = SERVER_URL + "/send-notification";
        Log.d(TAG, "Full URL: " + fullUrl);
        
        Request httpRequest = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .build();

        Log.d(TAG, "🚀 Making HTTP request to server...");
        httpClient.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "❌ HTTP request failed", e);
                Log.e(TAG, "Error message: " + e.getMessage());
                Log.e(TAG, "Error type: " + e.getClass().getSimpleName());
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "📡 HTTP response received");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());
                
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                Log.d(TAG, "Response body: " + responseBody);
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Notification sent successfully!");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    String errorMessage = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "❌ Failed to send notification: " + errorMessage);
                    Log.e(TAG, "Response body: " + responseBody);
                    if (callback != null) {
                        callback.onFailure(errorMessage);
                    }
                }
                response.close();
            }
        });
    }

    /**
     * Test server connection
     */
    public void testConnection(NotificationCallback callback) {
        Request request = new Request.Builder()
                .url(SERVER_URL + "/health")
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Server connection test failed", e);
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Server connection test successful");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    String errorMessage = "HTTP " + response.code() + ": " + response.message();
                    Log.e(TAG, "Server connection test failed: " + errorMessage);
                    if (callback != null) {
                        callback.onFailure(errorMessage);
                    }
                }
                response.close();
            }
        });
    }

    // Data classes for requests
    public static class NotificationRequest {
        public String type;
        public Object data;

        public NotificationRequest(String type, Object data) {
            this.type = type;
            this.data = data;
        }
    }

    public static class AllianceInviteNotificationRequest {
        public String toUserId;
        public String fromUsername;
        public String allianceName;
        public String inviteId;
        public String allianceId;
        public String fromUserId;

        public AllianceInviteNotificationRequest(String toUserId, String fromUsername, String allianceName, String inviteId, String allianceId, String fromUserId) {
            this.toUserId = toUserId;
            this.fromUsername = fromUsername;
            this.allianceName = allianceName;
            this.inviteId = inviteId;
            this.allianceId = allianceId;
            this.fromUserId = fromUserId;
        }
    }

    public static class AllianceInviteResponseRequest {
        public String action;
        public String inviteId;
        public String userId;
        public String allianceId;
        public String fromUserId;

        public AllianceInviteResponseRequest(String action, String inviteId, String userId, String allianceId, String fromUserId) {
            this.action = action;
            this.inviteId = inviteId;
            this.userId = userId;
            this.allianceId = allianceId;
            this.fromUserId = fromUserId;
        }
    }

    // Callback interface
    public interface NotificationCallback {
        void onSuccess();
        void onFailure(String error);
        default void onError(String error) {
            onFailure(error);
        }
    }
}
