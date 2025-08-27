package com.e2_ma_tim09_2025.questify.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentSnapshot;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserViewModel extends ViewModel {

    private final UserService userService;

    // LiveData to observe user data
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();

    @Inject
    public UserViewModel(UserService userService) {
        this.userService = userService;
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getRegistrationStatus() {
        return registrationStatus;
    }

    public LiveData<Boolean> getLoginStatus() {
        return loginStatus;
    }

    // Register a new user
    // Register a new user
    public void registerUser(String email, String password, String username, String avatarUri) {
        userService.registerNewUser(
                email, password, username, avatarUri,
                authResultTask -> {
                    boolean ok = authResultTask.isSuccessful();
                    Log.d("VM_REGISTER", "Auth result: " + ok, authResultTask.getException());
                    registrationStatus.postValue(ok);
                },
                userSaveTask -> {
                    if (userSaveTask.isSuccessful()) {
                        String uid = userService.getCurrentUserId();
                        if (uid != null) fetchUser(uid);
                    } else {
                        Log.e("VM_REGISTER", "Saving user failed", userSaveTask.getException());
                    }
                }
        );
    }



    // Login user
    public void loginUser(String email, String password) {
        userService.login(email, password, task -> loginStatus.postValue(task.isSuccessful()));
    }

    // Fetch user by UID
    public void fetchUser(String uid) {
        userService.getUser(uid, (OnCompleteListener<DocumentSnapshot>) task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().toObject(User.class);
                userLiveData.postValue(user);
            }
        });
    }

    // Update user
    public void updateUser(User user) {
        userService.updateUser(user, task -> {
            if (task.isSuccessful()) {
                userLiveData.postValue(user);
            }
        });
    }

    // Logout
    public void logout() {
        userService.logout();
        userLiveData.postValue(null);
    }
}
