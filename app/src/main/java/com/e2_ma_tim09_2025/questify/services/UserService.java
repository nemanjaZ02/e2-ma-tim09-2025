package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.models.enums.EquipmentType;
import com.e2_ma_tim09_2025.questify.models.enums.TaskDifficulty;
import com.e2_ma_tim09_2025.questify.models.enums.TaskPriority;
import com.e2_ma_tim09_2025.questify.repositories.BossRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService {

    private final UserRepository userRepository;
    private final BossRepository bossRepository;
    private final BossService bossService;
    private final FirebaseFirestore db;
    private final CollectionReference usersRef ;


    @Inject
    public UserService(UserRepository userRepository, BossRepository bossRepository, BossService bossService) {
        this.userRepository = userRepository;
        this.bossRepository = bossRepository;
        this.bossService = bossService;
        this.db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("users");
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
        newUser.setQrCode(null);
        newUser.setAllianceId(null);
        newUser.setFriends(new ArrayList<>());

        userRepository.registerUser(email, password, newUser, authTask -> {
            if (authListener != null) authListener.onComplete(authTask);

            if (authTask.isSuccessful() && authTask.getResult() != null) {
                String newUserId = authTask.getResult().getUser().getUid();

                Boss newBoss = new Boss(
                        BossStatus.INACTIVE,
                        newUserId,
                        200,
                        200,
                        200,
                        0
                );

                BossRepository bossRepository = new BossRepository();
                bossRepository.createBoss(newBoss, bossTask -> {
                    if (bossTask.isSuccessful()) {
                        Log.d("REGISTER", "Boss created for user: " + newUserId);
                    } else {
                        Log.e("REGISTER", "Boss failed creation.", bossTask.getException());
                    }
                });

            }

        }, userSaveListener);
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
                int originalLevel = level;

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

                // If user leveled up activate Boss
                if (level > originalLevel) {
                    bossRepository.getBossByUserId(user.getId(), work -> {
                        if (work.isSuccessful() && work.getResult() != null && work.getResult().exists()) {
                            Boss boss = work.getResult().toObject(Boss.class);
                            if (boss != null) {
                                boss.setStatus(BossStatus.ACTIVE);
                                bossService.calculateHitChanceAsync(user.getId(), originalLevel, hitChance -> {

                                    boss.setHitChance(hitChance);

                                    bossRepository.updateBoss(boss, updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Log.d("UserService", "Boss activated and updated for user: " + user.getId());
                                        } else {
                                            Log.e("UserService", "Failed to update boss", updateTask.getException());
                                        }
                                    });
                                });
                            }
                        } else {
                            Log.e("UserService", "Getting boss for this user failed.", work.getException());
                        }
                    });
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
    public void addFriend(String friendId) {
        String currentUserId = getCurrentUserId(); // your method to get current user
        if (currentUserId != null) {
            usersRef.document(currentUserId)
                    .update("friends", FieldValue.arrayUnion(friendId))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("UserService", "Friend added successfully");
                            // Optional: trigger LiveData or UI update here
                        } else {
                            Log.e("UserService", "Failed to add friend", task.getException());
                        }
                    });
        }
    }


    public void getAllFriends(String userId, OnCompleteListener<List<User>> listener) {
        usersRef.document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User currentUser = task.getResult().toObject(User.class);
                        List<String> friendIds = currentUser.getFriends();

                        if (friendIds == null || friendIds.isEmpty()) {
                            listener.onComplete(Tasks.forResult(Collections.emptyList()));
                            return;
                        }

                        // Fetch all friend users
                        List<Task<DocumentSnapshot>> friendTasks = new ArrayList<>();
                        for (String fid : friendIds) {
                            friendTasks.add(usersRef.document(fid).get());
                        }

                        Tasks.whenAllSuccess(friendTasks)
                                .addOnSuccessListener(results -> {
                                    List<User> friends = new ArrayList<>();
                                    for (Object obj : results) {
                                        DocumentSnapshot doc = (DocumentSnapshot) obj;
                                        User friend = doc.toObject(User.class);
                                        if (friend != null) friends.add(friend);
                                    }
                                    listener.onComplete(Tasks.forResult(friends));
                                })
                                .addOnFailureListener(e -> {
                                    listener.onComplete(Tasks.forException(e));
                                });

                    } else {
                        listener.onComplete(Tasks.forException(
                                task.getException() != null ? task.getException() : new Exception("User not found")
                        ));
                    }
                });
    }


    public FirebaseUser getCurrentUser(){
        return userRepository.getCurrentUser();
    }
    public void getAllUsers(OnCompleteListener<QuerySnapshot> listener){
        userRepository.getAllUsers(listener);
    }
    public void getAllNonFriendUsers(UserRepository.UsersCallback callback) {
        userRepository.getAllNonFriendUsers(callback);
    }


    public void getFriends(String userId, OnCompleteListener<QuerySnapshot> listener) {
        userRepository.getFriends(userId, listener);
    }

    /**
     * Get user's equipment list
     * Business logic: Provides equipment data for user
     */
    public void getUserEquipment(String userId, OnCompleteListener<List<MyEquipment>> listener) {
        userRepository.getUserEquipment(userId, listener);
    }

    /**
     * Get specific equipment item from user's inventory
     * Business logic: Provides single equipment item for user
     */
    public void getUserEquipmentById(String userId, String equipmentId, OnCompleteListener<MyEquipment> listener) {
        userRepository.getUserEquipmentById(userId, equipmentId, listener);
    }

    /**
     * Get equipment count for user
     * Business logic: Counts total equipment in inventory
     */
    public void getUserEquipmentCount(String userId, OnCompleteListener<Integer> listener) {
        userRepository.getUserEquipment(userId, equipmentTask -> {
            if (equipmentTask.isSuccessful()) {
                List<MyEquipment> equipment = equipmentTask.getResult();
                int count = (equipment != null) ? equipment.size() : 0;
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(count));
            } else {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    equipmentTask.getException() != null ? equipmentTask.getException() : new Exception("Failed to fetch equipment")));
            }
        });
    }

    /**
     * Get activated equipment for user
     * Business logic: Provides only activated equipment items
     */
    public void getUserActivatedEquipment(String userId, OnCompleteListener<List<MyEquipment>> listener) {
        userRepository.getUserActivatedEquipment(userId, listener);
    }
    
    /**
     * Add equipment to user's inventory
     * Business logic: Adds new equipment to user's equipment list
     */
    public void addEquipmentToUser(String userId, Equipment equipment, OnCompleteListener<Void> listener) {
        getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    // Create MyEquipment from Equipment
                    MyEquipment myEquipment = new MyEquipment();
                    myEquipment.setId(equipment.getId());
                    myEquipment.setEquipmentId(equipment.getId());
                    myEquipment.setLeftAmount(equipment.getLasting() == 3 ? -1 : equipment.getLasting()); // -1 for permanent
                    myEquipment.setTimesUpgraded(0);
                    myEquipment.setActivated(false);
                    
                    // Add to user's equipment list
                    if (user.getEquipment() == null) {
                        user.setEquipment(new ArrayList<>());
                    }
                    user.getEquipment().add(myEquipment);
                    
                    // Update user in database
                    updateUser(user, listener);
                } else {
                    listener.onComplete(Tasks.forException(new Exception("User not found")));
                }
            } else {
                listener.onComplete(Tasks.forException(new Exception("Failed to get user")));
            }
        });
    }
    
    /**
     * Increment startedMissions count for a user
     */
    public void incrementStartedMissions(String userId, OnCompleteListener<Void> listener) {
        getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    int currentStartedMissions = user.getStartedMissions();
                    user.setStartedMissions(currentStartedMissions + 1);
                    updateUser(user, listener);
                    Log.d("UserService", "Incremented startedMissions for user " + userId + " to " + user.getStartedMissions());
                } else {
                    listener.onComplete(Tasks.forException(new Exception("User not found")));
                }
            } else {
                listener.onComplete(Tasks.forException(new Exception("Failed to get user")));
            }
        });
    }
    
    /**
     * Increment finishedMissions count for a user
     */
    public void incrementFinishedMissions(String userId, OnCompleteListener<Void> listener) {
        getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    int currentFinishedMissions = user.getFinishedMissions();
                    user.setFinishedMissions(currentFinishedMissions + 1);
                    updateUser(user, listener);
                    Log.d("UserService", "Incremented finishedMissions for user " + userId + " to " + user.getFinishedMissions());
                } else {
                    listener.onComplete(Tasks.forException(new Exception("User not found")));
                }
            } else {
                listener.onComplete(Tasks.forException(new Exception("Failed to get user")));
            }
        });
    }
    
    /**
     * Increment startedMissions for multiple users (alliance members)
     */
    public void incrementStartedMissionsForAll(List<String> userIds, OnCompleteListener<Void> listener) {
        if (userIds == null || userIds.isEmpty()) {
            listener.onComplete(Tasks.forResult(null));
            return;
        }
        
        Log.d("UserService", "Incrementing startedMissions for " + userIds.size() + " users");
        
        // Use AtomicInteger to track completed operations
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(userIds.size());
        boolean[] hasError = {false};
        
        for (String userId : userIds) {
            incrementStartedMissions(userId, task -> {
                if (!task.isSuccessful()) {
                    Log.e("UserService", "Failed to increment startedMissions for user " + userId, task.getException());
                    hasError[0] = true;
                }
                
                int completed = completedCount.incrementAndGet();
                if (completed == totalCount.get()) {
                    if (hasError[0]) {
                        listener.onComplete(Tasks.forException(new Exception("Some users failed to update")));
                    } else {
                        Log.d("UserService", "Successfully incremented startedMissions for all " + userIds.size() + " users");
                        listener.onComplete(Tasks.forResult(null));
                    }
                }
            });
        }
    }
    
    /**
     * Increment finishedMissions for multiple users (alliance members)
     */
    public void incrementFinishedMissionsForAll(List<String> userIds, OnCompleteListener<Void> listener) {
        if (userIds == null || userIds.isEmpty()) {
            listener.onComplete(Tasks.forResult(null));
            return;
        }
        
        Log.d("UserService", "Incrementing finishedMissions for " + userIds.size() + " users");
        
        // Use AtomicInteger to track completed operations
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(userIds.size());
        boolean[] hasError = {false};
        
        for (String userId : userIds) {
            incrementFinishedMissions(userId, task -> {
                if (!task.isSuccessful()) {
                    Log.e("UserService", "Failed to increment finishedMissions for user " + userId, task.getException());
                    hasError[0] = true;
                }
                
                int completed = completedCount.incrementAndGet();
                if (completed == totalCount.get()) {
                    if (hasError[0]) {
                        listener.onComplete(Tasks.forException(new Exception("Some users failed to update")));
                    } else {
                        Log.d("UserService", "Successfully incremented finishedMissions for all " + userIds.size() + " users");
                        listener.onComplete(Tasks.forResult(null));
                    }
                }
            });
        }
    }
}

