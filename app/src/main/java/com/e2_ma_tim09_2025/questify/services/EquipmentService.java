package com.e2_ma_tim09_2025.questify.services;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.models.enums.EquipmentType;
import com.e2_ma_tim09_2025.questify.repositories.EquipmentRepository;
import com.e2_ma_tim09_2025.questify.repositories.MyEquipmentRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final UserService userService;
    private final MyEquipmentRepository myEquipmentRepository;
    private final UserRepository userRepository;
    private final BossService bossService;

    @Inject
    public EquipmentService(EquipmentRepository equipmentRepository, MyEquipmentRepository myEquipmentRepository,
                            UserService userService, UserRepository userRepository, BossService bossService) {
        this.equipmentRepository = equipmentRepository;
        this.myEquipmentRepository = myEquipmentRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.bossService = bossService;
    }

    /**
     * Activate user's equipment
     * Business logic: Check if user owns equipment, equipment not exhausted, set isActivated to true
     * Note: LeftAmount reduction is handled in a separate method
     */
    public void activateEquipment(String userId, String equipmentId, OnCompleteListener<Boolean> listener) {
        // First check if user owns this equipment
        myEquipmentRepository.getUserEquipmentByEquipmentId(userId, equipmentId, myEquipmentTask -> {
            if (!myEquipmentTask.isSuccessful()) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(myEquipmentTask.getException()));
                return;
            }

            MyEquipment myEquipment = myEquipmentTask.getResult();
            if (myEquipment == null) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                        new Exception("Equipment not found in user inventory")));
                return;
            }

            // Check if equipment still has uses left
            if (myEquipment.getLeftAmount() == 0) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                        new Exception("Equipment is exhausted, no uses left")));
                return;
            }

            // Get equipment master data to check if it's activatable
            equipmentRepository.getEquipment(equipmentId, equipmentTask -> {
                if (!equipmentTask.isSuccessful()) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(equipmentTask.getException()));
                    return;
                }

                Equipment equipment = equipmentTask.getResult();
                if (equipment == null) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            new Exception("Equipment master data not found")));
                    return;
                }

                myEquipment.setActivated(true);

                // Get user and update their equipment
                userService.getUser(userId, userTask -> {
                    if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().exists()) {
                        User user = userTask.getResult().toObject(User.class);
                        if (user != null && user.getEquipment() != null) {
                            // Find and update the equipment in user's list
                            boolean updated = false;
                            for (int i = 0; i < user.getEquipment().size(); i++) {
                                if (user.getEquipment().get(i).getId().equals(myEquipment.getId())) {
                                    user.getEquipment().set(i, myEquipment);
                                    updated = true;
                                    break;
                                }
                            }

                            if (updated) {
                                // Save updated user
                                userService.updateUser(user, updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(true));
                                    } else {
                                        // Rollback activation on failure
                                        myEquipment.setActivated(false);
                                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                                new Exception("Failed to save equipment update")));
                                    }
                                });
                            } else {
                                // Rollback activation on failure
                                myEquipment.setActivated(false);
                                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                        new Exception("Equipment not found in user inventory")));
                            }
                        } else {
                            // Rollback activation on failure
                            myEquipment.setActivated(false);
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                    new Exception("Failed to parse user data")));
                        }
                    } else {
                        // Rollback activation on failure
                        myEquipment.setActivated(false);
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                userTask.getException() != null ? userTask.getException() : new Exception("User not found")));
                    }
                });
            });
        });
    }

    /**
     * Get user's equipment list
     * Business logic: Provides user's equipment from repository
     */
    public void getUserEquipment(String userId, OnCompleteListener<List<MyEquipment>> listener) {
        userRepository.getUserEquipment(userId, listener);
    }

    /**
     * Get specific equipment item from user's inventory
     * Business logic: Provides single equipment item for user
     */
    public void getUserEquipmentById(String userId, String equipmentId, OnCompleteListener<MyEquipment> listener) {
        userRepository.getUserEquipmentById(userId, equipmentId, listener);
    }

    /**
     * Get activated equipment for user
     * Business logic: Provides only activated equipment items with business validation
     */
    public void getUserActivatedEquipment(String userId, OnCompleteListener<List<MyEquipment>> listener) {
        userRepository.getUserActivatedEquipment(userId, equipmentTask -> {
            if (equipmentTask.isSuccessful()) {
                List<MyEquipment> activatedEquipment = equipmentTask.getResult();
                if (activatedEquipment == null) {
                    activatedEquipment = new ArrayList<>();
                }

                // Business logic: Filter out exhausted equipment from activated list
                List<MyEquipment> availableActivatedEquipment = new ArrayList<>();
                for (MyEquipment equipment : activatedEquipment) {
                    if (equipment.getLeftAmount() > 0) { // Only include equipment with uses left
                        availableActivatedEquipment.add(equipment);
                    }
                }

                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(availableActivatedEquipment));
            } else {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                        equipmentTask.getException() != null ? equipmentTask.getException() : new Exception("Failed to fetch activated equipment")));
            }
        });
    }

    /**
     * Buy equipment for user
     * Business logic: Reduce user's PP, add equipment to inventory, create MyEquipment instance
     */
    public void buyEquipment(String userId, String equipmentId, OnCompleteListener<Boolean> listener) {
        // First get the equipment master data and user data sequentially
        equipmentRepository.getEquipment(equipmentId, equipmentTask -> {
            if (!equipmentTask.isSuccessful()) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                        equipmentTask.getException() != null ? equipmentTask.getException() : new Exception("Failed to fetch equipment")));
                return;
            }

            Equipment equipment = equipmentTask.getResult();
