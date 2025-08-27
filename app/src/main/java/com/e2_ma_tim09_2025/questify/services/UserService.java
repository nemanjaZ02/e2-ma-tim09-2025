package com.e2_ma_tim09_2025.questify.services;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {

    private final UserRepository userRepository;

    @Inject
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register new user with default values
    public void registerNewUser(String email, String password, String username, String avatarUri,
                                OnCompleteListener<AuthResult> authListener,
                                OnCompleteListener<Void> userSaveListener) {

        // Create user object with default values
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setAvatar(avatarUri);
        newUser.setLevel(1);
        newUser.setTitle("Novice");
        newUser.setPowerPoints(100);
        newUser.setExperiencePoints(0);
        newUser.setCoins(0);
        newUser.setBadges(new ArrayList<>());
        newUser.setEquipment(new ArrayList<>());
        newUser.setQrCode(null); // generate later

        // Call repository to register
        userRepository.registerUser(email, password, newUser, authListener, userSaveListener);
    }

    // Login user
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        userRepository.loginUser(email, password, listener);
    }

    // Update user
    public void updateUser(User user, OnCompleteListener<Void> listener) {
        userRepository.updateUser(user, listener);
    }

    // Get user by UID
    public void getUser(String uid, OnCompleteListener<com.google.firebase.firestore.DocumentSnapshot> listener) {
        userRepository.getUser(uid, listener);
    }

    // Logout
    public void logout() {
        userRepository.logout();
    }

    public String getCurrentUserId() {
        return userRepository.getCurrentUserId();
    }


}
