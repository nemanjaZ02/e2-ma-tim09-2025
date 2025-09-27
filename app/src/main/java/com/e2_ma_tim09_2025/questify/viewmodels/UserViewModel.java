package com.e2_ma_tim09_2025.questify.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserViewModel extends ViewModel {

    private final UserService userService;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationStatus = new MutableLiveData<>();
    private final MutableLiveData<String> registrationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();
    private final MutableLiveData<String> changePasswordResult = new MutableLiveData<>();


    @Inject
    public UserViewModel(UserService userService) {
        this.userService = userService;
    }

    public LiveData<String> getChangePasswordResult() {
        return changePasswordResult;
    }
    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getRegistrationStatus() {
        return registrationStatus;
    }

    public LiveData<String> getRegistrationError() {
        return registrationError;
    }

    public LiveData<Boolean> getLoginStatus() {
        return loginStatus;
    }

    public void registerUser(String email, String password, String username, String avatarUri) {
        userService.registerNewUser(
                email, password, username, avatarUri,
                authResultTask -> {
                    boolean ok = authResultTask.isSuccessful();
                    Log.d("VM_REGISTER", "Auth result: " + ok, authResultTask.getException());
                    registrationStatus.postValue(ok);

                    if (!ok) {
                        Exception ex = authResultTask.getException();
                        String message;

                    if (ex instanceof FirebaseAuthWeakPasswordException) {
                        message = "Password is too weak. It should be at least 6 characters.";
                    } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                        message = "Invalid email format.";
                    } else if (ex instanceof FirebaseAuthUserCollisionException) {
                        message = "This email is already registered. Please log in instead.";
                    } else {
                        message = "Registration failed: " + ex.getMessage();
                    }

                    registrationError.postValue(message);
                    }
                },
                userSaveTask -> {
                    if (userSaveTask.isSuccessful()) {
                        String uid = userService.getCurrentUserId();
                        if (uid != null) fetchUser(uid);

                    } else {
                        Log.e("VM_REGISTER", "Saving user failed", userSaveTask.getException());
                        String message = "Could not save user profile. Please try again.";

                        if (userSaveTask.getException() != null && userSaveTask.getException().getMessage() != null) {
                            message = userSaveTask.getException().getMessage();
                        }
                        registrationError.postValue(message);
                    }
                        }
        );
    }

    public void loginUser(String email, String password) {
        userService.login(email, password, task -> loginStatus.postValue(task.isSuccessful()));
    }

    public void fetchUser(String uid) {
        userService.getUser(uid, (OnCompleteListener<DocumentSnapshot>) task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().toObject(User.class);
                userLiveData.postValue(user);
            }
        });
    }

    public void updateUser(User user) {
        userService.updateUser(user, task -> {
            if (task.isSuccessful()) {
                userLiveData.postValue(user);
            }
        });
    }

    public void logout() {
        userService.logout();
        userLiveData.postValue(null);
    }
    public void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        // Validation
        if (oldPassword == null || oldPassword.isEmpty() ||
                newPassword == null || newPassword.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {
            changePasswordResult.setValue("All fields are required");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            changePasswordResult.setValue("New passwords do not match");
            return;
        }

        if (newPassword.length() < 6) {
            changePasswordResult.setValue("Password must be at least 6 characters");
            return;
        }

        // Delegate actual Firebase operation to service
        userService.changePassword(oldPassword, newPassword, confirmPassword, task -> {
            if (task.isSuccessful()) {
                changePasswordResult.postValue("Password updated successfully");
            } else {
                changePasswordResult.postValue(task.getException().getMessage());
            }
        });
    }

//    public void deleteUser(String user) {
//        userService.deleteUser(user, task -> {
//            if (task.isSuccessful()) {
//                userLiveData.postValue(user);
//            }
//        });
//    }
}