//            if (equipment == null) {
//                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
//                        new Exception("Equipment not found")));
//                return;
//            }

            // Now get user data
            userService.getUser(userId, userTask -> {
                if (!userTask.isSuccessful()) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            userTask.getException() != null ? userTask.getException() : new Exception("Failed to fetch user")));
                    return;
                }

                com.google.firebase.firestore.DocumentSnapshot userDoc = userTask.getResult();
                if (userDoc == null || !userDoc.exists()) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            new Exception("User not found")));
                    return;
                }

                User user = userDoc.toObject(User.class);
                if (user == null) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            new Exception("Failed to parse user data")));
                    return;
                }

                // Business logic validations
                double equipmentPrice = equipment.getPrice();
                int userCoins = user.getCoins();

                // Check if user has enough coins
                if (userCoins < (int) equipmentPrice) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            new Exception("Insufficient Coins. Required: " + (int) equipmentPrice + ", Available: " + userCoins)));
                    return;
                }

                // Create new MyEquipment instance
                String myEquipmentId = UUID.randomUUID().toString();
                MyEquipment newMyEquipment = new MyEquipment(
                        myEquipmentId,
                        equipmentId,
                        0, // timesUpgraded = 0
                        equipment.getLasting() // leftAmount = equipment lasting
                );
                newMyEquipment.setActivated(false); // Initially not activated

                // Update user: reduce coins and add equipment
                user.setCoins(userCoins - (int) equipmentPrice);

                if (user.getEquipment() == null) {
                    user.setEquipment(new ArrayList<>());
                }
                user.getEquipment().add(newMyEquipment);

                // Save updated user
                userService.updateUser(user, updateTask -> {
                    if (updateTask.isSuccessful()) {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(true));
                    } else {
                        // Rollback coins on failure
                        user.setCoins(userCoins);
                        user.getEquipment().remove(newMyEquipment);
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                new Exception("Failed to save purchase: " +
                                        (updateTask.getException() != null ? updateTask.getException().getMessage() : "Unknown error"))));
                    }
                });
            });
        });
    }

    public void getEquipmentPrice(String userId, String equipmentId, OnCompleteListener<Double> listener) {
        // Step 1: Get user to determine current level
        userService.getUser(userId, userTask -> {
            if (!userTask.isSuccessful()) {
                listener.onComplete(Tasks.forException(userTask.getException()));
                return;
            }

            User user = userTask.getResult().toObject(User.class);
            if (user == null) {
                listener.onComplete(Tasks.forException(new Exception("User not found")));
                return;
            }

            // Step 2: Get equipment by ID
            equipmentRepository.getEquipment(equipmentId, equipmentTask -> {
                if (!equipmentTask.isSuccessful()) {
                    listener.onComplete(Tasks.forException(equipmentTask.getException()));
                    return;
                }

                Equipment equipment = equipmentTask.getResult();
//                if (equipment == null) {
//                    listener.onComplete(Tasks.forException(new Exception("Equipment not found")));
//                    return;
//                }

                // Step 3: Get boss data asynchronously
                bossService.getBossByUser(userId, bossTask -> {
                    if (!bossTask.isSuccessful()) {
                        listener.onComplete(Tasks.forException(bossTask.getException()));
                        return;
                    }
                    
                    Boss userBoss = bossTask.getResult();
                    if (userBoss == null) {
                        // If no boss found for this user, cannot purchase
                        listener.onComplete(Tasks.forException(new Exception("No boss found for user - cannot purchase equipment")));
                        return;
                    }

                    // Step 4: Calculate price
                    double price = calculateEquipmentPrice(user, equipment, userBoss);
                    listener.onComplete(Tasks.forResult(price));
                });
            });
        });
    }
    private double calculateEquipmentPrice(User user, Equipment equipment, Boss boss) {
        // Calculate previous level boss coins (keeping your original logic)
        double previousLevelBossCoins = boss.getStatus().equals(BossStatus.DEFEATED) ? boss.getCoinsDrop() / 1.2 : boss.getCoinsDrop();

        // Calculate equipment price
        double price = equipment.getPrice() * previousLevelBossCoins;

        return price;
    }

    /**
     * Get all equipment from the database
     */
    public void getAllEquipment(OnCompleteListener<List<Equipment>> listener) {
        equipmentRepository.getAllEquipment(task -> {
            if (task.isSuccessful()) {
                listener.onComplete(Tasks.forResult(task.getResult()));
            } else {
                listener.onComplete(Tasks.forException(task.getException()));
            }
        });
    }

    /**
     * Get user's current coin balance
     */
    public void getUserCoins(String userId, OnCompleteListener<Integer> listener) {
        userService.getUser(userId, task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                if (user != null) {
                    listener.onComplete(Tasks.forResult(user.getCoins()));
                } else {
                    listener.onComplete(Tasks.forResult(0));
                }
            } else {
                listener.onComplete(Tasks.forResult(0));
            }
        });
    }

    /**
     * Purchase equipment for user
     */
    public void purchaseEquipment(String userId, String equipmentId, int price, OnCompleteListener<Void> listener) {
        // First get the user and equipment data
        userService.getUser(userId, userTask -> {
            if (!userTask.isSuccessful()) {
                listener.onComplete(Tasks.forException(userTask.getException()));
                return;
            }

            User user = userTask.getResult().toObject(User.class);
            if (user == null) {
                listener.onComplete(Tasks.forException(new Exception("User not found")));
                return;
            }

            // Check if user has enough coins
            if (user.getCoins() < price) {
                listener.onComplete(Tasks.forException(new Exception("Not enough coins")));
                return;
            }

            // Get the equipment details
            equipmentRepository.getEquipment(equipmentId, equipmentTask -> {
                if (!equipmentTask.isSuccessful()) {
                    listener.onComplete(Tasks.forException(equipmentTask.getException()));
                    return;
                }

               Equipment equipment = equipmentTask.getResult();
//                if (equipment == null) {
//                    listener.onComplete(Tasks.forException(new Exception("Equipment not found")));
//                    return;
//                }

                // Create MyEquipment object
                MyEquipment myEquipment = new MyEquipment();
                myEquipment.setId(userId + "_" + equipmentId + "_" + System.currentTimeMillis()); // Unique ID
                myEquipment.setEquipmentId(equipmentId);
                myEquipment.setLeftAmount(equipment.getLasting());
                myEquipment.setTimesUpgraded(0);
                myEquipment.setActivated(false);

                // Update user: add equipment and reduce coins
                user.getEquipment().add(myEquipment);
                user.setCoins(user.getCoins() - price);

                // Save updated user
                userService.updateUser(user, updateTask -> {
                    if (updateTask.isSuccessful()) {
                        listener.onComplete(Tasks.forResult(null));
                    } else {
                        listener.onComplete(Tasks.forException(updateTask.getException()));
                    }
                });
            });
        });
    }

    /**
     * Get random equipment by type
     * Business logic: Returns random equipment of specified type
     */
    public void getRandomEquipmentByType(EquipmentType type, OnCompleteListener<Equipment> listener) {
        equipmentRepository.getAllEquipment(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Equipment> allEquipment = task.getResult();
                List<Equipment> filteredEquipment = new ArrayList<>();
                
                for (Equipment equipment : allEquipment) {
                    if (equipment.getType() == type) {
                        filteredEquipment.add(equipment);
                    }
                }
                
                if (!filteredEquipment.isEmpty()) {
                    // Return random equipment
                    Equipment randomEquipment = filteredEquipment.get((int) (Math.random() * filteredEquipment.size()));
                    listener.onComplete(Tasks.forResult(randomEquipment));
                } else {
                    listener.onComplete(Tasks.forException(new Exception("No equipment of type " + type + " found")));
                }
            } else {
                listener.onComplete(Tasks.forException(new Exception("Failed to get equipment")));
            }
        });
    }

}




