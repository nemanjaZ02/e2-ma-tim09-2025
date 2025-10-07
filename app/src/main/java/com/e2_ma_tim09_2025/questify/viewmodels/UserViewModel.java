package com.e2_ma_tim09_2025.questify.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserViewModel extends ViewModel {

    private final UserService userService;
    private final EquipmentService equipmentService;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationStatus = new MutableLiveData<>();
    private final MutableLiveData<String> registrationError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginStatus = new MutableLiveData<>();
    private final MutableLiveData<String> changePasswordResult = new MutableLiveData<>();
    private final MutableLiveData<List<Equipment>> userEquipmentDetails = new MutableLiveData<>();
    private final MutableLiveData<List<EquipmentWithQuantity>> userEquipmentWithQuantities = new MutableLiveData<>();
    private final MutableLiveData<Integer> equipmentCount = new MutableLiveData<>();


    @Inject
    public UserViewModel(UserService userService, EquipmentService equipmentService) {
        this.userService = userService;
        this.equipmentService = equipmentService;
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
    
    public LiveData<List<Equipment>> getUserEquipmentDetails() {
        return userEquipmentDetails;
    }
    
    public LiveData<List<EquipmentWithQuantity>> getUserEquipmentWithQuantities() {
        return userEquipmentWithQuantities;
    }
    
    public LiveData<Integer> getEquipmentCount() {
        return equipmentCount;
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
    
    /**
     * Refresh current user data
     */
    public void refreshCurrentUser() {
        String currentUserId = userService.getCurrentUserId();
        if (currentUserId != null) {
            fetchUser(currentUserId);
        }
    }
    
    /**
     * Load user equipment details - shows ALL individual equipment items
     */
    public void loadUserEquipmentDetails(List<MyEquipment> userEquipment) {
        if (userEquipment == null || userEquipment.isEmpty()) {
            userEquipmentDetails.setValue(new ArrayList<>());
            equipmentCount.setValue(0);
            return;
        }
        
        // Group equipment by ID and count quantities
        Map<String, Integer> equipmentQuantities = new HashMap<>();
        for (MyEquipment myEquipment : userEquipment) {
            String equipmentId = myEquipment.getEquipmentId();
            equipmentQuantities.put(equipmentId, equipmentQuantities.getOrDefault(equipmentId, 0) + 1);
        }
        
        // Update equipment count to show total items
        int totalItems = userEquipment.size();
        equipmentCount.setValue(totalItems);
        
        // Load equipment details for unique equipment items
        List<EquipmentWithQuantity> equipmentDetails = new ArrayList<>();
        AtomicInteger loadedCount = new AtomicInteger(0);
        int totalUniqueItems = equipmentQuantities.size();
        
        for (Map.Entry<String, Integer> entry : equipmentQuantities.entrySet()) {
            String equipmentId = entry.getKey();
            int quantity = entry.getValue();
            
            equipmentService.getEquipment(equipmentId, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    equipmentDetails.add(new EquipmentWithQuantity(task.getResult(), quantity));
                }
                
                // Check if all equipment details are loaded
                if (loadedCount.incrementAndGet() == totalUniqueItems) {
                    // Set the equipment with quantities
                    userEquipmentWithQuantities.setValue(equipmentDetails);
                    
                    // Convert to regular Equipment list for compatibility
                    List<Equipment> equipmentList = new ArrayList<>();
                    for (EquipmentWithQuantity eq : equipmentDetails) {
                        equipmentList.add(eq.equipment);
                    }
                    userEquipmentDetails.setValue(equipmentList);
                }
            });
        }
    }
    
    // Helper class to hold equipment with quantity
    public static class EquipmentWithQuantity {
        public Equipment equipment;
        public int quantity;
        
        public EquipmentWithQuantity(Equipment equipment, int quantity) {
            this.equipment = equipment;
            this.quantity = quantity;
        }
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
