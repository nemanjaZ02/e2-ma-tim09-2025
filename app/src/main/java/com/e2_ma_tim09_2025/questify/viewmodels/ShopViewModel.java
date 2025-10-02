package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ShopViewModel extends ViewModel {

    private final EquipmentService equipmentService;
    
    // LiveData for equipment data
    private final MutableLiveData<List<Equipment>> equipmentList = new MutableLiveData<>();

    @Inject
    public ShopViewModel(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    public LiveData<List<Equipment>> getEquipmentList() {
        return equipmentList;
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
}
