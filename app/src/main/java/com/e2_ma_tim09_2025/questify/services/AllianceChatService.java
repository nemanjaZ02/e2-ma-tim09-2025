package com.e2_ma_tim09_2025.questify.services;

import com.e2_ma_tim09_2025.questify.models.AllianceMessage;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.repositories.AllianceChatRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
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

    @Inject
    public AllianceChatService(AllianceChatRepository allianceChatRepository, UserRepository userRepository) {
        this.allianceChatRepository = allianceChatRepository;
        this.userRepository = userRepository;
    }

    /**
     * Send a message to alliance chat
     */
    public void sendMessage(String allianceId, String senderId, String messageText, OnCompleteListener<Boolean> listener) {
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
