package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.SpecialBoss;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.SpecialTask;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialMissionStatus;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;
import com.e2_ma_tim09_2025.questify.repositories.AllianceRepository;
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
    private final SpecialTaskRepository specialTaskRepository;

    @Inject
    public SpecialMissionService(
            AllianceRepository allianceRepository,
            SpecialMissionRepository specialMissionRepository,
            SpecialTaskRepository specialTaskRepository) {
        this.allianceRepository = allianceRepository;
        this.specialMissionRepository = specialMissionRepository;
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

            // Proveri da li već postoji misija
            specialMissionRepository.getSpecialMissionByAllianceId(allianceId, missionTask -> {
                if (missionTask.isSuccessful() && missionTask.getResult() != null && missionTask.getResult().exists()) {
                    // Ažuriraj postojeću misiju da bude aktivna
                    SpecialMission existingMission = missionTask.getResult().toObject(SpecialMission.class);
                    if (existingMission != null && !existingMission.isActive()) {
                        activateMission(existingMission, alliance, listener);
                    } else {
                        Log.e("SpecialMissionService", "Već postoji aktivna misija za ovaj savez");
                        listener.onComplete(Tasks.forResult(false));
                    }
                    return;
                }

                // Kreiraj novu misiju
                createMissionData(alliance, listener);
            });
        });
    }

    private void createMissionData(Alliance alliance, OnCompleteListener<Boolean> listener) {
        // 1. Kreiraj SpecialMission
        SpecialMission specialMission = new SpecialMission(alliance.getId());
        specialMission.setStatus(SpecialMissionStatus.ACTIVE); // Aktivna odmah
        
        // 2. Kreiraj SpecialBoss i dodaj u misiju
        String bossId = UUID.randomUUID().toString();
        SpecialBoss specialBoss = new SpecialBoss(specialMission.getAllianceId(), alliance.getId(), alliance.getMemberIds().size());
        specialBoss.setId(bossId);
        specialMission.setBoss(specialBoss); // Dodaj boss u misiju

        // 3. Kreiraj SpecialTask-ove za sve članove (6 tipova × broj članova)
        List<SpecialTask> specialTasks = createSpecialTasksForAlliance(alliance);

        // 4. Sačuvaj sve u Firebase (bez zasebnog boss-a)
        saveMissionData(specialMission, specialTasks, listener);
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

 
    private void saveMissionData(SpecialMission specialMission, 
            List<SpecialTask> specialTasks, OnCompleteListener<Boolean> listener) {
        
        // 1. Sačuvaj SpecialMission (sa boss-om unutar nje)
        specialMissionRepository.createSpecialMission(specialMission, task1 -> {
            if (!task1.isSuccessful()) {
                Log.e("SpecialMissionService", "Greška pri čuvanju SpecialMission");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            // 2. Sačuvaj sve SpecialTask-ove
            specialTaskRepository.createSpecialTasksForAlliance(specialTasks, task2 -> {
                if (!task2.isSuccessful()) {
                    Log.e("SpecialMissionService", "Greška pri čuvanju SpecialTask-ova");
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }

                Log.d("SpecialMissionService", "✅ Specijalna misija uspešno kreirana!");
                
                // Postavi alliance.isMissionStarted = true
                updateAllianceMissionStatus(specialMission.getAllianceId(), true, () -> {
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


    public void canCreateSpecialMission(String allianceId, String userId, OnCompleteListener<Boolean> listener) {
        Log.d("SpecialMissionService", "=== CAN CREATE SPECIAL MISSION ===");
        Log.d("SpecialMissionService", "Alliance ID: " + allianceId);
        Log.d("SpecialMissionService", "User ID: " + userId);
        
        allianceRepository.getAlliance(allianceId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                Log.d("SpecialMissionService", "Alliance not found");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            Alliance alliance = task.getResult().toObject(Alliance.class);
            if (alliance == null || !userId.equals(alliance.getLeaderId())) {
                Log.d("SpecialMissionService", "User is not leader. Leader ID: " + (alliance != null ? alliance.getLeaderId() : "null"));
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            Log.d("SpecialMissionService", "User is leader, checking mission status...");

            // Proveri da li već postoji aktivna misija
            specialMissionRepository.getSpecialMissionByAllianceId(allianceId, missionTask -> {
                if (missionTask.isSuccessful() && missionTask.getResult() != null && missionTask.getResult().exists()) {
                    // Misija postoji - proveri da li je aktivna
                    SpecialMission mission = missionTask.getResult().toObject(SpecialMission.class);
                    boolean canCreate = (mission == null || !mission.isActive());
                    Log.d("SpecialMissionService", "Mission exists. Active: " + (mission != null ? mission.isActive() : "null") + ", Can create: " + canCreate);
                    listener.onComplete(Tasks.forResult(canCreate));
                } else {
                    // Misija ne postoji - može da se kreira
                    Log.d("SpecialMissionService", "No mission exists, can create: true");
                    listener.onComplete(Tasks.forResult(true));
                }
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
                        // Označava sve taskove kao INACTIVE i postavi alliance.isMissionStarted = false
                        markAllTasksAsInactive(allianceId, () -> {
                            updateAllianceMissionStatus(allianceId, false, () -> {
                                listener.onComplete(Tasks.forResult(true));
                            });
                        });
                    } else {
                        listener.onComplete(Tasks.forResult(false));
                    }
                });
                return;
            }

            // Proveri da li je boss pobeden (koristi boss iz misije)
            if (mission.isBossDefeated()) {
                Log.d("SpecialMissionService", "Boss defeated! Distributing rewards...");
                mission.setStatus(SpecialMissionStatus.DEFEATED);
                specialMissionRepository.updateSpecialMission(mission, updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d("SpecialMissionService", "Mission status updated to DEFEATED");
                        // Označava sve taskove kao INACTIVE i postavi alliance.isMissionStarted = false
                        markAllTasksAsInactive(allianceId, () -> {
                            updateAllianceMissionStatus(allianceId, false, () -> {
                                listener.onComplete(Tasks.forResult(true));
                            });
                        });
                    } else {
                        listener.onComplete(Tasks.forResult(false));
                    }
                });
            } else {
                // Misija je aktivna i boss nije pobeden
                listener.onComplete(Tasks.forResult(false));
            }
        });
    }
    
    private void markAllTasksAsInactive(String allianceId, Runnable onComplete) {
        Log.d("SpecialMissionService", "Označavam sve taskove kao INACTIVE za alijansu: " + allianceId);
        
        specialTaskRepository.getSpecialTasksByAllianceId(allianceId, task -> {
            if (task.isSuccessful()) {
                List<SpecialTask> tasks = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    SpecialTask specialTask = doc.toObject(SpecialTask.class);
                    if (specialTask != null && specialTask.getStatus().toString().equals("ACTIVE")) {
                        // Označava samo ACTIVE taskove kao INACTIVE
                        specialTask.setStatus(com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskStatus.INACTIVE);
                        tasks.add(specialTask);
                        Log.d("SpecialMissionService", "Task " + specialTask.getTaskType() + " označen kao INACTIVE");
                    }
                }
                
                if (!tasks.isEmpty()) {
                    Log.d("SpecialMissionService", "Ažuriram " + tasks.size() + " taskova kao INACTIVE");
                    specialTaskRepository.updateSpecialTasks(tasks, updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("SpecialMissionService", "Svi taskovi uspešno označeni kao INACTIVE");
                        } else {
                            Log.e("SpecialMissionService", "Greška pri označavanju taskova kao INACTIVE", updateTask.getException());
                        }
                        onComplete.run();
                    });
                } else {
                    Log.d("SpecialMissionService", "Nema ACTIVE taskova za označavanje");
                    onComplete.run();
                }
            } else {
                Log.e("SpecialMissionService", "Greška pri dohvatanju taskova", task.getException());
                onComplete.run();
            }
        });
    }
    
    private void activateMission(SpecialMission mission, Alliance alliance, OnCompleteListener<Boolean> listener) {
        Log.d("SpecialMissionService", "Aktiviranje postojeće misije");
        
        // 1. Aktiviraj misiju
        mission.setStatus(SpecialMissionStatus.ACTIVE);
        mission.setStartTime(System.currentTimeMillis());
        mission.setEndTime(mission.getStartTime() + (14 * 24 * 60 * 60 * 1000L)); // 2 nedelje
        
        // 2. Ažuriraj misiju u bazi
        specialMissionRepository.updateSpecialMission(mission, task -> {
            if (!task.isSuccessful()) {
                Log.e("SpecialMissionService", "Greška pri ažuriranju misije");
                listener.onComplete(Tasks.forResult(false));
                return;
            }
            
            // 3. Kreiraj nove zadatke za sve članove
            List<SpecialTask> newTasks = createSpecialTasksForAlliance(alliance);
            specialTaskRepository.createSpecialTasksForAlliance(newTasks, task2 -> {
                if (!task2.isSuccessful()) {
                    Log.e("SpecialMissionService", "Greška pri kreiranju zadataka");
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }
                
                // 4. Postavi alliance.isMissionStarted = true
                updateAllianceMissionStatus(alliance.getId(), true, () -> {
                    Log.d("SpecialMissionService", "✅ Misija uspešno aktivirana!");
                    listener.onComplete(Tasks.forResult(true));
                });
            });
        });
    }
    
    private void updateAllianceMissionStatus(String allianceId, boolean missionStarted, Runnable onComplete) {
        allianceRepository.getAlliance(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Alliance alliance = task.getResult().toObject(Alliance.class);
                if (alliance != null) {
                    alliance.setMissionStarted(missionStarted);
                    allianceRepository.updateAlliance(alliance, updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("SpecialMissionService", "Alliance mission status updated to: " + missionStarted);
                        } else {
                            Log.e("SpecialMissionService", "Failed to update alliance mission status");
                        }
                        onComplete.run();
                    });
                } else {
                    onComplete.run();
                }
            } else {
                onComplete.run();
            }
        });
    }
}
