package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OtherProfileViewModel extends ViewModel {
    private final UserService userService; // your service
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    @Inject
    public OtherProfileViewModel(UserService userService) {
        this.userService = userService;
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void fetchUser(String uid) {
        userService.getUser(uid, (OnCompleteListener<DocumentSnapshot>) task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().toObject(User.class);
                userLiveData.postValue(user);
            }
        });
    }
    public void addFriend(String userId, String friendId) {
        userService.addFriend( friendId);
    }
}
