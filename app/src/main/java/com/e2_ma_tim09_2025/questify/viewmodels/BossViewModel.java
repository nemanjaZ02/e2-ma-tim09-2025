package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.TaskCategory;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.services.BossService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossViewModel extends ViewModel {
    private final UserService userService;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final BossService bossService;
    private final LiveData<Boss> boss;
    private final int BOSS_MAX_HEALTH = 100;
    private int attacksLeft = 5;
    private final MutableLiveData<Integer> currentHealth = new MutableLiveData<>();

    @Inject
    public BossViewModel(BossService bossService, UserService userService) {
        this.bossService = bossService;
        this.userService = userService;

        fetchCurrentUser();

        MediatorLiveData<Boss> bossMediator = new MediatorLiveData<>();
        this.boss = bossMediator;

        currentUser.observeForever(user -> {
            if (user != null) {
                LiveData<Boss> userBoss = bossService.getBoss(user.getId());
                bossMediator.addSource(userBoss, bossVal -> {
                    bossMediator.setValue(bossVal);
                });
            }
        });

        currentHealth.setValue(BOSS_MAX_HEALTH);
    }

    public void fetchCurrentUser() {
        String uid = userService.getCurrentUserId();
        if (uid != null) {
            userService.getUser(uid, task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    User user = task.getResult().toObject(User.class);
                    currentUser.postValue(user);
                } else {
                    currentUser.postValue(null);
                }
            });
        } else {
            currentUser.postValue(null);
        }
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
                "TheDreadLord",                       // username
                "avatar_icon_05",                     // avatar
                55,                                   // level
                "The Unyielding",                     // title
                25,                                   // powerPoints
                350000,                               // experiencePoints
                1500,                                 // coins
                Arrays.asList("Champion", "NoLifer"), // badges
                Collections.emptyList(),                     // equipment
                null,                                       // qrCode
                System.currentTimeMillis() - 86400000L,     // createdAt
                new ArrayList<>(Arrays.asList("22", "33")), // friends
                "alliance_007"                              // allianceId
        );
    }
}