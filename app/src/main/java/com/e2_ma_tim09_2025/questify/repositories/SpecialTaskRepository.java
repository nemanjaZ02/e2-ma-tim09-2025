package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.SpecialTask;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialTaskType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecialTaskRepository {
    private final FirebaseFirestore db;
    private final CollectionReference specialTasksRef;

    @Inject
    public SpecialTaskRepository(FirebaseFirestore firestore) {
        this.db = firestore;
        this.specialTasksRef = db.collection("specialTasks");
    }

    public void createSpecialTask(SpecialTask specialTask, OnCompleteListener<Void> listener) {
        specialTasksRef.document(specialTask.getId())
                .set(specialTask)
                .addOnCompleteListener(listener);
    }

    public void createSpecialTasksForAlliance(List<SpecialTask> specialTasks, OnCompleteListener<Void> listener) {
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        
        for (SpecialTask task : specialTasks) {
            batch.set(specialTasksRef.document(task.getId()), task);
        }
        
        batch.commit().addOnCompleteListener(listener);
    }

    public void getSpecialTaskById(String taskId, OnCompleteListener<DocumentSnapshot> listener) {
        specialTasksRef.document(taskId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getSpecialTasksByAllianceId(String allianceId, OnCompleteListener<QuerySnapshot> listener) {
        specialTasksRef.whereEqualTo("allianceId", allianceId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getSpecialTasksByUserId(String userId, OnCompleteListener<QuerySnapshot> listener) {
        specialTasksRef.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getSpecialTasksByAllianceAndUser(String allianceId, String userId, OnCompleteListener<QuerySnapshot> listener) {
        specialTasksRef.whereEqualTo("allianceId", allianceId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getSpecialTasksByAllianceAndType(String allianceId, SpecialTaskType taskType, OnCompleteListener<QuerySnapshot> listener) {
        specialTasksRef.whereEqualTo("allianceId", allianceId)
                .whereEqualTo("taskType", taskType)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateSpecialTask(SpecialTask specialTask, OnCompleteListener<Void> listener) {
        specialTasksRef.document(specialTask.getId())
                .set(specialTask)
                .addOnCompleteListener(listener);
    }

    public void deleteSpecialTask(String taskId, OnCompleteListener<Void> listener) {
        specialTasksRef.document(taskId)
                .delete()
                .addOnCompleteListener(listener);
    }

    public void deleteSpecialTasksByAllianceId(String allianceId, OnCompleteListener<Void> listener) {
        specialTasksRef.whereEqualTo("allianceId", allianceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        for (DocumentSnapshot doc : task.getResult()) {
                            batch.delete(specialTasksRef.document(doc.getId()));
                        }
                        batch.commit().addOnCompleteListener(listener);
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(task.getException()));
                    }
                });
    }

    public void listenToSpecialTasksByUserId(String userId, String allianceId, OnCompleteListener<List<SpecialTask>> listener) {
        specialTasksRef.whereEqualTo("userId", userId)
                .whereEqualTo("allianceId", allianceId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(error));
                        return;
                    }

                    if (querySnapshot != null) {
                        List<SpecialTask> tasks = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            SpecialTask task = doc.toObject(SpecialTask.class);
                            if (task != null) {
                                tasks.add(task);
                            }
                        }
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(tasks));
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(new ArrayList<>()));
                    }
                });
    }
}
