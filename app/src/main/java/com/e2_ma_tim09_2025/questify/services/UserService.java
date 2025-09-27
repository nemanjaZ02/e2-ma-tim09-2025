package com.e2_ma_tim09_2025.questify.services;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

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

    public void registerNewUser(String email, String password, String username, String avatarUri,
                                OnCompleteListener<AuthResult> authListener,
                                OnCompleteListener<Void> userSaveListener) {

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setAvatar(avatarUri);
        newUser.setLevel(0);
        newUser.setTitle("Novice");
        newUser.setPowerPoints(0);
        newUser.setExperiencePoints(0);
        newUser.setCoins(0);
        newUser.setBadges(new ArrayList<>());
        newUser.setEquipment(new ArrayList<>());
        newUser.setQrCode(null); // generate later

        userRepository.registerUser(email, password, newUser, authListener, userSaveListener);
    }

    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        userRepository.loginUser(email, password, listener);
    }

    public void updateUser(User user, OnCompleteListener<Void> listener) {
        userRepository.updateUser(user, listener);
    }

    public void getUser(String uid, OnCompleteListener<com.google.firebase.firestore.DocumentSnapshot> listener) {
        userRepository.getUser(uid, listener);
    }

    public void logout() {
        userRepository.logout();
    }

    public String getCurrentUserId() {
        return userRepository.getCurrentUserId();
    }

    public void deleteUser(String user, OnCompleteListener<Void> listener) {
        userRepository.deleteUser(user, listener);
    }

    private int getRequiredXpForNextLevel(int previousLevelXP){
        double newXP = previousLevelXP * 2 + previousLevelXP / 2;
            return (int) (Math.ceil(newXP / 100.0) * 100);
    }

    private int getRequiredXpForLevel(int level) {
        if (level == 0) return 200;
        int req = 200;
        for (int i = 1; i <= level; i++) {
            req = getRequiredXpForNextLevel(req);
        }
        return req;
    }
    public int calculateXpForImportance(TaskPriority importance, int userLevel) {
        int baseXp;
        switch (importance) {
            case NORMAL:
                baseXp = 1;
                break;
            case IMPORTANT:
                baseXp = 3;
                break;
            case CRUCIAL:
                baseXp = 10;
                break;
            case SPECIAL:
                baseXp = 100;
                break;
            default:
                baseXp = 1;
        }

        double xp = baseXp;
        for (int level = 1; level <= userLevel; level++) {
            xp *= 1.5; // increase by 50% per level
        }

        return (int) Math.round(xp);
    }
    public int calculateXpForDifficulty(TaskDifficulty difficulty, int userLevel) {
        int baseXp;
        switch (difficulty) {
            case VERY_EASY:
                baseXp = 1;
                break;
            case EASY:
                baseXp = 3;
                break;
            case HARD:
                baseXp = 7;
                break;
            case EXTREME:
                baseXp = 20;
                break;
            default:
                baseXp = 1; // fallback
        }

        double xp = baseXp;
        for (int level = 1; level <= userLevel; level++) {
            xp *= 1.5; // increase by 50% per level
        }

        return (int) Math.round(xp);
    }

    private int getPowerPointsForLevel(int level) {
        if (level == 1) return 40;

        int pp = 40;
        for (int i = 2; i <= level; i++) {
            pp = (int) Math.round(pp * 1.75);
        }
        return pp;
    }

    private String getTitleForLevel(int level) {
        switch (level) {
            case 0:
                return "Novice";    // početna titula
            case 1:
                return "Adventurer";
            case 2:
                return "Warrior";
            case 3:
                return "Champion";
            default:
                return "Legend";    // za sve iznad 3
        }
    }

    public void addXP(String id, int newXP, OnCompleteListener<Void> listener) {
        getUser(id, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();

                User user = document.toObject(User.class);
                if (user == null) {
                    if (listener != null) {
                        listener.onComplete(Tasks.forException(new Exception("User object is null")));
                    }
                    return;
                }

                int level = user.getLevel();
                int pp = user.getPowerPoints();
                String title = user.getTitle();

                // Total cumulative XP (never resets)
                int totalXp = user.getExperiencePoints() + newXP;

                // XP used for level-up calculation
                int xpForLevel = user.getExperiencePoints() % getRequiredXpForLevel(level) + newXP;

                // Check if user levels up
                int requiredForNext = getRequiredXpForLevel(level);
                while (xpForLevel >= requiredForNext) {
                    xpForLevel -= requiredForNext;
                    level++;

                    // Add PP for this level
                    pp += getPowerPointsForLevel(level);

                    // Update title for this level
                    title = getTitleForLevel(level);

                    // Get new threshold for next level
                    requiredForNext = getRequiredXpForLevel(level);
                }

                // Update user object
                user.setLevel(level);
                user.setExperiencePoints(totalXp);  // cumulative XP
                user.setPowerPoints(pp);
                user.setTitle(title);


                // Save to Firestore
                updateUser(user, listener);

            } else {
                if (listener != null) {
                    listener.onComplete(Tasks.forException(new Exception("User not found or read failed")));
                }
            }
        });
    }
    public void changePassword(String oldPassword, String newPassword, String confirmPassword, OnCompleteListener<Void> listener) {
        FirebaseUser currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            if (listener != null) {
                listener.onComplete(Tasks.forException(new Exception("No authenticated user found.")));
            }
            return;
        }

        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            if (listener != null) {
                listener.onComplete(Tasks.forException(new Exception("New passwords do not match.")));
            }
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(listener);
                    } else {
                        if (listener != null) {
                            listener.onComplete(Tasks.forException(new Exception("Old password is incorrect.")));
                        }
                    }
                });
    }

    public FirebaseUser getCurrentUser(){
        return userRepository.getCurrentUser();
    }
}

