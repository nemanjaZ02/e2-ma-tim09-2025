package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.AllianceMessage;
import com.e2_ma_tim09_2025.questify.services.AllianceChatService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AllianceChatViewModel extends ViewModel {
    private final AllianceChatService allianceChatService;
    private final UserService userService;
    private final Executor executor = Executors.newSingleThreadExecutor();

    // LiveData for UI
    private final MutableLiveData<List<AllianceMessage>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> messageSent = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();
    private final MutableLiveData<String> currentAllianceId = new MutableLiveData<>();

    @Inject
    public AllianceChatViewModel(AllianceChatService allianceChatService, UserService userService) {
        this.allianceChatService = allianceChatService;
        this.userService = userService;
    }

    // Getters for LiveData
    public LiveData<List<AllianceMessage>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getMessageSent() {
        return messageSent;
    }

    public LiveData<String> getCurrentUserId() {
        return currentUserId;
    }

    public LiveData<String> getCurrentAllianceId() {
        return currentAllianceId;
    }

    /**
     * Initialize chat with alliance ID
     */
    public void initializeChat(String allianceId) {
        currentAllianceId.setValue(allianceId);
        currentUserId.setValue(userService.getCurrentUserId());
        loadMessages();
    }

    /**
     * Load messages for the current alliance
     */
    public void loadMessages() {
        String allianceId = currentAllianceId.getValue();
        if (allianceId == null) {
            errorMessage.setValue("No alliance selected");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        executor.execute(() -> {
            allianceChatService.getRecentMessages(allianceId, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    messages.postValue(task.getResult());
                    isLoading.postValue(false);
                } else {
                    String error = "Failed to load messages";
                    if (task.getException() != null) {
                        error += ": " + task.getException().getMessage();
                    }
                    errorMessage.postValue(error);
                    isLoading.postValue(false);
                }
            });
        });
    }

    /**
     * Send a message
     */
    public void sendMessage(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            errorMessage.setValue("Message cannot be empty");
            return;
        }

        String allianceId = currentAllianceId.getValue();
        String userId = currentUserId.getValue();
        
        if (allianceId == null || userId == null) {
            errorMessage.setValue("Not properly initialized");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        executor.execute(() -> {
            allianceChatService.sendMessage(allianceId, userId, messageText.trim(), task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult()) {
                    messageSent.postValue(true);
                    isLoading.postValue(false);
                    // Reload messages to show the new one
                    loadMessages();
                } else {
                    String error = "Failed to send message";
                    if (task.getException() != null) {
                        error += ": " + task.getException().getMessage();
                    }
                    errorMessage.postValue(error);
                    isLoading.postValue(false);
                }
            });
        });
    }

    /**
     * Check for new messages
     */
    public void checkForNewMessages() {
        String allianceId = currentAllianceId.getValue();
        if (allianceId == null) return;

        List<AllianceMessage> currentMessages = messages.getValue();
        long lastTimestamp = 0;
        
        if (currentMessages != null && !currentMessages.isEmpty()) {
            lastTimestamp = currentMessages.get(currentMessages.size() - 1).getTimestamp();
        }

        allianceChatService.checkForNewMessages(allianceId, lastTimestamp, task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                // Add new messages to existing list
                List<AllianceMessage> updatedMessages = messages.getValue();
                if (updatedMessages != null) {
                    updatedMessages.addAll(task.getResult());
                    messages.postValue(updatedMessages);
                }
            }
        });
    }

    /**
     * Refresh messages
     */
    public void refreshMessages() {
        loadMessages();
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * Reset message sent status
     */
    public void resetMessageSent() {
        messageSent.setValue(false);
    }
}
