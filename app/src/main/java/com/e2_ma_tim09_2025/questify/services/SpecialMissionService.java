package com.e2_ma_tim09_2025.questify.services;

import android.os.CountDownTimer;
import android.util.Log;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.SpecialBoss;
import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.SpecialTask;
import com.e2_ma_tim09_2025.questify.models.Task;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.enums.EquipmentType;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialMissionStatus;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskStatus;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;
import com.e2_ma_tim09_2025.questify.models.enums.TaskStatus;
import com.e2_ma_tim09_2025.questify.repositories.AllianceRepository;
import com.e2_ma_tim09_2025.questify.repositories.SpecialMissionRepository;
import com.e2_ma_tim09_2025.questify.repositories.SpecialTaskRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.e2_ma_tim09_2025.questify.repositories.EquipmentRepository;
import com.e2_ma_tim09_2025.questify.repositories.BossRepository;
import com.e2_ma_tim09_2025.questify.repositories.TaskRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecialMissionService {
    private final AllianceRepository allianceRepository;
    private final SpecialMissionRepository specialMissionRepository;
    private final SpecialTaskRepository specialTaskRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final BossRepository bossRepository;
    private final TaskRepository taskRepository;
    
    // Timer za praćenje završetka misije
    private CountDownTimer missionTimer;

    @Inject
    public SpecialMissionService(
            AllianceRepository allianceRepository,
            SpecialMissionRepository specialMissionRepository,
            SpecialTaskRepository specialTaskRepository,
            UserRepository userRepository,
            EquipmentRepository equipmentRepository,
            BossRepository bossRepository,
            TaskRepository taskRepository) {
        this.allianceRepository = allianceRepository;
        this.specialMissionRepository = specialMissionRepository;
        this.specialTaskRepository = specialTaskRepository;
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
        this.bossRepository = bossRepository;
        this.taskRepository = taskRepository;
    }
    
    /**
     * Pokreni timer za misiju - poziva se kada se misija aktivira
     */
    private void startMissionTimer(String allianceId, long endTime) {
        // Zaustavi postojeći timer ako postoji
        stopMissionTimer();
        
        long currentTime = System.currentTimeMillis();
        long timeUntilEnd = endTime - currentTime;
        
        if (timeUntilEnd <= 0) {
            // Misija je već istekla
            Log.d("SpecialMissionService", "Mission already expired for alliance: " + allianceId);
            onMissionTimeExpired(allianceId);
            return;
        }
        
        Log.d("SpecialMissionService", "Starting mission timer for alliance: " + allianceId + 
              ", time until end: " + (timeUntilEnd / 1000) + " seconds");
        
        missionTimer = new CountDownTimer(timeUntilEnd, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Možete dodati logiku ovde ako treba
            }
            
            @Override
            public void onFinish() {
                Log.d("SpecialMissionService", "Mission timer finished for alliance: " + allianceId);
                onMissionTimeExpired(allianceId);
            }
        };
        
        missionTimer.start();
    }
    
    /**
     * Zaustavi timer
     */
    private void stopMissionTimer() {
        if (missionTimer != null) {
            Log.d("SpecialMissionService", "Stopping mission timer");
            missionTimer.cancel();
            missionTimer = null;
        }
    }
    

    private void onMissionTimeExpired(String allianceId) {
        Log.d("SpecialMissionService", "=== TIMER ISTEKAO - PROVERAVAM STATUS MISIJE ===");
        Log.d("SpecialMissionService", "Alliance ID: " + allianceId);
        
        // Koristi postojeću logiku za proveru i ažuriranje statusa
        checkAndCompleteMission(allianceId, task -> {
            if (task.isSuccessful()) {
                Log.d("SpecialMissionService", "Status misije uspešno ažuriran preko checkAndCompleteMission");
            } else {
                Log.e("SpecialMissionService", "Greška pri ažuriranju statusa misije preko checkAndCompleteMission");
            }
        });
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
        // 1. Kreiraj SpecialMission (neaktivna)
        SpecialMission specialMission = new SpecialMission(alliance.getId());
        specialMission.setStatus(SpecialMissionStatus.INACTIVE); // Neaktivna na početku
        specialMission.setStartTime(0); // Nije pokrenuta
        specialMission.setEndTime(0); // Nije pokrenuta
        specialMission.setMissionNumber(0); // Početni broj - nije pokrenuta
        
        // 2. Kreiraj SpecialBoss i dodaj u misiju
        SpecialBoss specialBoss = new SpecialBoss(specialMission.getAllianceId(), alliance.getId(), alliance.getMemberIds().size());
        specialMission.setBoss(specialBoss); // Dodaj boss u misiju

        // 3. Sačuvaj misiju u Firebase (bez taskova - oni se kreiraju kada se aktivira)
        specialMissionRepository.createSpecialMission(specialMission, task -> {
            if (!task.isSuccessful()) {
                Log.e("SpecialMissionService", "Greška pri čuvanju SpecialMission");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            Log.d("SpecialMissionService", "✅ Specijalna misija uspešno kreirana (neaktivna)!");
            listener.onComplete(Tasks.forResult(true));
        });
    }

    private List<SpecialTask> createSpecialTasksForAlliance(Alliance alliance, int missionNumber) {
        List<SpecialTask> tasks = new ArrayList<>();
        String specialMissionId = alliance.getId(); // allianceId = specialMissionId

        for (String memberId : alliance.getMemberIds()) {
            // Kreiraj svih 6 tipova zadataka za svakog člana
            for (SpecialTaskType taskType : SpecialTaskType.values()) {
                String taskId = UUID.randomUUID().toString();
                SpecialTask task = new SpecialTask(memberId, specialMissionId, alliance.getId(), missionNumber, taskType);
                task.setId(taskId);
                tasks.add(task);
            }
        }

        Log.d("SpecialMissionService", "Kreirano " + tasks.size() + " special task-ova za " + alliance.getMemberIds().size() + " članova sa mission number: " + missionNumber);
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


                // Increment startedMissions for all alliance members
                incrementStartedMissionsForAlliance(specialMission.getAllianceId(), () -> {
                    // Postavi alliance.isMissionStarted = true
                    updateAllianceMissionStatus(specialMission.getAllianceId(), true, () -> {

                        startMissionTimer(specialMission.getAllianceId(), specialMission.getEndTime());
                        listener.onComplete(Tasks.forResult(true));
                    });

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

            // Prvo proveri NO_UNRESOLVED_TASKS taskove i skini health bossu ako treba
            checkAndUpdateNoUnresolvedTasks(allianceId, mission.getMissionNumber(), () -> {
                // Ažuriraj mission objekat sa najnovijim podacima iz baze
                specialMissionRepository.getSpecialMissionByAllianceId(allianceId, updatedMissionTask -> {
                    if (updatedMissionTask.isSuccessful() && updatedMissionTask.getResult() != null && updatedMissionTask.getResult().exists()) {
                        SpecialMission updatedMission = updatedMissionTask.getResult().toObject(SpecialMission.class);
                        if (updatedMission != null) {
                            // Koristi ažuriranu misiju za proveru
                            checkMissionStatus(updatedMission, allianceId, listener);
                        } else {
                            listener.onComplete(Tasks.forResult(false));
                        }
                    } else {
                        listener.onComplete(Tasks.forResult(false));
                    }
                });
            });
    });
    }
    
    private void checkMissionStatus(SpecialMission mission, String allianceId, OnCompleteListener<Boolean> listener) {
        // Proveri da li je boss pobeden
        if (mission.isBossDefeated()) {
            Log.d("SpecialMissionService", "Boss defeated! Distributing rewards...");
            mission.setStatus(SpecialMissionStatus.DEFEATED);
            specialMissionRepository.updateSpecialMission(mission, updateTask -> {
                if (updateTask.isSuccessful()) {
                    Log.d("SpecialMissionService", "Mission status updated to DEFEATED");
                    // Označava sve taskove kao INACTIVE i postavi alliance.isMissionStarted = false
                    markAllTasksAsInactive(allianceId, () -> {
                        // Dodeli bedževe igračima
                        distributeBadges(allianceId, mission.getMissionNumber(), () -> {
                            // Dodeli nagrade igračima (coins, potion, clothes) - samo ako je boss pobeden
                            distributeRewards(allianceId, mission, () -> {
                                incrementFinishedMissionsForAlliance(allianceId, () -> {
                                    // Označava sve taskove kao INACTIVE i postavi alliance.isMissionStarted = false
                                    markAllTasksAsInactive(allianceId, () -> {
                                        updateAllianceMissionStatus(allianceId, false, () -> {
                                            listener.onComplete(Tasks.forResult(true));
                                        });
                                    });
                                });
                            });
                        });
                    });
                } else {
                    listener.onComplete(Tasks.forResult(false));
                }
            });
        } else {
            // Boss nije pobeden - misija je istekla
            Log.d("SpecialMissionService", "Boss not defeated - mission expired");
            mission.setStatus(SpecialMissionStatus.EXPIRED);
            specialMissionRepository.updateSpecialMission(mission, updateTask -> {
                if (updateTask.isSuccessful()) {
                    Log.d("SpecialMissionService", "Mission status updated to EXPIRED");
                    // Označava sve taskove kao INACTIVE i postavi alliance.isMissionStarted = false
                    markAllTasksAsInactive(allianceId, () -> {
                        // Dodeli bedževe igračima
                        distributeBadges(allianceId, mission.getMissionNumber(), () -> {
                            updateAllianceMissionStatus(allianceId, false, () -> {
                                // Zaustavi timer jer je misija završena
                                stopMissionTimer();
                                listener.onComplete(Tasks.forResult(true));
                            });
                        });
                    });
                } else {
                    listener.onComplete(Tasks.forResult(false));
                }
            });
        }
    }
    
    private void markAllTasksAsInactive(String allianceId, Runnable onComplete) {
        Log.d("SpecialMissionService", "Označavam sve taskove kao EXPIRED za alijansu: " + allianceId);
        
        specialTaskRepository.getSpecialTasksByAllianceId(allianceId, task -> {
            if (task.isSuccessful()) {
                List<SpecialTask> tasks = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    SpecialTask specialTask = doc.toObject(SpecialTask.class);
                    if (specialTask != null && specialTask.getStatus().toString().equals("ACTIVE")) {
                        // Preskoči NO_UNRESOLVED_TASKS taskove - oni će biti obrađeni u checkAndUpdateNoUnresolvedTasks
                        if (specialTask.getTaskType() == SpecialTaskType.NO_UNRESOLVED_TASKS) {
                            Log.d("SpecialMissionService", "Preskačem NO_UNRESOLVED_TASKS task - biće obrađen posebno");
                            continue;
                        }
                        
                        // Označava samo ACTIVE taskove kao EXPIRED
                        specialTask.setStatus(com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskStatus.EXPIRED);
                        tasks.add(specialTask);
                        Log.d("SpecialMissionService", "Task " + specialTask.getTaskType() + " označen kao EXPIRED");
                    }
                }
                
                if (!tasks.isEmpty()) {
                    Log.d("SpecialMissionService", "Ažuriram " + tasks.size() + " taskova kao EXPIRED");
                    for (SpecialTask specialTask : tasks) {
                        Log.d("SpecialMissionService", "Updating task " + specialTask.getTaskType() + " to EXPIRED");
                    }
                    specialTaskRepository.updateSpecialTasks(tasks, updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("SpecialMissionService", "✅ Svi taskovi uspešno označeni kao EXPIRED");
                        } else {
                            Log.e("SpecialMissionService", "❌ Greška pri označavanju taskova kao EXPIRED", updateTask.getException());
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
        // mission.setEndTime(mission.getStartTime() + (14 * 24 * 60 * 60 * 1000L)); // 2 nedelje
        mission.setEndTime(mission.getStartTime() + (2 * 60 * 1000L)); // 1 minut za testiranje
        
        // Povećaj mission number za 1
        mission.setMissionNumber(mission.getMissionNumber() + 1);
        
        // 2. Resetuj boss-a za novu misiju
        SpecialBoss newBoss = new SpecialBoss(mission.getAllianceId(), alliance.getId(), alliance.getMemberIds().size());
        mission.setBoss(newBoss);
        
        // 3. Ažuriraj misiju u bazi
        specialMissionRepository.updateSpecialMission(mission, task -> {
            if (!task.isSuccessful()) {
                Log.e("SpecialMissionService", "Greška pri ažuriranju misije");
                listener.onComplete(Tasks.forResult(false));
                return;
            }
            
            // 4. Prvo dohvati broj NOT_COMPLETED taskova za sve članove, pa tek onda kreiraj taskove
            getNotCompletedTasksCountForAllMembers(alliance.getMemberIds(), (notCompletedCounts) -> {
                if (!notCompletedCounts.isSuccessful()) {
                    Log.e("SpecialMissionService", "Failed to get NOT_COMPLETED counts");
                    listener.onComplete(Tasks.forResult(false));
                    return;
                }


                // 5. Kreiraj taskove za sve članove (kada se aktivira misija)
                List<SpecialTask> newTasks = createSpecialTasksForAlliance(alliance, mission.getMissionNumber());
                
                // 6. Postavi notCompletedTasksBeforeActivation za NO_UNRESOLVED_TASKS taskove
                Map<String, Integer> counts = notCompletedCounts.getResult();
                Log.d("SpecialMissionService", "=== SETTING NOT_COMPLETED_TASKS_BEFORE_ACTIVATION ===");
                Log.d("SpecialMissionService", "Counts map: " + counts);
                Log.d("SpecialMissionService", "New tasks count: " + newTasks.size());
                
                for (SpecialTask specialTask : newTasks) {
                    if (specialTask.getTaskType() == SpecialTaskType.NO_UNRESOLVED_TASKS) {
                        String userId = specialTask.getUserId();
                        int count = counts.getOrDefault(userId, 0);
                        specialTask.setNotCompletedTasksBeforeActivation(count);
                        Log.d("SpecialMissionService", "User: " + userId);
                        Log.d("SpecialMissionService", "Task ID: " + specialTask.getId());
                        Log.d("SpecialMissionService", "NOT_COMPLETED tasks before activation: " + count);
                        Log.d("SpecialMissionService", "Task set to: " + specialTask.getNotCompletedTasksBeforeActivation());
                    }
                }
                
                // 7. Kreiraj taskove u bazi
                specialTaskRepository.createSpecialTasksForAlliance(newTasks, task2 -> {
                    if (!task2.isSuccessful()) {
                        Log.e("SpecialMissionService", "Greška pri kreiranju zadataka");
                        listener.onComplete(Tasks.forResult(false));
                        return;
                    }
                    
                    // 8. Ažuriraj taskove u bazi sa notCompletedTasksBeforeActivation
                    updateTasksWithNotCompletedCounts(newTasks, () -> {
                        // 9. Postavi alliance.isMissionStarted = true
                        // 4. Increment startedMissions for all alliance members
                        incrementStartedMissionsForAlliance(alliance.getId(), () -> {

                            updateAllianceMissionStatus(alliance.getId(), true, () -> {
                                // Pokreni timer za misiju
                                startMissionTimer(mission.getAllianceId(), mission.getEndTime());
                                Log.d("SpecialMissionService", "✅ Misija uspešno aktivirana!");
                                listener.onComplete(Tasks.forResult(true));
                            });
                        });
                    });
                });
            });
        });
    }

    /**
     * Ažuriraj taskove u bazi sa notCompletedTasksBeforeActivation
     */
    private void updateTasksWithNotCompletedCounts(List<SpecialTask> tasks, Runnable onComplete) {
        List<SpecialTask> noUnresolvedTasks = new ArrayList<>();
        for (SpecialTask task : tasks) {
            if (task.getTaskType() == SpecialTaskType.NO_UNRESOLVED_TASKS) {
                noUnresolvedTasks.add(task);
            }
        }
        
        if (noUnresolvedTasks.isEmpty()) {
            Log.d("SpecialMissionService", "No NO_UNRESOLVED_TASKS tasks to update");
            onComplete.run();
            return;
        }
        
        final int[] completedTasks = {0};
        final int totalTasks = noUnresolvedTasks.size();
        
        for (SpecialTask task : noUnresolvedTasks) {
            specialTaskRepository.updateSpecialTask(task, updateTask -> {
                if (updateTask.isSuccessful()) {
                    Log.d("SpecialMissionService", "Updated task " + task.getId() + " with notCompletedTasksBeforeActivation: " + task.getNotCompletedTasksBeforeActivation());
                } else {
                    Log.e("SpecialMissionService", "Failed to update task " + task.getId());
                }
                
                completedTasks[0]++;
                if (completedTasks[0] == totalTasks) {
                    Log.d("SpecialMissionService", "All NO_UNRESOLVED_TASKS tasks updated in database");
                    onComplete.run();
                }
            });
        }
    }
    
    /**
     * Dohvati broj NOT_COMPLETED taskova za sve članove alijanse
     */
    private void getNotCompletedTasksCountForAllMembers(List<String> memberIds, OnCompleteListener<Map<String, Integer>> listener) {
        Map<String, Integer> notCompletedCounts = new HashMap<>();
        final int[] completedMembers = {0};
        final int totalMembers = memberIds.size();
        
        Log.d("SpecialMissionService", "=== GETTING NOT_COMPLETED TASKS FOR ALL MEMBERS ===");
        Log.d("SpecialMissionService", "Total members: " + totalMembers);
        Log.d("SpecialMissionService", "Member IDs: " + memberIds);
        
        if (totalMembers == 0) {
            Log.d("SpecialMissionService", "No members found, returning empty map");
            listener.onComplete(Tasks.forResult(notCompletedCounts));
            return;
        }
        
        for (String userId : memberIds) {
            Log.d("SpecialMissionService", "Getting NOT_COMPLETED count for user: " + userId);
            getNotCompletedTasksCount(userId, countTask -> {
                if (!countTask.isSuccessful()) {
                    Log.e("SpecialMissionService", "Failed to get count for user: " + userId);
                    notCompletedCounts.put(userId, 0);
                } else {
                    int count = countTask.getResult();
                    notCompletedCounts.put(userId, count);
                    Log.d("SpecialMissionService", "User " + userId + " has " + count + " NOT_COMPLETED tasks before activation");
                }
                
                completedMembers[0]++;
                Log.d("SpecialMissionService", "Completed members: " + completedMembers[0] + "/" + totalMembers);
                
                if (completedMembers[0] == totalMembers) {
                    Log.d("SpecialMissionService", "All members processed. Final counts: " + notCompletedCounts);
                    listener.onComplete(Tasks.forResult(notCompletedCounts));
                }
            });
        }
    }
    
    /**
     * Postavi notCompletedTasksBeforeActivation za NO_UNRESOLVED_TASKS taskove
     */
    private void setNotCompletedTasksBeforeActivation(List<SpecialTask> tasks, Runnable onComplete) {
        Log.d("SpecialMissionService", "Setting notCompletedTasksBeforeActivation for NO_UNRESOLVED_TASKS tasks");
        
        // Grupiši taskove po korisnicima
        Map<String, List<SpecialTask>> userTasks = new HashMap<>();
        for (SpecialTask task : tasks) {
            if (task.getTaskType() == SpecialTaskType.NO_UNRESOLVED_TASKS) {
                String userId = task.getUserId();
                if (!userTasks.containsKey(userId)) {
                    userTasks.put(userId, new ArrayList<>());
                }
                userTasks.get(userId).add(task);
            }
        }
        
        if (userTasks.isEmpty()) {
            Log.d("SpecialMissionService", "No NO_UNRESOLVED_TASKS tasks found");
            onComplete.run();
            return;
        }
        
        final int[] completedUsers = {0};
        final int totalUsers = userTasks.size();
        
        for (Map.Entry<String, List<SpecialTask>> entry : userTasks.entrySet()) {
            String userId = entry.getKey();
            List<SpecialTask> userTaskList = entry.getValue();
            
            // Dohvati broj NOT_COMPLETED taskova za ovog korisnika
            getNotCompletedTasksCount(userId, countTask -> {
                int count = countTask.getResult();
                // Postavi count za sve NO_UNRESOLVED_TASKS taskove ovog korisnika
                for (SpecialTask task : userTaskList) {
                    task.setNotCompletedTasksBeforeActivation(count);
                    Log.d("SpecialMissionService", "=== SETTING NOT_COMPLETED_TASKS_BEFORE_ACTIVATION ===");
                    Log.d("SpecialMissionService", "User: " + userId);
                    Log.d("SpecialMissionService", "Task ID: " + task.getId());
                    Log.d("SpecialMissionService", "NOT_COMPLETED tasks before activation: " + count);
                    Log.d("SpecialMissionService", "Task set to: " + task.getNotCompletedTasksBeforeActivation());
                }
                
                completedUsers[0]++;
                if (completedUsers[0] == totalUsers) {
                    Log.d("SpecialMissionService", "All NO_UNRESOLVED_TASKS tasks updated");
                    onComplete.run();
                }
            });
        }
    }
    
    /**
     * Dohvati broj NOT_COMPLETED taskova za korisnika
     */
    private void getNotCompletedTasksCount(String userId, OnCompleteListener<Integer> listener) {
        Log.d("SpecialMissionService", "=== GETTING NOT_COMPLETED TASKS COUNT ===");
        Log.d("SpecialMissionService", "User: " + userId);
        
        // Pokreni u novom thread-u da ne blokira UI
        new Thread(() -> {
            try {
                List<com.e2_ma_tim09_2025.questify.models.Task> userTasks = taskRepository.getTasksByUser(userId);
                int count = 0;
                Log.d("SpecialMissionService", "Total tasks found: " + userTasks.size());
                
                for (com.e2_ma_tim09_2025.questify.models.Task userTask : userTasks) {
                    Log.d("SpecialMissionService", "Task: " + userTask.getName() + ", Status: " + userTask.getStatus());
                    if (userTask.getStatus() == TaskStatus.NOT_COMPLETED) {
                        count++;
                        Log.d("SpecialMissionService", "NOT_COMPLETED task found: " + userTask.getName());
                    }
                }
                Log.d("SpecialMissionService", "Final count: " + count + " NOT_COMPLETED tasks");
                listener.onComplete(Tasks.forResult(count));
            } catch (Exception e) {
                Log.e("SpecialMissionService", "Failed to get tasks for user: " + userId, e);
                listener.onComplete(Tasks.forResult(0));
            }
        }).start();
    }
    
    /**
     * Proveri NO_UNRESOLVED_TASKS taskove i postavi status na COMPLETED ili EXPIRED
     */
    private void checkAndUpdateNoUnresolvedTasks(String allianceId, int missionNumber, Runnable onComplete) {
        Log.d("SpecialMissionService", "=== CHECKING NO_UNRESOLVED_TASKS TASKS ===");
        Log.d("SpecialMissionService", "Alliance ID: " + allianceId + ", Mission Number: " + missionNumber);
        
        // Dohvati sve taskove za ovu misiju
        specialTaskRepository.getSpecialTasksByAllianceId(allianceId, task -> {
            if (!task.isSuccessful()) {
                Log.e("SpecialMissionService", "Failed to get tasks for NO_UNRESOLVED_TASKS check");
                onComplete.run();
                return;
            }
            
            List<SpecialTask> tasksToUpdate = new ArrayList<>();
            
            for (DocumentSnapshot doc : task.getResult()) {
                SpecialTask specialTask = doc.toObject(SpecialTask.class);
                if (specialTask != null && 
                    specialTask.getMissionNumber() == missionNumber && 
                    specialTask.getTaskType() == SpecialTaskType.NO_UNRESOLVED_TASKS) {
                    
                    // Proveri da li je korisnik uspešno završio task
                    checkNoUnresolvedTaskForUser(specialTask, () -> {
                        tasksToUpdate.add(specialTask);
                        
                        // Ako smo proverili sve taskove, ažuriraj ih
                        if (tasksToUpdate.size() == getNoUnresolvedTasksCount(task.getResult(), missionNumber)) {
                            if (!tasksToUpdate.isEmpty()) {
                                specialTaskRepository.updateSpecialTasks(tasksToUpdate, updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Log.d("SpecialMissionService", "NO_UNRESOLVED_TASKS tasks updated successfully");
                                    } else {
                                        Log.e("SpecialMissionService", "Failed to update NO_UNRESOLVED_TASKS tasks");
                                    }
                                    onComplete.run();
                                });
                            } else {
                                onComplete.run();
                            }
                        }
                    });
                }
            }
            
            // Ako nema NO_UNRESOLVED_TASKS taskova, završi odmah
            if (getNoUnresolvedTasksCount(task.getResult(), missionNumber) == 0) {
                Log.d("SpecialMissionService", "No NO_UNRESOLVED_TASKS tasks found for this mission");
                onComplete.run();
            }
        });
    }
    
    /**
     * Proveri NO_UNRESOLVED_TASKS task za jednog korisnika
     */
    private void checkNoUnresolvedTaskForUser(SpecialTask specialTask, Runnable onComplete) {
        String userId = specialTask.getUserId();
        int beforeActivation = specialTask.getNotCompletedTasksBeforeActivation();
        
        Log.d("SpecialMissionService", "=== CHECKING NO_UNRESOLVED_TASKS FOR USER ===");
        Log.d("SpecialMissionService", "User: " + userId);
        Log.d("SpecialMissionService", "Task ID: " + specialTask.getId());
        Log.d("SpecialMissionService", "Before activation: " + beforeActivation);
        Log.d("SpecialMissionService", "Task status: " + specialTask.getStatus());
        Log.d("SpecialMissionService", "Task type: " + specialTask.getTaskType());
        
        // Dohvati trenutni broj NOT_COMPLETED taskova
        getNotCompletedTasksCount(userId, countTask -> {
            int count = countTask.getResult();
            Log.d("SpecialMissionService", "=== NO_UNRESOLVED_TASKS DEBUG ===");
            Log.d("SpecialMissionService", "User: " + userId);
            Log.d("SpecialMissionService", "Before activation: " + beforeActivation);
            Log.d("SpecialMissionService", "Current count: " + count);
            Log.d("SpecialMissionService", "Comparison: " + count + " <= " + beforeActivation + " = " + (count <= beforeActivation));
            
            if (count <= beforeActivation) {
                // Uspešno - nema više NOT_COMPLETED taskova nego pre aktivacije
                // Koristi postojeću logiku za completovanje taska
                completeNoUnresolvedTask(specialTask);
                Log.d("SpecialMissionService", "✅ User " + userId + " successfully completed NO_UNRESOLVED_TASKS task");
            } else {
                // Neuspešno - ima više NOT_COMPLETED taskova nego pre aktivacije
                specialTask.setStatus(SpecialTaskStatus.EXPIRED);
                Log.d("SpecialMissionService", "❌ User " + userId + " failed NO_UNRESOLVED_TASKS task");
            }
            
            onComplete.run();
        });
    }
    
    /**
     * Broji koliko ima NO_UNRESOLVED_TASKS taskova za datu misiju
     */
    private int getNoUnresolvedTasksCount(QuerySnapshot querySnapshot, int missionNumber) {
        int count = 0;
        for (DocumentSnapshot doc : querySnapshot) {
            SpecialTask specialTask = doc.toObject(SpecialTask.class);
            if (specialTask != null && 
                specialTask.getMissionNumber() == missionNumber && 
                specialTask.getTaskType() == SpecialTaskType.NO_UNRESOLVED_TASKS) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Completuje NO_UNRESOLVED_TASKS task koristeći istu logiku kao SpecialTaskService
     */
    private void completeNoUnresolvedTask(SpecialTask specialTask) {
        Log.d("SpecialMissionService", "Completing NO_UNRESOLVED_TASKS task for user: " + specialTask.getUserId());
        
        // Proveri da li task može biti completovan
        if (!specialTask.canComplete()) {
            Log.e("SpecialMissionService", "Task cannot be completed (already completed or inactive)");
            return;
        }
        
        // Izvrši task (koristi postojeću logiku)
        specialTask.complete();
        
        Log.d("SpecialMissionService", "Task completed. Current count: " + specialTask.getCurrentCount() + 
              "/" + specialTask.getMaxCount() + ", Status: " + specialTask.getStatus());
        
        // Izračunaj damage i skini health bossu
        int damage = specialTask.getDamagePerCompletion();
        Log.d("SpecialMissionService", "Dealing " + damage + " damage to boss from NO_UNRESOLVED_TASKS task");
        
        // Pozovi logiku za skidanje health-a bossu
        dealDamageToBossFromNoUnresolvedTask(specialTask.getAllianceId(), damage);
    }
    
    /**
     * Skida health bossu kada se NO_UNRESOLVED_TASKS task completuje
     */
    private void dealDamageToBossFromNoUnresolvedTask(String allianceId, int damage) {
        specialMissionRepository.getSpecialMissionByAllianceId(allianceId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                Log.e("SpecialMissionService", "Greška pri dohvatanju misije za NO_UNRESOLVED_TASKS damage");
                return;
            }

            SpecialMission mission = task.getResult().toObject(SpecialMission.class);
            if (mission == null || mission.getBoss() == null) {
                Log.e("SpecialMissionService", "Misija ili boss je null za NO_UNRESOLVED_TASKS damage");
                return;
            }

            // Nanesei štetu koristeći convenience metodu
            mission.dealDamageToBoss(damage);
            Log.d("SpecialMissionService", "NO_UNRESOLVED_TASKS damage: " + damage + " HP. Boss HP: " + mission.getBossCurrentHealth() + "/" + mission.getBossMaxHealth());

            // Ažuriraj misiju (sa boss-om unutar nje)
            specialMissionRepository.updateSpecialMission(mission, task1 -> {
                if (!task1.isSuccessful()) {
                    Log.e("SpecialMissionService", "Greška pri ažuriranju misije za NO_UNRESOLVED_TASKS damage");
                    return;
                }

                // Ažuriraj ukupnu štetu u misiji
                mission.addDamage(damage);
                specialMissionRepository.updateSpecialMission(mission, task2 -> {
                    if (task2.isSuccessful()) {
                        Log.d("SpecialMissionService", "✅ NO_UNRESOLVED_TASKS damage successfully applied to boss!");
                    } else {
                        Log.e("SpecialMissionService", "❌ Failed to update mission damage for NO_UNRESOLVED_TASKS");
                    }
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
    
    /**
     * Increment startedMissions for all alliance members when mission becomes ACTIVE
     */
    private void incrementStartedMissionsForAlliance(String allianceId, Runnable onComplete) {
        Log.d("SpecialMissionService", "Incrementing startedMissions for alliance: " + allianceId);
        
        allianceRepository.getAlliance(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Alliance alliance = task.getResult().toObject(Alliance.class);
                if (alliance != null && alliance.getMemberIds() != null) {
                    List<String> memberIds = alliance.getMemberIds();
                    Log.d("SpecialMissionService", "Found " + memberIds.size() + " alliance members to update");

                    incrementStartedMissionsForAll(memberIds, updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("SpecialMissionService", "✅ Successfully incremented startedMissions for all alliance members");
                        } else {
                            Log.e("SpecialMissionService", "❌ Failed to increment startedMissions for alliance members", updateTask.getException());
                        }
                        onComplete.run();
                    });
                } else {
                    Log.e("SpecialMissionService", "Alliance not found or has no members");
                    onComplete.run();
                }
            } else {
                Log.e("SpecialMissionService", "Failed to get alliance for startedMissions increment");
            }
        });
    }

    public void checkExpiredMissions (OnCompleteListener < Boolean > listener) {
        Log.d("SpecialMissionService", "=== PROVERAVAM ISTEKLE MISIJE PRI POKRETANJU APLIKACIJE ===");

        // Dohvati sve aktivne misije
        specialMissionRepository.getAllActiveMissions(task -> {
            if (!task.isSuccessful()) {
                Log.e("SpecialMissionService", "Failed to get active missions");
                listener.onComplete(Tasks.forResult(false));
                return;
            }

            List<SpecialMission> activeMissions = new ArrayList<>();
            for (DocumentSnapshot doc : task.getResult()) {
                SpecialMission mission = doc.toObject(SpecialMission.class);
                if (mission != null && mission.getStatus() == SpecialMissionStatus.ACTIVE) {
                    activeMissions.add(mission);
                }
            }

            Log.d("SpecialMissionService", "Found " + activeMissions.size() + " active missions to check");

            if (activeMissions.isEmpty()) {
                listener.onComplete(Tasks.forResult(true));
                return;
            }

            // Proveri svaku aktivnu misiju
            final int[] completedChecks = {0};
            final int totalMissions = activeMissions.size();
            final boolean[] hasExpiredMissions = {false};

            for (SpecialMission mission : activeMissions) {
                long currentTime = System.currentTimeMillis();
                long endTime = mission.getEndTime();

                Log.d("SpecialMissionService", "Checking mission " + mission.getAllianceId() +
                        " - Current: " + currentTime + ", End: " + endTime +
                        ", Expired: " + (currentTime >= endTime));

                if (currentTime >= endTime) {
                    // Misija je istekla - završi je
                    Log.d("SpecialMissionService", "Mission " + mission.getAllianceId() + " has expired, completing...");
                    hasExpiredMissions[0] = true;

                    checkAndCompleteMission(mission.getAllianceId(), missionCheckTask -> {
                        completedChecks[0]++;
                        if (completedChecks[0] == totalMissions) {
                            Log.d("SpecialMissionService", "All mission checks completed. Had expired missions: " + hasExpiredMissions[0]);
                            listener.onComplete(Tasks.forResult(true));
                        }
                    });
                } else {
                    // Misija je još uvek aktivna - pokreni timer
                    Log.d("SpecialMissionService", "Mission " + mission.getAllianceId() + " is still active, starting timer...");
                    startMissionTimer(mission.getAllianceId(), mission.getEndTime());

                    completedChecks[0]++;
                    if (completedChecks[0] == totalMissions) {
                        Log.d("SpecialMissionService", "All mission checks completed. Had expired missions: " + hasExpiredMissions[0]);
                        listener.onComplete(Tasks.forResult(true));
                    }
                }
            }
        });
    }

    private void distributeBadges (String allianceId,int missionNumber, Runnable
    onComplete){
        Log.d("SpecialMissionService", "=== DISTRIBUTING BADGES ===");
        Log.d("SpecialMissionService", "Alliance ID: " + allianceId + ", Mission Number: " + missionNumber);

        // Dohvati sve taskove za ovu misiju
        specialTaskRepository.getSpecialTasksByAllianceId(allianceId, task -> {
            if (!task.isSuccessful()) {
                Log.e("SpecialMissionService", "Failed to get tasks for badge distribution");
                onComplete.run();
                return;
            }

            // Grupisi taskove po korisniku
            Map<String, List<SpecialTask>> userTasks = new HashMap<>();
            for (DocumentSnapshot doc : task.getResult()) {
                SpecialTask specialTask = doc.toObject(SpecialTask.class);
                if (specialTask != null && specialTask.getMissionNumber() == missionNumber) {
                    String userId = specialTask.getUserId();
                    if (!userTasks.containsKey(userId)) {
                        userTasks.put(userId, new ArrayList<>());
                    }
                    userTasks.get(userId).add(specialTask);
                }
            }

            Log.d("SpecialMissionService", "Found " + userTasks.size() + " users for badge distribution");

            // Dodeli bedževe svakom korisniku
            final int[] completedUsers = {0};
            final int totalUsers = userTasks.size();

            if (totalUsers == 0) {
                onComplete.run();
                return;
            }

            for (Map.Entry<String, List<SpecialTask>> entry : userTasks.entrySet()) {
                String userId = entry.getKey();
                List<SpecialTask> userTaskList = entry.getValue();

                // Broji koliko je taskova korisnik završio
                int completedTasks = 0;
                for (SpecialTask userTask : userTaskList) {
                    if (userTask.getStatus() == SpecialTaskStatus.COMPLETED) {
                        completedTasks++;
                    }
                }

                Log.d("SpecialMissionService", "User " + userId + " completed " + completedTasks + "/6 tasks");

                // Odredi koji bedž da dodeli
                String badgeToAdd = getBadgeForCompletedTasks(completedTasks);

                if (badgeToAdd != null) {
                    addBadgeToUser(userId, badgeToAdd, () -> {
                        completedUsers[0]++;
                        if (completedUsers[0] == totalUsers) {
                            Log.d("SpecialMissionService", "Badge distribution completed");
                            onComplete.run();
                        }
                    });
                } else {
                    Log.d("SpecialMissionService", "User " + userId + " gets no badge (completed: " + completedTasks + ")");
                    completedUsers[0]++;
                    if (completedUsers[0] == totalUsers) {
                        Log.d("SpecialMissionService", "Badge distribution completed");
                        onComplete.run();
                    }
                }
            }
        });
    }

    private String getBadgeForCompletedTasks ( int completedTasks){
        if (completedTasks < 2) {
            return null;
        } else if (completedTasks >= 2 && completedTasks <= 3) {
            return "bronze_badge";
        } else if (completedTasks >= 4 && completedTasks <= 5) {
            return "silver_badge";
        } else if (completedTasks == 6) {
            return "gold_badge";
        }
        return null;
    }

    private void addBadgeToUser (String userId, String badge, Runnable onComplete){
        Log.d("SpecialMissionService", "Adding badge " + badge + " to user " + userId);

        userRepository.getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    List<String> currentBadges = user.getBadges();
                    if (currentBadges == null) {
                        currentBadges = new ArrayList<>();
                    }

                    // Proveri da li korisnik već ima ovaj bedž
                    if (!currentBadges.contains(badge)) {
                        currentBadges.add(badge);
                        user.setBadges(currentBadges);

                        userRepository.updateUser(user, updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Log.d("SpecialMissionService", "Badge " + badge + " added to user " + userId);
                            } else {
                                Log.e("SpecialMissionService", "Failed to add badge to user " + userId);
                            }
                            onComplete.run();
                        });
                    } else {
                        Log.d("SpecialMissionService", "User " + userId + " already has badge " + badge);
                        onComplete.run();
                    }
                } else {
                    Log.e("SpecialMissionService", "User is null for ID: " + userId);
                    onComplete.run();
                }
            } else {
                Log.e("SpecialMissionService", "Failed to get user for ID: " + userId);
                onComplete.run();
            }
        });
    }


    private void incrementFinishedMissionsForAlliance (String allianceId, Runnable
    onComplete){
        Log.d("SpecialMissionService", "Incrementing finishedMissions for alliance: " + allianceId);

        allianceRepository.getAlliance(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Alliance alliance = task.getResult().toObject(Alliance.class);
                if (alliance != null && alliance.getMemberIds() != null) {
                    List<String> memberIds = alliance.getMemberIds();
                    Log.d("SpecialMissionService", "Found " + memberIds.size() + " alliance members to update");

                    incrementFinishedMissionsForAll(memberIds, updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Log.d("SpecialMissionService", "✅ Successfully incremented finishedMissions for all alliance members");
                        } else {
                            Log.e("SpecialMissionService", "❌ Failed to increment finishedMissions for alliance members", updateTask.getException());
                        }
                        onComplete.run();
                    });
                } else {
                    Log.e("SpecialMissionService", "Alliance not found or has no members");
                    onComplete.run();
                }
            } else {
                Log.e("SpecialMissionService", "Failed to get alliance for finishedMissions increment");
                onComplete.run();
            }
        });
    }

    private void distributeRewards (String allianceId, SpecialMission mission, Runnable
    onComplete){
        Log.d("SpecialMissionService", "=== DISTRIBUTING REWARDS ===");
        Log.d("SpecialMissionService", "Alliance ID: " + allianceId);

        // Dohvati sve taskove za ovu misiju da vidimo ko je učestvovao
        specialTaskRepository.getSpecialTasksByAllianceId(allianceId, task -> {
            if (!task.isSuccessful()) {
                Log.e("SpecialMissionService", "Failed to get tasks for reward distribution");
                onComplete.run();
                return;
            }

            // Grupiši taskove po korisnicima
            Map<String, List<SpecialTask>> userTasks = new HashMap<>();
            for (DocumentSnapshot doc : task.getResult()) {
                SpecialTask specialTask = doc.toObject(SpecialTask.class);
                if (specialTask != null && specialTask.getMissionNumber() == mission.getMissionNumber()) {
                    String userId = specialTask.getUserId();
                    if (!userTasks.containsKey(userId)) {
                        userTasks.put(userId, new ArrayList<>());
                    }
                    userTasks.get(userId).add(specialTask);
                }
            }

            Log.d("SpecialMissionService", "Found " + userTasks.size() + " users for reward distribution");

            if (userTasks.isEmpty()) {
                onComplete.run();
                return;
            }

            // Dodeli nagrade svakom korisniku
            final int[] completedUsers = {0};
            final int totalUsers = userTasks.size();

            for (Map.Entry<String, List<SpecialTask>> entry : userTasks.entrySet()) {
                String userId = entry.getKey();

                // Dodeli nagrade za ovog korisnika
                giveRewardsToUser(userId, () -> {
                    completedUsers[0]++;
                    if (completedUsers[0] == totalUsers) {
                        Log.d("SpecialMissionService", "Reward distribution completed");

                        // Postavi rewardsDistributed na true
                        mission.setRewardsDistributed(true);
                        specialMissionRepository.updateSpecialMission(mission, updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Log.d("SpecialMissionService", "✅ rewardsDistributed set to true");
                            } else {
                                Log.e("SpecialMissionService", "❌ Failed to update rewardsDistributed");
                            }
                            onComplete.run();
                        });
                    }
                });
            }
        });
    }

    /**
     * Dodeli nagrade jednom korisniku
     */
    private void giveRewardsToUser (String userId, Runnable onComplete){
        Log.d("SpecialMissionService", "Giving rewards to user: " + userId);

        // Dohvati korisnika
        userRepository.getUser(userId, userTask -> {
            if (!userTask.isSuccessful() || userTask.getResult() == null) {
                Log.e("SpecialMissionService", "Failed to get user for rewards: " + userId);
                onComplete.run();
                return;
            }

            User user = userTask.getResult().toObject(User.class);
            if (user == null) {
                Log.e("SpecialMissionService", "User is null for ID: " + userId);
                onComplete.run();
                return;
            }

            // Dohvati običnog boss-a korisnika i uzmi 50% od njegovog coinsDrop
            bossRepository.getBossByUserId(userId, bossTask -> {
                if (bossTask.isSuccessful() && bossTask.getResult() != null && bossTask.getResult().exists()) {
                    Boss userBoss = bossTask.getResult().toObject(Boss.class);
                    if (userBoss != null) {
                        int coinsReward = userBoss.getCoinsDrop() / 2;
                        user.setCoins(user.getCoins() + coinsReward);

                        Log.d("SpecialMissionService", "User " + userId + " gets " + coinsReward + " coins from boss " + userBoss.getCoinsDrop());
                    } else {
                        Log.e("SpecialMissionService", "Boss is null for user: " + userId);
                    }
                } else {
                    Log.e("SpecialMissionService", "Failed to get boss for user: " + userId);
                    // Ako nema boss-a, dodeli 0 coins
                }

                // Nastavi sa dodelom opreme
                giveRandomEquipmentToUser(user, EquipmentType.POTION, () -> {
                    giveRandomEquipmentToUser(user, EquipmentType.CLOTHES, () -> {
                        // Ažuriraj korisnika u bazi
                        userRepository.updateUser(user, updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Log.d("SpecialMissionService", "User " + userId + " rewards updated successfully");
                            } else {
                                Log.e("SpecialMissionService", "Failed to update user rewards: " + userId);
                            }
                            onComplete.run();
                        });
                    });
                });
            });
        });
    }

    /**
     * Dodeli random opremu korisniku
     */
    private void giveRandomEquipmentToUser (User user, EquipmentType type, Runnable
    onComplete){
        Log.d("SpecialMissionService", "Giving random " + type + " to user: " + user.getId());

        // Dohvati sve opreme određenog tipa
        equipmentRepository.getAllEquipment(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Log.e("SpecialMissionService", "Failed to get equipment for type: " + type);
                onComplete.run();
                return;
            }

            List<Equipment> allEquipment = task.getResult();
            List<Equipment> filteredEquipment = new ArrayList<>();

            // Filtriraj po tipu
            for (Equipment equipment : allEquipment) {
                if (equipment.getType() == type) {
                    filteredEquipment.add(equipment);
                }
            }

            if (filteredEquipment.isEmpty()) {
                Log.e("SpecialMissionService", "No equipment found for type: " + type);
                onComplete.run();
                return;
            }

            // Izaberi random opremu
            Equipment randomEquipment = filteredEquipment.get((int) (Math.random() * filteredEquipment.size()));

            // Kreiraj MyEquipment
            MyEquipment myEquipment = new MyEquipment();
            myEquipment.setId(java.util.UUID.randomUUID().toString());
            myEquipment.setEquipmentId(randomEquipment.getId());
            myEquipment.setLeftAmount(randomEquipment.getLasting());
            myEquipment.setTimesUpgraded(0);
            myEquipment.setActivated(false);

            // Dodaj u korisnikovu opremu
            List<MyEquipment> userEquipment = user.getEquipment();
            if (userEquipment == null) {
                userEquipment = new ArrayList<>();
            }
            userEquipment.add(myEquipment);
            user.setEquipment(userEquipment);

            Log.d("SpecialMissionService", "User " + user.getId() + " got equipment: " + randomEquipment.getName());
            onComplete.run();
        });
    }
    
    /**
     * Increment startedMissions for multiple users (alliance members)
     */
    private void incrementStartedMissionsForAll(List<String> userIds, OnCompleteListener<Void> listener) {
        if (userIds == null || userIds.isEmpty()) {
            listener.onComplete(Tasks.forResult(null));
            return;
        }
        
        Log.d("SpecialMissionService", "Incrementing startedMissions for " + userIds.size() + " users");
        
        // Use AtomicInteger to track completed operations
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(userIds.size());
        boolean[] hasError = {false};
        
        for (String userId : userIds) {
            incrementStartedMissions(userId, task -> {
                if (!task.isSuccessful()) {
                    Log.e("SpecialMissionService", "Failed to increment startedMissions for user " + userId, task.getException());
                    hasError[0] = true;
                }
                
                int completed = completedCount.incrementAndGet();
                if (completed == totalCount.get()) {
                    if (hasError[0]) {
                        listener.onComplete(Tasks.forException(new Exception("Some users failed to update")));
                    } else {
                        Log.d("SpecialMissionService", "Successfully incremented startedMissions for all " + userIds.size() + " users");
                        listener.onComplete(Tasks.forResult(null));
                    }
                }
            });
        }
    }
    
    /**
     * Increment finishedMissions for multiple users (alliance members)
     */
    private void incrementFinishedMissionsForAll(List<String> userIds, OnCompleteListener<Void> listener) {
        if (userIds == null || userIds.isEmpty()) {
            listener.onComplete(Tasks.forResult(null));
            return;
        }
        
        Log.d("SpecialMissionService", "Incrementing finishedMissions for " + userIds.size() + " users");
        
        // Use AtomicInteger to track completed operations
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(userIds.size());
        boolean[] hasError = {false};
        
        for (String userId : userIds) {
            incrementFinishedMissions(userId, task -> {
                if (!task.isSuccessful()) {
                    Log.e("SpecialMissionService", "Failed to increment finishedMissions for user " + userId, task.getException());
                    hasError[0] = true;
                }
                
                int completed = completedCount.incrementAndGet();
                if (completed == totalCount.get()) {
                    if (hasError[0]) {
                        listener.onComplete(Tasks.forException(new Exception("Some users failed to update")));
                    } else {
                        Log.d("SpecialMissionService", "Successfully incremented finishedMissions for all " + userIds.size() + " users");
                        listener.onComplete(Tasks.forResult(null));
                    }
                }
            });
        }
    }
    
    /**
     * Increment startedMissions count for a user
     */
    private void incrementStartedMissions(String userId, OnCompleteListener<Void> listener) {
        userRepository.getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    int currentStartedMissions = user.getStartedMissions();
                    user.setStartedMissions(currentStartedMissions + 1);
                    userRepository.updateUser(user, listener);
                    Log.d("SpecialMissionService", "Incremented startedMissions for user " + userId + " to " + user.getStartedMissions());
                } else {
                    listener.onComplete(Tasks.forException(new Exception("User not found")));
                }
            } else {
                listener.onComplete(Tasks.forException(new Exception("Failed to get user")));
            }
        });
    }
    
    /**
     * Increment finishedMissions count for a user
     */
    private void incrementFinishedMissions(String userId, OnCompleteListener<Void> listener) {
        userRepository.getUser(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    int currentFinishedMissions = user.getFinishedMissions();
                    user.setFinishedMissions(currentFinishedMissions + 1);
                    userRepository.updateUser(user, listener);
                    Log.d("SpecialMissionService", "Incremented finishedMissions for user " + userId + " to " + user.getFinishedMissions());
                } else {
                    listener.onComplete(Tasks.forException(new Exception("User not found")));
                }
            } else {
                listener.onComplete(Tasks.forException(new Exception("Failed to get user")));
            }
        });
    }

}
