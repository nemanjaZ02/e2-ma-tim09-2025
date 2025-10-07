package com.e2_ma_tim09_2025.questify.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;
import com.e2_ma_tim09_2025.questify.repositories.BossRepository;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;

public class BossService {

    private final BossRepository bossRepository;
    private final TaskRepository taskRepository;
    private final SpecialTaskService specialTaskService;
    private final AllianceService allianceService;
    private final Executor backgroundExecutor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public BossService(BossRepository bossRepository, TaskRepository taskRepository, SpecialTaskService specialTaskService, AllianceService allianceService) {
        this.bossRepository = bossRepository;
        this.taskRepository = taskRepository;
        this.specialTaskService = specialTaskService;
        this.allianceService = allianceService;
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    public LiveData<Boss> getBoss(String userId) {
        MutableLiveData<Boss> liveData = new MutableLiveData<>();

        bossRepository.listenBossByUserId(userId, (snapshot, e) -> {
            if (e != null) {
                liveData.postValue(null);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Boss boss = snapshot.toObject(Boss.class);
                liveData.postValue(boss);
            } else {
                liveData.postValue(null);
            }
        });

        return liveData;
    }

    public void calculateHitChanceAsync(String userId, int originalLevel, Consumer<Double> callback) {
        backgroundExecutor.execute(() -> {
            try {
                int completedInThisLevel = taskRepository.countCompletedTasksByLevel(userId, originalLevel);
                int createdInThisLevel = taskRepository.countCreatedTasksInLevel(userId, originalLevel);
                int completedInThisLevelCreatedBefore = taskRepository.countCompletedTasksCreatedBeforeLevel(userId, originalLevel);

                int denominator = createdInThisLevel + completedInThisLevelCreatedBefore;
                if (denominator == 0) {
                    denominator = 1;
                }

                double hitChance = (double) completedInThisLevel / denominator;

                if (hitChance == 0) {
                    hitChance = 52.0;
                } else {
                    hitChance = Math.round(hitChance * 10000.0) / 100.0;
                }

                final double finalHitChance = hitChance;

                mainHandler.post(() -> {
                    callback.accept(finalHitChance);
                });

            } catch (Exception e) {
                Log.e("BossService", "Error calculating hit chance asynchronously", e);
                mainHandler.post(() -> callback.accept(0.0));
            }
        });
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
                    boss.setAttacksLeft(boss.getAttacksLeft() - 1);

                    if (boss.getCurrentHealth() <= 0) {
                        boss.setStatus(BossStatus.DEFEATED);
                    }

                    bossRepository.updateBoss(boss, updateTask -> {
                        if (updateTask.isSuccessful()) {
                            // Pošto je damage > 0, znači da je napad uspešan - reši special task
                            if (damage > 0) {
                                Log.d("BossService", "=== SUCCESSFUL BOSS ATTACK! ===");
                                Log.d("BossService", "Damage: " + damage + ", User ID: " + userId);
                                Log.d("BossService", "Completing special task...");
                                
                                // Dobij allianceId za korisnika
                                allianceService.getUserAlliance(userId, new OnCompleteListener<Alliance>() {
                                    @Override
                                    public void onComplete(com.google.android.gms.tasks.Task<Alliance> allianceTask) {
                                        if (allianceTask.isSuccessful() && allianceTask.getResult() != null) {
                                            String allianceId = allianceTask.getResult().getId();
                                            Log.d("BossService", "User alliance found: " + allianceId);
                                            specialTaskService.completeSpecialTaskForAllAlliances(userId, SpecialTaskType.BOSS_ATTACK, new OnCompleteListener<Boolean>() {
                                                @Override
                                                public void onComplete(com.google.android.gms.tasks.Task<Boolean> specialTaskResult) {
                                                    if (specialTaskResult.isSuccessful()) {
                                                        Log.d("BossService", "✅ Special task completed successfully");
                                                    } else {
                                                        Log.e("BossService", "❌ Failed to complete special task", specialTaskResult.getException());
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.d("BossService", "User is not in any alliance, skipping special task");
                                        }
                                    }
                                });
                            }
                        }
                        // Pozovi originalni listener
                        if (listener != null) {
                            listener.onComplete(updateTask);
                        }
                    });
                } else {
                    Log.e("BossService", "Boss is null for user: " + userId);
                    if (listener != null) {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(new Exception("Boss is null")));
                    }
                }
            } else {
                Log.e("BossService", "Failed getting boss for: " + userId);
                if (listener != null) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(new Exception("Failed to get boss")));
                }
            }
        });
    }

    public Boss setNewBoss(Boss boss, boolean isDefeated) {
        if(isDefeated) {
            int newMaxHealth = boss.getMaxHealth() * 2 + boss.getMaxHealth() / 2;
            int newCoinsDrop = (int) Math.round(boss.getCoinsDrop() * 1.2);
            boss.setStatus(BossStatus.DEFEATED);
            boss.setMaxHealth(newMaxHealth);
            boss.setCurrentHealth(newMaxHealth);
            boss.setCoinsDrop(newCoinsDrop);
            boss.setAttacksLeft(5);
        }
        else {
            boss.setCurrentHealth(boss.getMaxHealth());
            boss.setAttacksLeft(5);
            boss.setStatus(BossStatus.INACTIVE);
        }

        return boss;
    }

    public void deleteBoss(String userId, OnCompleteListener<Void> listener) {
        bossRepository.deleteBossByUserId(userId, listener);
    }
    public void getBossByUser(String userId, OnCompleteListener<Boss> listener) {
        bossRepository.getBossByUserId(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Boss boss = task.getResult().toObject(Boss.class);
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(boss));
            } else {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
            }
        });
    }

    public Boss getBossForUser(String userId) {
        // This is synchronous - you'll need to handle the async nature differently
        final Boss[] result = {null};
        final CountDownLatch latch = new CountDownLatch(1);

        bossRepository.getBossByUserId(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                result[0] = task.getResult().toObject(Boss.class);
            } else {
                result[0] = null;
            }
            latch.countDown();
        });

        try {
            latch.await(); // Wait for the async operation to complete
            return result[0];
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}