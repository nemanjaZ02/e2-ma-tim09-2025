package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.services.BossService;
import com.e2_ma_tim09_2025.questify.services.UserService;

import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossViewModel extends ViewModel {
    private final UserService userService;
    private final BossService bossService;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MediatorLiveData<Boss> boss = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> currentHealth = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> attacksLeft = new MediatorLiveData<>();
    private final MediatorLiveData<BossStatus> bossStatus = new MediatorLiveData<>();
    private int maxHealth = 0;
    private int coinsDrop = 0;
    private double hitChance = 0.0;
    private boolean staticDataLoaded = false;

    @Inject
    public BossViewModel(BossService bossService, UserService userService) {
        this.bossService = bossService;
        this.userService = userService;

        fetchCurrentUser();

        currentUser.observeForever(user -> {
            if (user != null) {
                LiveData<Boss> userBoss = bossService.getBoss(user.getId());
                boss.addSource(userBoss, bossVal -> {
                    boss.setValue(bossVal);
                });
            }
        });

        currentHealth.addSource(boss, bossVal -> {
            if (bossVal != null) {
                currentHealth.setValue(bossVal.getCurrentHealth());
                if (!staticDataLoaded) {
                    maxHealth = bossVal.getMaxHealth();
                    coinsDrop = bossVal.getCoinsDrop();
                    hitChance = bossVal.getHitChance();
                    staticDataLoaded = true;
                }
            }
        });

        attacksLeft.addSource(boss, bossVal -> {
            if (bossVal != null) {
                attacksLeft.setValue(bossVal.getAttacksLeft());
            }
        });

        bossStatus.addSource(boss, bossVal -> {
            if (bossVal != null) {
                bossStatus.setValue(bossVal.getStatus());
            }
        });
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

    public LiveData<Integer> getAttacksLeft() {
        return attacksLeft;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCoinsDrop() {
        return coinsDrop;
    }

    public LiveData<BossStatus> getBossStatus() {
        return bossStatus;
    }

    public double getHitChance() {
        return hitChance;
    }

    public void damage(int damageAmount, OnDamageCompleteListener listener) {
        if (damageAmount < 0) {
            if (listener != null) listener.onFailure("Damage cannot be lower than 0");
            return;
        }

        User user = currentUser.getValue();
        if (user == null) {
            if (listener != null) listener.onFailure("User not loaded.");
            return;
        }

        Boss currentBoss = boss.getValue();
        if (currentBoss == null) {
            if (listener != null) listener.onFailure("Boss not loaded.");
            return;
        }

        Integer attacks = attacksLeft.getValue();
        if (attacks == null || attacks <= 0) {
            if (listener != null) listener.onFailure("No attacks left.");
            return;
        }

        Integer health = currentHealth.getValue();
        if (health == null || health <= 0) {
            if (listener != null) listener.onFailure("Boss is already dead.");
            return;
        }

        currentBoss.setAttacksLeft(currentBoss.getAttacksLeft() - 1);

        Random random = new Random();
        int roll = random.nextInt(100) + 1;

        if (roll > hitChance) {
            bossService.updateBoss(currentBoss, task -> {
                if (task.isSuccessful()) {
                    refreshBoss();
                    if (listener != null) listener.onMiss();
                } else {
                    if (listener != null) listener.onFailure("Failed updating boss.");
                }
            });
            return;
        }

        bossService.damageBoss(user.getId(), damageAmount, task -> {
            if (task.isSuccessful()) {
                refreshBoss();
                if (listener != null) listener.onSuccess();
            } else {
                if (listener != null) listener.onFailure("Failed updating boss.");
            }
        });
    }

    public void rewardUser() {
        User user = currentUser.getValue();
        if (user == null) return;

        int reward = coinsDrop;

        Integer currentHealthValue = currentHealth.getValue();
        if (currentHealthValue != null && currentHealthValue <= (maxHealth / 2)) {
            reward = coinsDrop / 2;

            // Ovo ako nije pobedio bossa da mu da pola nagrada a iskoristio je sve napade
            Boss currentBoss = boss.getValue();
            if (currentBoss != null) {
                Boss newBoss = bossService.setNewBoss(currentBoss);
                bossService.updateBoss(newBoss, task -> {
                    if (task.isSuccessful()) {
                        boss.postValue(newBoss);
                    }
                });
            }
            // Ovo znaci da niti je pobedio bossa niti mu je spustio health ispod pola (currentHealthValue < maxHealth gledam
            // jer nakon pobede se currentHealth odma azurira na maxHealth koji je veci od sadasnjeg pa ako je manje od toga
            // znaci da se nije azuriralo tj nije ga pobedio pa nema nagrade
        } else if(currentHealthValue != null && currentHealthValue > (maxHealth / 2) && currentHealthValue < maxHealth) {
            reward = 0;

            Boss currentBoss = boss.getValue();
            if (currentBoss != null) {
                Boss newBoss = bossService.setNewBoss(currentBoss);
                bossService.updateBoss(newBoss, task -> {
                    if (task.isSuccessful()) {
                        boss.postValue(newBoss);
                    }
                });
            }
        }

        user.setCoins(user.getCoins() + reward);

        userService.updateUser(user, task -> {
            if (task.isSuccessful()) {
                currentUser.postValue(user);
            }
        });
    }

    public void refreshBoss() {
        User user = currentUser.getValue();
        if (user != null) {
            LiveData<Boss> freshBoss = bossService.getBoss(user.getId());
            boss.addSource(freshBoss, bossVal -> {
                boss.setValue(bossVal);
                boss.removeSource(freshBoss);
            });
        }
    }

    public LiveData<Boss> getBoss() {
        return boss;
    }

    public LiveData<User> getCurrentUserLiveData() {
        return currentUser;
    }

    public interface OnDamageCompleteListener {
        void onSuccess();
        void onMiss();
        void onFailure(String error);
    }
}