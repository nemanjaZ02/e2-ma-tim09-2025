package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.Collections;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossViewModel extends ViewModel {
    private final int BOSS_MAX_HEALTH = 100;
    private int attacksLeft = 5;
    private final MutableLiveData<Integer> currentHealth = new MutableLiveData<>();

    @Inject
    public BossViewModel() {
        currentHealth.setValue(BOSS_MAX_HEALTH);
    }

    public LiveData<Integer> getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return BOSS_MAX_HEALTH;
    }

    public void damage(int damageAmount) {
        if (damageAmount < 0) return;
        Integer current = currentHealth.getValue();
        if (current == null) return;
        int newHealth = Math.max(0, current - damageAmount);
        currentHealth.setValue(newHealth);
    }

    public int attackUsed() {
        attacksLeft = attacksLeft - 1;
        return attacksLeft;
    }

    public User getCurrentUser() {
        return new User(
                "11",                                 // id
                "TheDreadLord",                         // username
                "avatar_icon_05",                       // avatar
                55,                                     // level
                "The Unyielding",                       // title
                25,                                     // powerPoints
                350000,                                 // experiencePoints
                1500,                                   // coins
                Arrays.asList("Champion", "NoLifer"),   // badges
                Collections.emptyList(),                // equipment (npr. prazna lista)
                null,                                   // qrCode (ostavljamo null)
                System.currentTimeMillis() - 86400000L  // createdAt (npr. juče)
        );
    }
}