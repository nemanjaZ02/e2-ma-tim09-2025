package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.SpecialBoss;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.SpecialTask;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialMissionStatus;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;
import com.e2_ma_tim09_2025.questify.repositories.AllianceRepository;
import com.e2_ma_tim09_2025.questify.repositories.SpecialBossRepository;
import com.e2_ma_tim09_2025.questify.repositories.SpecialMissionRepository;
import com.e2_ma_tim09_2025.questify.repositories.SpecialTaskRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecialMissionService {
    private final AllianceRepository allianceRepository;
    private final SpecialMissionRepository specialMissionRepository;
    private final SpecialBossRepository specialBossRepository;
    private final SpecialTaskRepository specialTaskRepository;

    @Inject
    public SpecialMissionService(
            AllianceRepository allianceRepository,
            SpecialMissionRepository specialMissionRepository,
            SpecialBossRepository specialBossRepository,
            SpecialTaskRepository specialTaskRepository) {
        this.allianceRepository = allianceRepository;
        this.specialMissionRepository = specialMissionRepository;
        this.specialBossRepository = specialBossRepository;
        this.specialTaskRepository = specialTaskRepository;
    }

    public void createSpecialMission(String allianceId, String leaderId, OnCompleteListener<Boolean> listener) {
        Log.d("SpecialMissionService", "=== KREIRANJE SPECIJALNE MISIJE ===");
        Log.d("SpecialMissionService", "Alliance ID: " + allianceId);
        Log.d("SpecialMissionService", "Leader ID: " + leaderId);

        // 1. Proveri da li savez postoji i da li je korisnik vođa
        allianceRepository.getAlliance(allianceId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                Log.e("SpecialMissionService", "Savez ne postoji");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            Alliance alliance = task.getResult().toObject(Alliance.class);
            if (alliance == null) {
                Log.e("SpecialMissionService", "Savez je null");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            // Proveri da li je korisnik vođa
            if (!leaderId.equals(alliance.getLeaderId())) {
                Log.e("SpecialMissionService", "Korisnik nije vodja saveza");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            // Proveri da li već postoji aktivna misija
            specialMissionRepository.getSpecialMissionByAllianceId(allianceId, missionTask -> {
                if (missionTask.isSuccessful() && missionTask.getResult() != null && missionTask.getResult().exists()) {
                    Log.e("SpecialMissionService", "Već postoji aktivna misija za ovaj savez");
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }

                // Kreiraj misiju
                createMissionData(alliance, listener);
            });
        });
    }

    private void createMissionData(Alliance alliance, OnCompleteListener<Boolean> listener) {
        // 1. Kreiraj SpecialMission
        SpecialMission specialMission = new SpecialMission(alliance.getId());
        
        // 2. Kreiraj SpecialBoss
        String bossId = UUID.randomUUID().toString();
        SpecialBoss specialBoss = new SpecialBoss(specialMission.getAllianceId(), alliance.getId(), alliance.getMemberIds().size());
        specialBoss.setId(bossId);

        // 3. Kreiraj SpecialTask-ove za sve članove (6 tipova × broj članova)
        List<SpecialTask> specialTasks = createSpecialTasksForAlliance(alliance);

        // 4. Sačuvaj sve u Firebase
        saveMissionData(specialMission, specialBoss, specialTasks, listener);
    }

    private List<SpecialTask> createSpecialTasksForAlliance(Alliance alliance) {
        List<SpecialTask> tasks = new ArrayList<>();
        String specialMissionId = alliance.getId(); // allianceId = specialMissionId

        for (String memberId : alliance.getMemberIds()) {
            // Kreiraj svih 6 tipova zadataka za svakog člana
            for (SpecialTaskType taskType : SpecialTaskType.values()) {
                String taskId = UUID.randomUUID().toString();
                SpecialTask task = new SpecialTask(memberId, specialMissionId, alliance.getId(), taskType);
                task.setId(taskId);
                tasks.add(task);
            }
        }

        Log.d("SpecialMissionService", "Kreirano " + tasks.size() + " special task-ova za " + alliance.getMemberIds().size() + " članova");
        return tasks;
    }

 
    private void saveMissionData(SpecialMission specialMission, SpecialBoss specialBoss, 
            List<SpecialTask> specialTasks, OnCompleteListener<Boolean> listener) {
        
        // 1. Sačuvaj SpecialMission
        specialMissionRepository.createSpecialMission(specialMission, task1 -> {
            if (!task1.isSuccessful()) {
                Log.e("SpecialMissionService", "Greška pri čuvanju SpecialMission");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            // 2. Sačuvaj SpecialBoss
            specialBossRepository.createSpecialBoss(specialBoss, task2 -> {
                if (!task2.isSuccessful()) {
                    Log.e("SpecialMissionService", "Greška pri čuvanju SpecialBoss");
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }

                // 3. Sačuvaj sve SpecialTask-ove
                specialTaskRepository.createSpecialTasksForAlliance(specialTasks, task3 -> {
                    if (!task3.isSuccessful()) {
                        Log.e("SpecialMissionService", "Greška pri čuvanju SpecialTask-ova");
                        listener.onComplete(Tasks.forResult(false));
                        return;
                    }

                    Log.d("SpecialMissionService", "✅ Specijalna misija uspešno kreirana!");
                    listener.onComplete(Tasks.forResult(true));
                });
            });
        });
    }

    public void getSpecialMission(String allianceId, OnCompleteListener<SpecialMission> listener) {
        specialMissionRepository.getSpecialMissionByAllianceId(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                SpecialMission mission = task.getResult().toObject(SpecialMission.class);
                listener.onComplete(Tasks.forResult(mission));
            } else {
                listener.onComplete(Tasks.forResult(null));
            }
        });
    }

    public void getSpecialBoss(String allianceId, OnCompleteListener<SpecialBoss> listener) {
        specialBossRepository.getSpecialBossByAllianceId(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                SpecialBoss boss = task.getResult().toObject(SpecialBoss.class);
                listener.onComplete(Tasks.forResult(boss));
            } else {
                listener.onComplete(Tasks.forResult(null));
            }
        });
    }

    public void canCreateSpecialMission(String allianceId, String userId, OnCompleteListener<Boolean> listener) {
        allianceRepository.getAlliance(allianceId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            Alliance alliance = task.getResult().toObject(Alliance.class);
            if (alliance == null || !userId.equals(alliance.getLeaderId())) {
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            // Proveri da li već postoji aktivna misija
            specialMissionRepository.getSpecialMissionByAllianceId(allianceId, missionTask -> {
                boolean canCreate = !(missionTask.isSuccessful() && missionTask.getResult() != null && missionTask.getResult().exists());
                listener.onComplete(Tasks.forResult(canCreate));
            });
        });
    }

    /**
     * Proveri da li je misija istekla ili boss pobeden i ažuriraj status
     */
    public void checkAndCompleteMission(String allianceId, OnCompleteListener<Boolean> listener) {
        getSpecialMission(allianceId, missionTask -> {
            if (!missionTask.isSuccessful() || missionTask.getResult() == null) {
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            SpecialMission mission = missionTask.getResult();
            if (mission.getStatus() != SpecialMissionStatus.ACTIVE) {
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            // Proveri da li je misija istekla
            if (mission.isExpired()) {
                Log.d("SpecialMissionService", "Mission expired without defeating boss");
                mission.setStatus(SpecialMissionStatus.EXPIRED);
                specialMissionRepository.updateSpecialMission(mission, updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d("SpecialMissionService", "Mission status updated to EXPIRED");
                        listener.onComplete(Tasks.forResult(true));
                    } else {
                        listener.onComplete(Tasks.forResult(false));
                    }
                });
                return;
            }

            // Proveri da li je boss pobeden
            getSpecialBoss(allianceId, bossTask -> {
                if (!bossTask.isSuccessful() || bossTask.getResult() == null) {
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }

                SpecialBoss boss = bossTask.getResult();
                if (boss.isDefeated()) {
                    Log.d("SpecialMissionService", "Boss defeated! Distributing rewards...");
                    mission.setStatus(SpecialMissionStatus.DEFEATED);
                    specialMissionRepository.updateSpecialMission(mission, updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("SpecialMissionService", "Mission status updated to DEFEATED");
                            // TODO: Implement reward distribution
                            listener.onComplete(Tasks.forResult(true));
                        } else {
                            listener.onComplete(Tasks.forResult(false));
                        }
                    });
                } else {
                    // Misija je aktivna i boss nije pobeden
                    listener.onComplete(Tasks.forResult(false));
                }
            });
        });
    }
}
