package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.models.enums.EquipmentType;
import com.e2_ma_tim09_2025.questify.services.BossService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BossViewModel extends ViewModel {
    private final UserService userService;
    private final BossService bossService;
    private final EquipmentService equipmentService;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MediatorLiveData<Boss> boss = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> currentHealth = new MediatorLiveData<>();
    private final MediatorLiveData<Integer> attacksLeft = new MediatorLiveData<>();
    private final MediatorLiveData<BossStatus> bossStatus = new MediatorLiveData<>();
    private final MutableLiveData<String> rewardMessage = new MutableLiveData<>();
    private final MutableLiveData<Equipment> lastRewardedEquipment = new MutableLiveData<>();
    private final MutableLiveData<List<Equipment>> rewardedEquipment = new MutableLiveData<>();
    private int maxHealth = 0;
    private int coinsDrop = 0;
    private double hitChance = 0.0;
    private boolean staticDataLoaded = false;

    @Inject
    public BossViewModel(BossService bossService, UserService userService, EquipmentService equipmentService) {
        this.bossService = bossService;
        this.userService = userService;
        this.equipmentService = equipmentService;

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
                if (!staticDataLoaded
                        || maxHealth != bossVal.getMaxHealth()
                        || coinsDrop != bossVal.getCoinsDrop()
                        || hitChance != bossVal.getHitChance()) {
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

        Random random = new Random();
        int roll = random.nextInt(100) + 1;

        // Normalize hit chance: treat hitChance as percentage (0-100)
        if (roll > hitChance) {
            // Miss: decrement attacks on server side to avoid local desync
            bossService.damageBoss(user.getId(), 0, task -> {
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
        String userId = userService.getCurrentUserId();

        if (currentHealthValue != null && currentHealthValue <= 0) {
            // Defeated: full reward and respawn with difficulty scaling
            reward = coinsDrop;
            giveEquipmentReward(userId, 95.0, 5.0); // 95% clothes, 5% weapons
            
            Boss currentBoss = boss.getValue();
            if (currentBoss != null) {
                Boss newBoss = bossService.setNewBoss(currentBoss, true);
                bossService.updateBoss(newBoss, task -> {
                    if (task.isSuccessful()) {
                        boss.postValue(newBoss);
                    }
                });
            }
        } else if (currentHealthValue != null && currentHealthValue <= (maxHealth / 2) && currentHealthValue > 0) {
            // Weakened: half reward and equipment chance
            reward = coinsDrop / 2;
            giveEquipmentReward(userId, 47.5, 2.5); // 47.5% clothes, 2.5% weapons, 50% nothing

            Boss currentBoss = boss.getValue();
            if (currentBoss != null) {
                Boss newBoss = bossService.setNewBoss(currentBoss, false);
                bossService.updateBoss(newBoss, task -> {
                    if (task.isSuccessful()) {
                        boss.postValue(newBoss);
                    }
                });
            }
        } else if(currentHealthValue != null && currentHealthValue > (maxHealth / 2) && currentHealthValue <= maxHealth) {
            // Not weakened enough: no reward, no equipment
            reward = 0;
            rewardMessage.postValue("Boss not weakened enough - no equipment reward");
            lastRewardedEquipment.postValue(null);

            Boss currentBoss = boss.getValue();
            if (currentBoss != null) {
                Boss newBoss = bossService.setNewBoss(currentBoss, false);
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

    private void giveEquipmentReward(String userId, double clothesChance, double weaponsChance) {
        double random = Math.random() * 100;
        
        if (random <= clothesChance) {
            // Give clothes
            equipmentService.getRandomEquipmentByType(EquipmentType.CLOTHES, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Equipment equipment = task.getResult();
                    userService.addEquipmentToUser(userId, equipment, addTask -> {
                        if (addTask.isSuccessful()) {
                            rewardMessage.postValue("You received: " + equipment.getName() + " (Clothes)");
                            lastRewardedEquipment.postValue(equipment);
                            addToRewardedEquipment(equipment);
                        }
                    });
                }
            });
        } else if (random <= clothesChance + weaponsChance) {
            // Give weapon
            equipmentService.getRandomEquipmentByType(EquipmentType.WEAPON, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Equipment equipment = task.getResult();
                    userService.addEquipmentToUser(userId, equipment, addTask -> {
                        if (addTask.isSuccessful()) {
                            rewardMessage.postValue("You received: " + equipment.getName() + " (Weapon)");
                            lastRewardedEquipment.postValue(equipment);
                            addToRewardedEquipment(equipment);
                        }
                    });
                }
            });
        } else {
            // No equipment reward
            rewardMessage.postValue("No equipment reward this time");
            lastRewardedEquipment.postValue(null);
        }
    }

    private void addToRewardedEquipment(Equipment equipment) {
        List<Equipment> currentList = rewardedEquipment.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(equipment);
        rewardedEquipment.postValue(currentList);
    }
    
    public void clearRewardedEquipment() {
        rewardedEquipment.postValue(new ArrayList<>());
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

    public LiveData<String> getRewardMessage() {
        return rewardMessage;
    }
    
    public LiveData<Equipment> getLastRewardedEquipment() {
        return lastRewardedEquipment;
    }
    
    public LiveData<List<Equipment>> getRewardedEquipment() {
        return rewardedEquipment;
    }

    public interface OnDamageCompleteListener {
        void onSuccess();
        void onMiss();
        void onFailure(String error);
    }
}