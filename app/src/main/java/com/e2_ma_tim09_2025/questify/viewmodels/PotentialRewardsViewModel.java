package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.services.BossService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PotentialRewardsViewModel extends ViewModel {

    private final BossService bossService;
    private final MutableLiveData<Integer> killBossReward = new MutableLiveData<>();
    private final MutableLiveData<Integer> weakenBossReward = new MutableLiveData<>();

    @Inject
    public PotentialRewardsViewModel(BossService bossService) {
        this.bossService = bossService;
    }

    public void loadRewards() {
        // Get boss data to calculate rewards
        // For now, we'll use a default value since we need the boss data
        // In a real implementation, you would get the boss data and extract coinsDrop
        int defaultCoinsDrop = 100; // This should come from boss data
        
        // Calculate rewards based on coinsDrop
        killBossReward.setValue(defaultCoinsDrop);
        weakenBossReward.setValue(defaultCoinsDrop / 2);
    }

    public LiveData<Integer> getKillBossReward() {
        return killBossReward;
    }

    public LiveData<Integer> getWeakenBossReward() {
        return weakenBossReward;
    }
}
