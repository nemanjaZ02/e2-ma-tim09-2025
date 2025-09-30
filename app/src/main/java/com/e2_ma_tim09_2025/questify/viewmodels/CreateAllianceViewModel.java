package com.e2_ma_tim09_2025.questify.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.AllianceService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class CreateAllianceViewModel extends ViewModel {
    private final MutableLiveData<List<User>> friendsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedFriendsCountLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAllianceCreatedLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private final UserService userService;
    private final AllianceService allianceService;
    private final Set<String> selectedFriendIds = new HashSet<>();

    @Inject
    public CreateAllianceViewModel(UserService userService, AllianceService allianceService) {
        this.userService = userService;
        this.allianceService = allianceService;
        selectedFriendsCountLiveData.setValue(0);
    }

    public LiveData<List<User>> getFriends() {
        return friendsLiveData;
    }

    public LiveData<Integer> getSelectedFriendsCount() {
        return selectedFriendsCountLiveData;
    }

    public LiveData<Boolean> getIsAllianceCreated() {
        return isAllianceCreatedLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public void loadFriends() {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            errorMessageLiveData.postValue("User not logged in");
            return;
        }

        userService.getFriends(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> friends = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        friends.add(user);
                    }
                }
                friendsLiveData.postValue(friends);
            } else {
                errorMessageLiveData.postValue("Failed to load friends: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            }
        });
    }

    public void setSelectedFriends(Set<String> selectedFriends) {
        selectedFriendIds.clear();
        selectedFriendIds.addAll(selectedFriends);
        selectedFriendsCountLiveData.postValue(selectedFriendIds.size());
    }
    
    public void toggleFriendSelection(String friendId) {
        if (selectedFriendIds.contains(friendId)) {
            selectedFriendIds.remove(friendId);
        } else {
            selectedFriendIds.add(friendId);
        }
        selectedFriendsCountLiveData.postValue(selectedFriendIds.size());
    }

    public void selectAllFriends() {
        List<User> friends = friendsLiveData.getValue();
        if (friends != null) {
            selectedFriendIds.clear();
            for (User friend : friends) {
                selectedFriendIds.add(friend.getId());
            }
            selectedFriendsCountLiveData.postValue(selectedFriendIds.size());
        }
    }

    public void createAlliance(String allianceName) {
        Log.d("CreateAllianceViewModel", "=== CREATE ALLIANCE CALLED ===");
        Log.d("CreateAllianceViewModel", "Alliance Name: " + allianceName);
        
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            Log.e("CreateAllianceViewModel", "❌ User not logged in");
            errorMessageLiveData.postValue("User not logged in");
            return;
        }
        
        Log.d("CreateAllianceViewModel", "Current User ID: " + currentUserId);

        List<String> invitedFriendIds = new ArrayList<>(selectedFriendIds);
        Log.d("CreateAllianceViewModel", "Selected Friend IDs: " + invitedFriendIds.toString());
        Log.d("CreateAllianceViewModel", "Number of friends to invite: " + invitedFriendIds.size());

        Log.d("CreateAllianceViewModel", "🚀 Calling allianceService.createAllianceWithInvites...");
        allianceService.createAllianceWithInvites(
                allianceName,
                currentUserId,
                invitedFriendIds,
                task -> {
                    if (task.isSuccessful()) {
                        Log.d("CreateAllianceViewModel", "✅ Alliance creation completed successfully");
                        isAllianceCreatedLiveData.postValue(true);
                    } else {
                        Log.e("CreateAllianceViewModel", "❌ Alliance creation failed", task.getException());
                        errorMessageLiveData.postValue("Failed to create alliance: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                }
        );
    }
}