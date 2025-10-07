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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

                // Get calculated price for this user
                getEquipmentPrice(userId, equipmentId, priceTask -> {
                    if (!priceTask.isSuccessful()) {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                priceTask.getException() != null ? priceTask.getException() : new Exception("Failed to calculate equipment price")));
                        return;
                    }

                    double calculatedPrice = priceTask.getResult();
                    int userCoins = user.getCoins();
                    int priceToPay = (int) calculatedPrice;

                    // Check if user has enough coins using calculated price
                    if (userCoins < priceToPay) {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                new Exception("Insufficient Coins. Required: " + priceToPay + ", Available: " + userCoins)));
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

                    // Update user: reduce coins and add equipment using calculated price
                    user.setCoins(userCoins - priceToPay);

                    if (user.getEquipment() == null) {
                        user.setEquipment(new ArrayList<>());
                    }
                    user.getEquipment().add(newMyEquipment);

                    // Save updated user
                    userService.updateUser(user, updateTask -> {
                        if (updateTask.isSuccessful()) {
                            System.out.println("Purchase successful: User " + userId + " bought " + equipment.getName() + " for " + priceToPay + " coins");
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
     * Get equipment by ID
     */
    public void getEquipment(String equipmentId, OnCompleteListener<Equipment> listener) {
        equipmentRepository.getEquipment(equipmentId, listener);
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
     * Recalculate user stats based on active equipment
     * This method handles all calculations and updates user stats
     */
    @FunctionalInterface
    public interface BonusesCallback {
        void onComplete(List<Double> bonuses);
    }
    public void recalculateUserStatsWithActiveEquipment(String userId, BonusesCallback callback) {
        // Step 1: Get user's activated equipment
        getUserActivatedEquipment(userId, activatedEquipmentTask -> {
            if (!activatedEquipmentTask.isSuccessful() || activatedEquipmentTask.getResult() == null) {
                callback.onComplete(null);
                return;
            }

            List<MyEquipment> activatedEquipment = activatedEquipmentTask.getResult();

            if (activatedEquipment.isEmpty()) {
                // No activated equipment → return zero bonuses
                callback.onComplete(Arrays.asList(0.0, 0.0, 0.0, 0.0));
                return;
            }

            // Step 2: Initialize counters
            AtomicInteger loadedCount = new AtomicInteger(0);
            int totalItems = activatedEquipment.size();

            AtomicReference<Double> totalPPBonus = new AtomicReference<>(0.0);
            AtomicReference<Double> totalSuccessBonus = new AtomicReference<>(0.0);
            AtomicReference<Double> totalNumberOfAttacksBonus = new AtomicReference<>(0.0);
            AtomicReference<Double> totalMoneyBonus = new AtomicReference<>(0.0);

            // Step 3: Fetch each equipment and sum bonuses
            for (MyEquipment myEquipment : activatedEquipment) {
                equipmentRepository.getEquipmentCallback(myEquipment.getEquipmentId(), eq -> {
                        if (eq != null && eq.getRefersTo() != null) {
                            double amount = eq.getReferingAmount(); // assuming this returns double
                            switch (eq.getRefersTo()) {
                                case "PP":
                                    totalPPBonus.updateAndGet(v -> v + amount);
                                    break;
                                case "success":
                                    totalSuccessBonus.updateAndGet(v -> v + amount);
                                    break;
                                case "numberOfAttacks":
                                    totalNumberOfAttacksBonus.updateAndGet(v -> v + amount);
                                    break;
                                case "money":
                                    totalMoneyBonus.updateAndGet(v -> v + amount);
                                    break;
                            }
                        }

                        // Check if all async calls finished
                        if (loadedCount.incrementAndGet() == totalItems) {
                            callback.onComplete(Arrays.asList(
                                    totalPPBonus.get(),
                                    totalSuccessBonus.get(),
                                    totalNumberOfAttacksBonus.get(),
                                    totalMoneyBonus.get()
                            ));
                        }

                });
            }
        });
    }

    /**
     * Damage user's activated equipment after boss fight
     * Decrements leftAmount for equipment with 1-2 uses, removes if becomes 0
     */
    public void damageEquipment(String userId, OnCompleteListener<Void> listener) {
        System.out.println("DEBUG: damageEquipment called for userId: " + userId);
        
        // Get user data
        userService.getUser(userId, userTask -> {
            if (!userTask.isSuccessful() || userTask.getResult() == null) {
                System.out.println("DEBUG: Failed to get user data");
                listener.onComplete(Tasks.forException(
                    userTask.getException() != null ? 
                    userTask.getException() : 
                    new Exception("Failed to get user data")));
                return;
            }

            User user = userTask.getResult().toObject(User.class);
            if (user == null || user.getEquipment() == null) {
                System.out.println("DEBUG: User or equipment list is null");
                listener.onComplete(Tasks.forException(new Exception("User or equipment list not found")));
                return;
            }

            System.out.println("DEBUG: User has " + user.getEquipment().size() + " equipment items");
            
            // Go through all active equipment for this user
            List<MyEquipment> equipmentToRemove = new ArrayList<>();
            boolean equipmentUpdated = false;

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

            // Remove exhausted equipment
            if (!equipmentToRemove.isEmpty()) {
                System.out.println("DEBUG: Removing " + equipmentToRemove.size() + " exhausted equipment items");
                user.getEquipment().removeAll(equipmentToRemove);
                equipmentUpdated = true;
            }

            // Update user if any changes were made
            if (equipmentUpdated) {
                System.out.println("DEBUG: Updating user with modified equipment");
                userService.updateUser(user, updateTask -> {
                    if (updateTask.isSuccessful()) {
                        System.out.println("DEBUG: Equipment damage completed successfully");
                        listener.onComplete(Tasks.forResult(null));
                    } else {
                        System.out.println("DEBUG: Failed to update user equipment");
                        listener.onComplete(Tasks.forException(
                            updateTask.getException() != null ? 
                            updateTask.getException() : 
                            new Exception("Failed to update user equipment")));
                    }
                });
            } else {
                System.out.println("DEBUG: No equipment changes made");
                listener.onComplete(Tasks.forResult(null));
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




