package com.e2_ma_tim09_2025.questify.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
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
    private final MutableLiveData<Integer> coinsDrop = new MutableLiveData<>();
    private final MutableLiveData<Double> hitChance = new MutableLiveData<>();
    private int maxHealth = 0;
    private boolean staticDataLoaded = false;
    private boolean equipmentBonusesApplied = false;

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
                        || (!equipmentBonusesApplied && (coinsDrop.getValue() == null || !coinsDrop.getValue().equals(bossVal.getCoinsDrop())))
                        || (!equipmentBonusesApplied && (hitChance.getValue() == null || !hitChance.getValue().equals(bossVal.getHitChance())))) {
                    maxHealth = bossVal.getMaxHealth();
                    // Only set coinsDrop and hitChance if equipment bonuses haven't been applied yet
                    if (!equipmentBonusesApplied) {
                        coinsDrop.setValue(bossVal.getCoinsDrop());
                        hitChance.setValue(bossVal.getHitChance());
                    }
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

    /**
     * Recalculate user stats based on active equipment
     * This method should be called when equipment selection is closed
     */
    public void recalculateUserStatsWithActiveEquipment() {
        equipmentService.recalculateUserStatsWithActiveEquipment(currentUser.getValue().getId(), bonuses -> {
            User user = currentUser.getValue();
            if (user == null || bonuses == null || bonuses.isEmpty()) return;

            int pp = user.getPowerPoints();
            pp += pp * bonuses.get(0);

            // Create a *new* User instance (so LiveData detects change)
            User updatedUser = user; // assuming you have a copy constructor
            updatedUser.setPowerPoints(pp);

            Log.d("USER PP", String.valueOf(updatedUser.getPowerPoints()));

            // Trigger LiveData update
            currentUser.setValue(updatedUser);

            // Update other LiveData values
            Double currentHitChance = hitChance.getValue();
            System.out.println("DEBUG: Before hit chance update - Current: " + currentHitChance + ", Bonus: " + bonuses.get(1));
            if (currentHitChance != null) {
                Double newHitChance = currentHitChance + currentHitChance * bonuses.get(1);
                hitChance.setValue(newHitChance);
                equipmentBonusesApplied = true; // Mark that equipment bonuses have been applied
                System.out.println("DEBUG: After hit chance update - New: " + newHitChance);
            } else {
                System.out.println("DEBUG: Hit chance is null, cannot update");
            }
            Integer currentCoinsDrop = coinsDrop.getValue();
            System.out.println("DEBUG: Before coinsDrop update - Current: " + currentCoinsDrop + ", Bonus: " + bonuses.get(3));
            if (currentCoinsDrop != null) {
                Integer newCoinsDrop = (int) (currentCoinsDrop + currentCoinsDrop * bonuses.get(3));
                coinsDrop.setValue(newCoinsDrop);
                System.out.println("DEBUG: After coinsDrop update - New: " + newCoinsDrop);
            } else {
                System.out.println("DEBUG: CoinsDrop is null, cannot update");
            }
        });
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

    public LiveData<Integer> getCoinsDrop() {
        return coinsDrop;
    }

    public LiveData<BossStatus> getBossStatus() {
        return bossStatus;
    }

    public LiveData<Double> getHitChance() {
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
        
        Double currentHitChance = hitChance.getValue();
        System.out.println("DEBUG: Hit chance calculation - Roll: " + roll + ", HitChance: " + currentHitChance);

        // Normalize hit chance: treat hitChance as percentage (0-100)
        if (currentHitChance == null || roll > currentHitChance) {
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

        int reward = coinsDrop.getValue();
        Integer currentHealthValue = currentHealth.getValue();
        String userId = userService.getCurrentUserId();

        if (currentHealthValue != null && currentHealthValue <= 0) {
            // Defeated: full reward and respawn with difficulty scaling
            reward = coinsDrop.getValue();
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
            reward = coinsDrop.getValue() / 2;
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

        // Damage activated equipment directly before updating user
        System.out.println("DEBUG: Starting equipment damage in BossViewModel");
        List<MyEquipment> equipmentToRemove = new ArrayList<>();
        boolean equipmentUpdated = false;

        if (user.getEquipment() != null) {
            for (MyEquipment equipment : user.getEquipment()) {
                if (equipment.isActivated()) {
                    int leftAmount = equipment.getLeftAmount();
                    System.out.println("DEBUG: Processing active equipment " + equipment.getEquipmentId() + 
                                     " with leftAmount: " + leftAmount);

                    if (leftAmount == 1 || leftAmount == 2) {
                        // Decrement leftAmount
                        equipment.setLeftAmount(leftAmount - 1);
                        equipmentUpdated = true;
                        
                        System.out.println("DEBUG: Decremented " + equipment.getEquipmentId() + 
                                         " to leftAmount: " + equipment.getLeftAmount());

                        // If after decrement amount is 0, remove that equipment item from user's list
                        if (equipment.getLeftAmount() == 0) {
                            equipmentToRemove.add(equipment);
                            System.out.println("DEBUG: Equipment " + equipment.getEquipmentId() + " exhausted, marking for removal");
                        }
                    } else if (leftAmount == 3) {
                        // Don't decrement
                        System.out.println("DEBUG: Equipment " + equipment.getEquipmentId() + " has 3 uses, not decrementing");
                    }
                }
            }
        }

        // Remove exhausted equipment
        if (!equipmentToRemove.isEmpty()) {
            System.out.println("DEBUG: Removing " + equipmentToRemove.size() + " exhausted equipment items");
            user.getEquipment().removeAll(equipmentToRemove);
            equipmentUpdated = true;
        }

        if (equipmentUpdated) {
            System.out.println("DEBUG: Equipment damage completed, updating user");
        } else {
            System.out.println("DEBUG: No equipment damage needed");
        }

        // Update user with modified equipment
        userService.updateUser(user, task -> {
            if (task.isSuccessful()) {
                currentUser.postValue(user);
                System.out.println("DEBUG: User updated successfully with equipment changes");
            } else {
                System.out.println("DEBUG: Failed to update user: " + 
                                 (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
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
                            lastRewardedEquipment.postValue(equipment);
                            addToRewardedEquipment(equipment);
                        }
                    });
                }
            });
        } else {
            // No equipment reward
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