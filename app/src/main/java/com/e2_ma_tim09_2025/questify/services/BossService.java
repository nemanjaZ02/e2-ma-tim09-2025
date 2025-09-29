package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.repositories.BossRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;

import javax.inject.Inject;

public class BossService {

    private final BossRepository bossRepository;

    @Inject
    public BossService(BossRepository bossRepository) {
        this.bossRepository = bossRepository;
    }

    public LiveData<Boss> getBoss(String userId) {
        MutableLiveData<Boss> liveData = new MutableLiveData<>();

        bossRepository.getBossByUserId(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Boss boss = task.getResult().toObject(Boss.class);
                liveData.postValue(boss);
            } else {
                liveData.postValue(null);
            }
        });

        return liveData;
    }

    public void updateBoss(Boss boss, OnCompleteListener<Void> listener) {
        bossRepository.updateBoss(boss, listener);
    }

    public void damageBoss(String userId, int damage, OnCompleteListener<Void> listener) {
        bossRepository.getBossByUserId(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Boss boss = task.getResult().toObject(Boss.class);
                if (boss != null) {
                    int newHealth = boss.getCurrentHealth() - damage;
                    boss.setCurrentHealth(Math.max(newHealth, 0));

                    if (boss.getCurrentHealth() <= 0) {
                        int newMaxHealth = boss.getMaxHealth() * 2 + boss.getMaxHealth() / 2;
                        int newCoinsDrop = (int) Math.round(boss.getCoinsDrop() * 1.2);

                        boss.setStatus(BossStatus.INACTIVE);
                        boss.setMaxHealth(newMaxHealth);
                        boss.setCurrentHealth(newMaxHealth);
                        boss.setCoinsDrop(newCoinsDrop);
                        boss.setAttacksLeft(5);
                    }

                    bossRepository.updateBoss(boss, listener);
                } else {
                    Log.e("BossService", "Boss is null for user: " + userId);
                }
            } else {
                Log.e("BossService", "Failed getting boss for: " + userId);
            }
        });
    }

    public void deleteBoss(String userId, OnCompleteListener<Void> listener) {
        bossRepository.deleteBossByUserId(userId, listener);
    }
}
