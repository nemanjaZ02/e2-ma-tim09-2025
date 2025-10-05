package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import com.e2_ma_tim09_2025.questify.models.SpecialBoss;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.SpecialTask;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;
import com.e2_ma_tim09_2025.questify.repositories.SpecialMissionRepository;
import com.e2_ma_tim09_2025.questify.repositories.SpecialTaskRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecialTaskService {
    private final SpecialTaskRepository specialTaskRepository;
    private final SpecialMissionRepository specialMissionRepository;

    @Inject
    public SpecialTaskService(
            SpecialTaskRepository specialTaskRepository,
            SpecialMissionRepository specialMissionRepository) {
        this.specialTaskRepository = specialTaskRepository;
        this.specialMissionRepository = specialMissionRepository;
    }

    public void completeSpecialTask(String userId, SpecialTaskType taskType, String allianceId, OnCompleteListener<Boolean> listener) {
        completeSpecialTaskMultiple(userId, taskType, allianceId, 1, listener);
    }
    
    public void completeSpecialTaskMultiple(String userId, SpecialTaskType taskType, String allianceId, int times, OnCompleteListener<Boolean> listener) {
        Log.d("SpecialTaskService", "=== IZVRŠAVANJE SPECIAL TASK " + times + " PUTA ===");
        Log.d("SpecialTaskService", "User ID: " + userId);
        Log.d("SpecialTaskService", "Task Type: " + taskType);
        Log.d("SpecialTaskService", "Alliance ID: " + allianceId);
        Log.d("SpecialTaskService", "Times: " + times);

        // 1. Pronađi task za korisnika i tip
        specialTaskRepository.getSpecialTasksByAllianceAndUser(allianceId, userId, task -> {
            if (!task.isSuccessful()) {
                Log.e("SpecialTaskService", "Greška pri dohvatanju task-ova");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            SpecialTask specialTask = null;
            for (DocumentSnapshot doc : task.getResult()) {
                SpecialTask t = doc.toObject(SpecialTask.class);
                if (t != null && t.getTaskType() == taskType) {
                    specialTask = t;
                    break;
                }
            }

            if (specialTask == null) {
                Log.e("SpecialTaskService", "Special task nije pronađen");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            if (!specialTask.canComplete()) {
                Log.e("SpecialTaskService", "Task ne može biti izvršen (već završen ili neaktivan)");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            // 2. Izvrši task više puta
            int totalDamage = 0;
            for (int i = 0; i < times; i++) {
                if (specialTask.canComplete()) {
                    specialTask.complete();
                    totalDamage += specialTask.getDamagePerCompletion();
                    Log.d("SpecialTaskService", "Task izvršen " + (i + 1) + "/" + times + ". Trenutni broj: " + specialTask.getCurrentCount() + "/" + specialTask.getMaxCount());
                } else {
                    Log.d("SpecialTaskService", "Task ne može biti izvršen više puta (dostigao max)");
                    break;
                }
            }

            // Sačuvaj totalDamage u final varijablu za lambda
            final int finalTotalDamage = totalDamage;

            // 3. Ažuriraj task u bazi
            specialTaskRepository.updateSpecialTask(specialTask, task1 -> {
                if (!task1.isSuccessful()) {
                    Log.e("SpecialTaskService", "Greška pri ažuriranju task-a");
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }

                // 4. Nanesei ukupnu štetu boss-u
                dealDamageToBoss(allianceId, finalTotalDamage, listener);
            });
        });
    }

    private void dealDamageToBoss(String allianceId, int damage, OnCompleteListener<Boolean> listener) {
        specialMissionRepository.getSpecialMissionByAllianceId(allianceId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                Log.e("SpecialTaskService", "Greška pri dohvatanju misije");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            SpecialMission mission = task.getResult().toObject(SpecialMission.class);
            if (mission == null || mission.getBoss() == null) {
                Log.e("SpecialTaskService", "Misija ili boss je null");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            // Nanesei štetu koristeći convenience metodu
            mission.dealDamageToBoss(damage);
            Log.d("SpecialTaskService", "Naneta šteta: " + damage + " HP. Boss HP: " + mission.getBossCurrentHealth() + "/" + mission.getBossMaxHealth());

            // Ažuriraj misiju (sa boss-om unutar nje)
            specialMissionRepository.updateSpecialMission(mission, task1 -> {
                if (!task1.isSuccessful()) {
                    Log.e("SpecialTaskService", "Greška pri ažuriranju misije");
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }

                // Ažuriraj ukupnu štetu u misiji
                updateMissionDamage(allianceId, damage, listener);
            });
        });
    }

    private void updateMissionDamage(String allianceId, int damage, OnCompleteListener<Boolean> listener) {
        specialMissionRepository.getSpecialMissionByAllianceId(allianceId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                Log.e("SpecialTaskService", "Greška pri dohvatanju misije");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            SpecialMission mission = task.getResult().toObject(SpecialMission.class);
            if (mission == null) {
                Log.e("SpecialTaskService", "Misija je null");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            mission.addDamage(damage);
            specialMissionRepository.updateSpecialMission(mission, task1 -> {
                if (!task1.isSuccessful()) {
                    Log.e("SpecialTaskService", "Greška pri ažuriranju misije");
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }

                Log.d("SpecialTaskService", "✅ Special task uspešno izvršen!");
                listener.onComplete(Tasks.forResult(true));
            });
        });
    }

    public void getTaskCompletionCount(String userId, SpecialTaskType taskType, String allianceId, OnCompleteListener<Integer> listener) {
        specialTaskRepository.getSpecialTasksByAllianceAndUser(allianceId, userId, task -> {
            if (!task.isSuccessful()) {
                listener.onComplete(Tasks.forResult(0));
                return;
            }

            for (DocumentSnapshot doc : task.getResult()) {
                SpecialTask specialTask = doc.toObject(SpecialTask.class);
                if (specialTask != null && specialTask.getTaskType() == taskType) {
                    listener.onComplete(Tasks.forResult(specialTask.getCurrentCount()));
                    return;
                }
            }

            listener.onComplete(Tasks.forResult(0));
        });
    }

    public void canCompleteTask(String userId, SpecialTaskType taskType, String allianceId, OnCompleteListener<Boolean> listener) {
        specialTaskRepository.getSpecialTasksByAllianceAndUser(allianceId, userId, task -> {
            if (!task.isSuccessful()) {
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            for (DocumentSnapshot doc : task.getResult()) {
                SpecialTask specialTask = doc.toObject(SpecialTask.class);
                if (specialTask != null && specialTask.getTaskType() == taskType) {
                    listener.onComplete(Tasks.forResult(specialTask.canComplete()));
                    return;
                }
            }

            listener.onComplete(Tasks.forResult(false));
        });
    }

    public void getUserSpecialTasks(String userId, String allianceId, OnCompleteListener<List<SpecialTask>> listener) {
        System.out.println("DEBUG SpecialTaskService: Getting special tasks for userId=" + userId + ", allianceId=" + allianceId);
        specialTaskRepository.getSpecialTasksByAllianceAndUser(allianceId, userId, task -> {
            if (task.isSuccessful()) {
                List<SpecialTask> tasks = new ArrayList<>();
                System.out.println("DEBUG SpecialTaskService: Query successful, found " + task.getResult().size() + " documents");
                for (DocumentSnapshot doc : task.getResult()) {
                    SpecialTask specialTask = doc.toObject(SpecialTask.class);
                    if (specialTask != null) {
                        tasks.add(specialTask);
                        System.out.println("DEBUG SpecialTaskService: Added task type=" + specialTask.getTaskType() + ", status=" + specialTask.getStatus());
                    }
                }
                System.out.println("DEBUG SpecialTaskService: Returning " + tasks.size() + " tasks");
                listener.onComplete(Tasks.forResult(tasks));
            } else {
                System.out.println("DEBUG SpecialTaskService: Query failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                listener.onComplete(Tasks.forResult(new ArrayList<>()));
            }
        });
    }

    public void getAllianceSpecialTasks(String allianceId, OnCompleteListener<List<SpecialTask>> listener) {
        specialTaskRepository.getSpecialTasksByAllianceId(allianceId, task -> {
            if (task.isSuccessful()) {
                List<SpecialTask> tasks = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    SpecialTask specialTask = doc.toObject(SpecialTask.class);
                    if (specialTask != null) {
                        tasks.add(specialTask);
                    }
                }
                listener.onComplete(Tasks.forResult(tasks));
            } else {
                listener.onComplete(Tasks.forResult(new ArrayList<>()));
            }
        });
    }

    public void listenToUserSpecialTasks(String userId, String allianceId, OnCompleteListener<List<SpecialTask>> listener) {
        specialTaskRepository.listenToSpecialTasksByUserId(userId, allianceId, task -> {
            if (task.isSuccessful()) {
                List<SpecialTask> tasks = task.getResult();
                listener.onComplete(Tasks.forResult(tasks));
            } else {
                listener.onComplete(Tasks.forResult(new ArrayList<>()));
            }
        });
    }

    public void getSpecialTaskByType(String userId, SpecialTaskType taskType, String allianceId, OnCompleteListener<SpecialTask> listener) {
        specialTaskRepository.getSpecialTasksByAllianceAndUser(allianceId, userId, task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot doc : task.getResult()) {
                    SpecialTask specialTask = doc.toObject(SpecialTask.class);
                    if (specialTask != null && specialTask.getTaskType() == taskType) {
                        listener.onComplete(Tasks.forResult(specialTask));
                        return;
                    }
                }
            }
            listener.onComplete(Tasks.forResult(null));
        });
    }
}
