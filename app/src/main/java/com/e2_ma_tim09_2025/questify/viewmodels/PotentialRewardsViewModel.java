package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.enums.EquipmentType;
import com.e2_ma_tim09_2025.questify.services.BossService;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PotentialRewardsViewModel extends ViewModel {

    private final BossService bossService;
    private final EquipmentService equipmentService;
    private final MutableLiveData<Integer> killBossReward = new MutableLiveData<>();
    private final MutableLiveData<Integer> weakenBossReward = new MutableLiveData<>();
    
    // Equipment lists
    private final MutableLiveData<List<Equipment>> killBossClothes = new MutableLiveData<>();
    private final MutableLiveData<List<Equipment>> killBossWeapons = new MutableLiveData<>();
    private final MutableLiveData<List<Equipment>> weakenBossClothes = new MutableLiveData<>();
    private final MutableLiveData<List<Equipment>> weakenBossWeapons = new MutableLiveData<>();

    @Inject
    public PotentialRewardsViewModel(BossService bossService, EquipmentService equipmentService) {
        this.bossService = bossService;
        this.equipmentService = equipmentService;
    }

    public void loadRewards() {
        // Get boss data to calculate rewards
        // For now, we'll use a default value since we need the boss data
        // In a real implementation, you would get the boss data and extract coinsDrop
        int defaultCoinsDrop = 100; // This should come from boss data
        
        // Calculate rewards based on coinsDrop
        killBossReward.setValue(defaultCoinsDrop);
        weakenBossReward.setValue(defaultCoinsDrop / 2);
        
        // Load equipment
        loadEquipment();
    }
    
    private void loadEquipment() {
        // Get all equipment from the service
        equipmentService.getAllEquipment(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Equipment> equipmentList = task.getResult();
                
                // Filter clothes and weapons
                List<Equipment> clothes = new ArrayList<>();
                List<Equipment> weapons = new ArrayList<>();
                
                for (Equipment equipment : equipmentList) {
                    if (equipment.getType() == EquipmentType.CLOTHES) {
                        clothes.add(equipment);
                    } else if (equipment.getType() == EquipmentType.WEAPON) {
                        weapons.add(equipment);
                    }
                }
                
                // Set the equipment lists (same for both kill and weaken scenarios)
                killBossClothes.setValue(clothes);
                killBossWeapons.setValue(weapons);
                weakenBossClothes.setValue(clothes);
                weakenBossWeapons.setValue(weapons);
            } else {
                // Set empty lists if equipment loading fails
                killBossClothes.setValue(new ArrayList<>());
                killBossWeapons.setValue(new ArrayList<>());
                weakenBossClothes.setValue(new ArrayList<>());
                weakenBossWeapons.setValue(new ArrayList<>());
            }
        });
    }

    public LiveData<Integer> getKillBossReward() {
        return killBossReward;
    }

    public LiveData<Integer> getWeakenBossReward() {
        return weakenBossReward;
    }
    
    public LiveData<List<Equipment>> getKillBossClothes() {
        return killBossClothes;
    }
    
    public LiveData<List<Equipment>> getKillBossWeapons() {
        return killBossWeapons;
    }
    
    public LiveData<List<Equipment>> getWeakenBossClothes() {
        return weakenBossClothes;
    }
    
    public LiveData<List<Equipment>> getWeakenBossWeapons() {
        return weakenBossWeapons;
    }
}
