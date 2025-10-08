package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import com.e2_ma_tim09_2025.questify.models.AllianceMessage;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;
import com.e2_ma_tim09_2025.questify.repositories.AllianceChatRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.e2_ma_tim09_2025.questify.services.NotificationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AllianceChatService {
    private final AllianceChatRepository allianceChatRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SpecialTaskService specialTaskService;

    @Inject
    public AllianceChatService(AllianceChatRepository allianceChatRepository, UserRepository userRepository, NotificationService notificationService, SpecialTaskService specialTaskService) {
        this.allianceChatRepository = allianceChatRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.specialTaskService = specialTaskService;
    }

    /**
     * Send a message to alliance chat
     */
    public void sendMessage(String allianceId, String senderId, String messageText, OnCompleteListener<Boolean> listener) {
        allianceChatRepository.hasUserSentMessageToday(senderId, task -> {
            if (task.isSuccessful() && Boolean.FALSE.equals(task.getResult())) {
                specialTaskService.completeSpecialTaskForAllAlliances(senderId, SpecialTaskType.ALLIANCE_MESSAGE_DAILY, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(com.google.android.gms.tasks.Task<Boolean> specialTaskResult) {
                        if (specialTaskResult.isSuccessful()) {
                            Log.d("BossService", "✅ Special task completed successfully");
                        } else {
                            Log.e("BossService", "❌ Failed to complete special task", specialTaskResult.getException());
                        }
                    }
                });
            } else {
                Log.d("BossService", "User already sent a message today or check failed, skipping special task");
            }
        });

        // First get sender's name
        userRepository.getUser(senderId, userTask -> {
            if (!userTask.isSuccessful() || userTask.getResult() == null) {
                listener.onComplete(Tasks.forException(
                    userTask.getException() != null ? 
                    userTask.getException() : 
                    new Exception("Failed to get user data")));
                return;
            }

            User sender = userTask.getResult().toObject(User.class);
            if (sender == null) {
                listener.onComplete(Tasks.forException(new Exception("User not found")));
                return;
            }

            // Create message
            String messageId = UUID.randomUUID().toString();
            AllianceMessage message = new AllianceMessage(
                messageId, 
                allianceId, 
                senderId, 
                sender.getUsername(), 
                messageText
            );

            // Send message
            allianceChatRepository.sendMessage(message, sendTask -> {
                if (sendTask.isSuccessful()) {
                    // Message sent successfully, now send notifications to alliance members
                    sendChatNotifications(allianceId, senderId, sender.getUsername(), messageText);
                    listener.onComplete(Tasks.forResult(true));
                } else {
                    listener.onComplete(Tasks.forException(
                        sendTask.getException() != null ? 
                        sendTask.getException() : 
                        new Exception("Failed to send message")));
                }
            });
        });
    }

    /**
     * Get all messages for an alliance
     */
    public void getAllianceMessages(String allianceId, OnCompleteListener<List<AllianceMessage>> listener) {
        allianceChatRepository.getAllianceMessages(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<AllianceMessage> messages = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    AllianceMessage message = doc.toObject(AllianceMessage.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                // Sort by timestamp in ascending order (oldest first)
                messages.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                listener.onComplete(Tasks.forResult(messages));
            } else {
                listener.onComplete(Tasks.forException(
                    task.getException() != null ? 
                    task.getException() : 
                    new Exception("Failed to load messages")));
            }
        });
    }

    /**
     * Get recent messages for an alliance (last 50)
     */
    public void getRecentMessages(String allianceId, OnCompleteListener<List<AllianceMessage>> listener) {
        allianceChatRepository.getRecentAllianceMessages(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<AllianceMessage> messages = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    AllianceMessage message = doc.toObject(AllianceMessage.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                // Sort by timestamp in ascending order (oldest first)
                messages.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                listener.onComplete(Tasks.forResult(messages));
            } else {
                listener.onComplete(Tasks.forException(
                    task.getException() != null ? 
                    task.getException() : 
                    new Exception("Failed to load recent messages")));
            }
        });
    }

    /**
     * Check for new messages since last timestamp
     */
    public void checkForNewMessages(String allianceId, long lastMessageTimestamp, OnCompleteListener<List<AllianceMessage>> listener) {
        allianceChatRepository.listenToNewMessages(allianceId, lastMessageTimestamp, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<AllianceMessage> newMessages = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    AllianceMessage message = doc.toObject(AllianceMessage.class);
                    if (message != null && message.getTimestamp() > lastMessageTimestamp) {
                        newMessages.add(message);
                    }
                }
                // Sort by timestamp in ascending order
                newMessages.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                listener.onComplete(Tasks.forResult(newMessages));
            } else {
                listener.onComplete(Tasks.forException(
                    task.getException() != null ? 
                    task.getException() : 
                    new Exception("Failed to check for new messages")));
            }
        });
    }

    /**
     * Start listening to messages in real-time
     */
    public void startListeningToMessages(String allianceId, EventListener<List<AllianceMessage>> listener) {
        allianceChatRepository.startListeningToMessages(allianceId, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle error
                    listener.onEvent(null, e);
                    return;
                }
                
                if (snapshot != null) {
                    List<AllianceMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        AllianceMessage message = doc.toObject(AllianceMessage.class);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                    // Sort by timestamp in ascending order (oldest first)
                    messages.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                    listener.onEvent(messages, null);
                }
            }
        });
    }
    
    /**
     * Stop listening to messages
     */
    public void stopListeningToMessages() {
        allianceChatRepository.stopListeningToMessages();
    }

    /**
     * Send chat notifications to all alliance members except the sender
     */
    private void sendChatNotifications(String allianceId, String senderId, String senderUsername, String messageText) {
        System.out.println("🔔 DEBUG: Starting sendChatNotifications");
        System.out.println("🔔 DEBUG: allianceId=" + allianceId);
        System.out.println("🔔 DEBUG: senderId=" + senderId);
        System.out.println("🔔 DEBUG: senderUsername=" + senderUsername);
        System.out.println("🔔 DEBUG: messageText=" + messageText);
        
        // Get alliance information to get member list and alliance name
        userRepository.getUser(senderId, userTask -> {
            System.out.println("🔔 DEBUG: User fetch task completed. Success: " + userTask.isSuccessful());
            
            if (userTask.isSuccessful() && userTask.getResult() != null) {
                User sender = userTask.getResult().toObject(User.class);
                System.out.println("🔔 DEBUG: Sender user object: " + (sender != null ? "Found" : "Null"));
                
                if (sender != null) {
                    System.out.println("🔔 DEBUG: Sender allianceId=" + sender.getAllianceId());
                    System.out.println("🔔 DEBUG: Expected allianceId=" + allianceId);
                    System.out.println("🔔 DEBUG: Alliance IDs match: " + (sender.getAllianceId() != null && sender.getAllianceId().equals(allianceId)));
                }
                
                if (sender != null) {
                    // User exists, send notification regardless of allianceId in user document
                    // The server will validate alliance membership by checking the alliance document
                    String allianceName = "Alliance"; // Default name, could be improved by getting actual alliance name
                    
                    System.out.println("🔔 DEBUG: User found, sending notification (alliance validation handled by server)");
                    System.out.println("🔔 DEBUG: Calling notificationService.sendAllianceChatNotification");
                    
                    notificationService.sendAllianceChatNotification(
                        allianceId,
                        senderId,
                        senderUsername,
                        messageText,
                        allianceName,
                        new NotificationService.NotificationCallback() {
                            @Override
                            public void onSuccess() {
                                System.out.println("🔔 DEBUG: ✅ Notification sent successfully!");
                            }

                            @Override
                            public void onFailure(String error) {
                                System.out.println("🔔 DEBUG: ❌ Notification failed: " + error);
                            }
                        }
                    );
                } else {
                    System.out.println("🔔 DEBUG: ❌ User not found - not sending notification");
                }
            } else {
                System.out.println("🔔 DEBUG: ❌ Failed to get user data for notification");
                if (userTask.getException() != null) {
                    System.out.println("🔔 DEBUG: Exception: " + userTask.getException().getMessage());
                }
            }
        });
    }

    /**
     * Delete a message (for moderation)
     */
    public void deleteMessage(String messageId, OnCompleteListener<Boolean> listener) {
        allianceChatRepository.deleteMessage(messageId, task -> {
            if (task.isSuccessful()) {
                listener.onComplete(Tasks.forResult(true));
            } else {
                listener.onComplete(Tasks.forException(
                    task.getException() != null ? 
                    task.getException() : 
                    new Exception("Failed to delete message")));
            }
        });
    }
}
