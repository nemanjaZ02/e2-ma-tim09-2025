package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AllUsersViewModel extends ViewModel {
    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final UserService service;
    @Inject
    public AllUsersViewModel(UserService userService) {
        this.service = userService;
    }

    public LiveData<List<User>> getUsers() {
        return usersLiveData;
    }

    public void fetchUsers() {
        service.getAllNonFriendUsers(users -> usersLiveData.postValue(users));
    }


    private final MutableLiveData<String> friendAddedLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> friendAddMessageLiveData = new MutableLiveData<>();

    public LiveData<String> getFriendAddedLiveData() {
        return friendAddedLiveData;
    }
    
    public LiveData<String> getFriendAddMessageLiveData() {
        return friendAddMessageLiveData;
    }

    public void addFriend(String friendId) {
        service.addFriend(friendId, task -> {
            if (task.isSuccessful()) {
                Boolean result = task.getResult();
                if (result != null && result) {
                    // Friend was successfully added
                    friendAddedLiveData.setValue(friendId);
                    friendAddMessageLiveData.setValue("Friend added successfully!");
                } else {
                    // User is already a friend
                    friendAddMessageLiveData.setValue("This user is already your friend!");
                }
            } else {
                // Handle error
                String errorMessage = "Failed to add friend: " + 
                    (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                friendAddMessageLiveData.setValue(errorMessage);
            }
        });
    }
    public interface FriendAddCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

}
