package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MyEquipmentRepository {
    private final FirebaseFirestore db;
    private final CollectionReference usersRef;

    @Inject
    public MyEquipmentRepository(FirebaseFirestore firestore) {
        this.db = firestore;
        this.usersRef = db.collection("users");
    }

    /**
     * Get all equipment owned by a user
     */
    public void getUserEquipments(String userId, OnCompleteListener<List<MyEquipment>> listener) {
        usersRef.document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            List<MyEquipment> equipment = user.getEquipment();
                            if (equipment == null) {
                                equipment = new ArrayList<>();
                            }
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(equipment));
                        } else {
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                new Exception("Failed to parse user data")));
                        }
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            task.getException() != null ? task.getException() : new Exception("User not found")));
                    }
                });
    }

    /**
     * Get specific equipment owned by user
     */
    public void getUserEquipmentByEquipmentId(String userId, String equipmentId, OnCompleteListener<MyEquipment> listener) {
        usersRef.document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null && user.getEquipment() != null) {
                            MyEquipment foundEquipment = null;
                            for (MyEquipment equipment : user.getEquipment()) {
                                if (equipmentId.equals(equipment.getEquipmentId())) {
                                    foundEquipment = equipment;
                                    break;
                                }
                            }
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(foundEquipment));
                        } else {
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                        }
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            task.getException() != null ? task.getException() : new Exception("User not found")));
                    }
                });
    }
}