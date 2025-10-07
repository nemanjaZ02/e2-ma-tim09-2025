package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.enums.EquipmentType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EquipmentRepository {
    private final FirebaseFirestore db;
    private final CollectionReference equipmentRef;

    @Inject
    public EquipmentRepository(FirebaseFirestore firestore) {
        this.db = firestore;
        this.equipmentRef = db.collection("equipment");
    }

    /**
     * Get equipment by ID (searches by id field, not document ID)
     */
    public void getEquipment(String equipmentId, OnCompleteListener<Equipment> listener) {
        // First try the old way (document ID lookup)
        equipmentRef.document(equipmentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Equipment equipment = task.getResult().toObject(Equipment.class);
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(equipment));
                    } else {
                        // Fallback: search by id field using query
                        equipmentRef.whereEqualTo("id", equipmentId)
                                .get()
                                .addOnCompleteListener(queryTask -> {
                                    if (queryTask.isSuccessful()) {
                                        QuerySnapshot result = queryTask.getResult();
                                        if (result != null && !result.isEmpty()) {
                                            for (DocumentSnapshot doc : result.getDocuments()) {
                                                Equipment equipment = doc.toObject(Equipment.class);
                                                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(equipment));
                                                return;
                                            }
                                        }
                                    }
                                    // If both methods fail
                                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                                        new Exception("Equipment not found")));
                                });
                    }
                });
    }

    @FunctionalInterface
    public interface EquipmentCallback {
        void onComplete(Equipment e);
    }


    public void getEquipmentCallback(String equipmentId, EquipmentCallback callback) {
        // Try fetching by document ID first
        equipmentRef.document(equipmentId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Equipment equipment = document.toObject(Equipment.class);
                        callback.onComplete(equipment);
                    } else {
                        // Fallback: query by 'id' field if not found by document ID
                        equipmentRef.whereEqualTo("id", equipmentId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        Equipment equipment = querySnapshot
                                                .getDocuments()
                                                .get(0)
                                                .toObject(Equipment.class);
                                        callback.onComplete(equipment);
                                    } else {
                                        callback.onComplete(null);
                                    }
                                });
                    }
                });
    }


    /**
     * Get all equipment
     */
    public void getAllEquipment(OnCompleteListener<List<Equipment>> listener) {
        equipmentRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Equipment> equipmentList = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Equipment equipment = doc.toObject(Equipment.class);
                            if (equipment != null) {
                                equipmentList.add(equipment);
                            }
                        }
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(equipmentList));
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            task.getException() != null ? task.getException() : new Exception("Failed to fetch equipment")));
                    }
                });
    }
}