package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EquipmentSelectionViewModel extends ViewModel {

    private final EquipmentService equipmentService;
    private final UserService userService;
    
    // LiveData for equipment management
    private final MutableLiveData<List<MyEquipment>> activeEquipment = new MutableLiveData<>();
    private final MutableLiveData<List<MyEquipment>> availableEquipment = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> activationSuccess = new MutableLiveData<>();

    @Inject
    public EquipmentSelectionViewModel(EquipmentService equipmentService, UserService userService) {
        this.equipmentService = equipmentService;
        this.userService = userService;
    }

    /**
     * Load user's equipment for display
     */
    public void loadUserEquipment() {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            message.setValue("User not logged in");
            return;
        }

        isLoading.setValue(true);
        
        userService.getUser(userId, task -> {
            isLoading.setValue(false);
            
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                com.e2_ma_tim09_2025.questify.models.User user = task.getResult().toObject(com.e2_ma_tim09_2025.questify.models.User.class);
                if (user != null && user.getEquipment() != null) {
                    // Separate active equipment from available equipment
                    List<MyEquipment> active = new ArrayList<>();
                    List<MyEquipment> available = new ArrayList<>();
                    
                    for (MyEquipment equipment : user.getEquipment()) {
                        System.out.println("DEBUG: Equipment ID: " + equipment.getEquipmentId() + 
                                         ", Activated: " + equipment.isActivated() + 
                                         ", LeftAmount: " + equipment.getLeftAmount());
                        
                        if (equipment.isActivated()) {
                            active.add(equipment);
                            System.out.println("DEBUG: Added to ACTIVE: " + equipment.getEquipmentId());
                        } else if (equipment.getLeftAmount() > 0) {
                            available.add(equipment);
                            System.out.println("DEBUG: Added to AVAILABLE: " + equipment.getEquipmentId());
                        } else {
                            System.out.println("DEBUG: SKIPPED (no uses left): " + equipment.getEquipmentId());
                        }
                    }
                    
                    System.out.println("DEBUG: Total active equipment: " + active.size());
                    System.out.println("DEBUG: Total available equipment: " + available.size());
                    
                    activeEquipment.setValue(active);
                    availableEquipment.setValue(available);
                } else {
                    activeEquipment.setValue(new ArrayList<>());
                    availableEquipment.setValue(new ArrayList<>());
                }
            } else {
                message.setValue("Error loading equipment");
                activeEquipment.setValue(new ArrayList<>());
                availableEquipment.setValue(new ArrayList<>());
            }
        });
    }

    /**
     * Activate multiple equipment items
     */
    public void activateEquipment(List<MyEquipment> equipmentToActivate) {
        if (equipmentToActivate == null || equipmentToActivate.isEmpty()) {
            message.setValue("No equipment selected for activation");
            return;
        }

        String userId = userService.getCurrentUserId();
        if (userId == null) {
            message.setValue("User not logged in");
            return;
        }

        isLoading.setValue(true);
        
        // Activate equipment items one by one
        activateEquipmentSequentially(userId, equipmentToActivate, 0);
    }

    /**
     * Activate equipment items sequentially to avoid race conditions
     */
    private void activateEquipmentSequentially(String userId, List<MyEquipment> equipmentToActivate, int currentIndex) {
        if (currentIndex >= equipmentToActivate.size()) {
            // All equipment activated successfully
            isLoading.setValue(false);
            activationSuccess.setValue(true);
            message.setValue("Equipment activated successfully!");
            
            // Add delay before reloading equipment to ensure activation is saved to database
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                System.out.println("DEBUG: Reloading equipment after activation delay");
                loadUserEquipment();
            }, 1000); // 1 second delay
            return;
        }

        MyEquipment equipment = equipmentToActivate.get(currentIndex);
        
        // Activate current equipment item
        equipmentService.activateEquipment(userId, equipment.getEquipmentId(), task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult()) {
                // Successfully activated this item, continue with next
                activateEquipmentSequentially(userId, equipmentToActivate, currentIndex + 1);
            } else {
                // Failed to activate this item
                isLoading.setValue(false);
                activationSuccess.setValue(false);
                
                String errorMessage = "Failed to activate equipment: " + equipment.getEquipmentId();
                if (task.getException() != null) {
                    errorMessage += " - " + task.getException().getMessage();
                }
                message.setValue(errorMessage);
            }
        });
    }

    // Getters for LiveData
    public LiveData<List<MyEquipment>> getActiveEquipment() {
        return activeEquipment;
    }

    public LiveData<List<MyEquipment>> getAvailableEquipment() {
        return availableEquipment;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getActivationSuccess() {
        return activationSuccess;
    }
}