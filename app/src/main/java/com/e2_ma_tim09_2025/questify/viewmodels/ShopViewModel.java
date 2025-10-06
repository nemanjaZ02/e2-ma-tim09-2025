package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ShopViewModel extends ViewModel {

    private final EquipmentService equipmentService;
    
    // LiveData for equipment data
    private final MutableLiveData<List<Equipment>> equipmentList = new MutableLiveData<>();
    
    // LiveData for purchase operations
    private final MutableLiveData<Boolean> purchaseResult = new MutableLiveData<>();
    private final MutableLiveData<String> purchaseMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Integer> userCoins = new MutableLiveData<>();

    @Inject
    public ShopViewModel(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    public LiveData<List<Equipment>> getEquipmentList() {
        return equipmentList;
    }
    
    public LiveData<Boolean> getPurchaseResult() {
        return purchaseResult;
    }
    
    public LiveData<String> getPurchaseMessage() {
        return purchaseMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<Integer> getUserCoins() {
        return userCoins;
    }

    public void loadEquipment() {
        System.out.println("=== LOADING EQUIPMENT FROM DATABASE ===");
        
        equipmentService.getAllEquipment(task -> {
            if (task.isSuccessful()) {
                List<Equipment> allEquipment = task.getResult();
                equipmentList.setValue(allEquipment);
                System.out.println("Equipment loaded successfully: " + allEquipment.size() + " items");
                
                // Log all equipment
                for (Equipment equipment : allEquipment) {
                    System.out.println("- " + equipment.getName() + " (ID: " + equipment.getId() + ", Type: " + equipment.getType() + ")");
                }
            } else {
                System.out.println("Failed to load equipment: " + task.getException().getMessage());
                equipmentList.setValue(null);
            }
        });
    }
    
    /**
     * Buy equipment for the current user
     * @param userId Current user ID
     * @param equipmentId ID of equipment to purchase
     */
    public void buyEquipment(String userId, String equipmentId) {
        isLoading.setValue(true);
        purchaseMessage.setValue(null);
        
        equipmentService.buyEquipment(userId, equipmentId, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(Task<Boolean> task) {
                isLoading.setValue(false);
                
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult()) {
                        purchaseResult.setValue(true);
                        purchaseMessage.setValue("Equipment purchased successfully!");
                        // Reload equipment list and user coins to reflect any changes
                        loadEquipment();
                        loadUserCoins(userId);
                    } else {
                        purchaseResult.setValue(false);
                        purchaseMessage.setValue("Purchase failed. Please try again.");
                    }
                } else {
                    purchaseResult.setValue(false);
                    String errorMessage = "Purchase failed";
                    if (task.getException() != null) {
                        errorMessage = task.getException().getMessage();
                    }
                    purchaseMessage.setValue(errorMessage);
                }
            }
        });
    }
    
    /**
     * Get equipment price for a user
     * @param userId Current user ID
     * @param equipmentId ID of equipment
     * @param callback Callback to receive the price
     */
    public void getEquipmentPrice(String userId, String equipmentId, OnCompleteListener<Double> callback) {
        equipmentService.getEquipmentPrice(userId, equipmentId, callback);
    }
    
    /**
     * Load user's current coin balance
     * @param userId Current user ID
     */
    public void loadUserCoins(String userId) {
        equipmentService.getUserCoins(userId, task -> {
            if (task.isSuccessful()) {
                userCoins.setValue(task.getResult());
            } else {
                userCoins.setValue(0);
            }
        });
    }
}
