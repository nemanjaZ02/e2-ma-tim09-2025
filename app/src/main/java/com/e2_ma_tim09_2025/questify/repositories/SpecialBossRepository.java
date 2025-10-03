package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.SpecialBoss;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecialBossRepository {
    private final FirebaseFirestore db;
    private final CollectionReference specialBossesRef;

    @Inject
    public SpecialBossRepository(FirebaseFirestore firestore) {
        this.db = firestore;
        this.specialBossesRef = db.collection("specialBosses");
    }

    public void createSpecialBoss(SpecialBoss specialBoss, OnCompleteListener<Void> listener) {
        specialBossesRef.document(specialBoss.getId())
                .set(specialBoss)
                .addOnCompleteListener(listener);
    }

    public void getSpecialBossById(String bossId, OnCompleteListener<DocumentSnapshot> listener) {
        specialBossesRef.document(bossId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getSpecialBossByAllianceId(String allianceId, OnCompleteListener<DocumentSnapshot> listener) {
        specialBossesRef.whereEqualTo("allianceId", allianceId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(task.getResult().getDocuments().get(0)));
                    } else {
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                    }
                });
    }

    public void updateSpecialBoss(SpecialBoss specialBoss, OnCompleteListener<Void> listener) {
        specialBossesRef.document(specialBoss.getId())
                .set(specialBoss)
                .addOnCompleteListener(listener);
    }

    public ListenerRegistration listenSpecialBossByAllianceId(String allianceId, 
            EventListener<DocumentSnapshot> listener) {
        return specialBossesRef.whereEqualTo("allianceId", allianceId)
                .limit(1)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        listener.onEvent(null, error);
                        return;
                    }
                    
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        listener.onEvent(querySnapshot.getDocuments().get(0), null);
                    } else {
                        listener.onEvent(null, null);
                    }
                });
    }

    public void deleteSpecialBoss(String bossId, OnCompleteListener<Void> listener) {
        specialBossesRef.document(bossId)
                .delete()
                .addOnCompleteListener(listener);
    }
}
