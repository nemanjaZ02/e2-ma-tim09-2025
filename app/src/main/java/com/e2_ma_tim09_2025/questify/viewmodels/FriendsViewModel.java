package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FriendsViewModel extends ViewModel {
    private final MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
    private final UserService service;

    @Inject
    public FriendsViewModel(UserService userService) {
        this.service = userService;
    }

    public LiveData<List<User>> getUsers() {
        return usersLiveData;
    }

    public void fetchUsers() {
        String userId = service.getCurrentUserId();
        service.getFriends(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> friends = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    User user = doc.toObject(User.class);
                    if (user != null) friends.add(user);
                }
                usersLiveData.postValue(friends);
            }
        });

    }
    public void getCurrentUserId(){
        service.getCurrentUserId();
    }
}