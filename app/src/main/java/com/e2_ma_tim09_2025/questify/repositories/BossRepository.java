package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.Boss;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import javax.inject.Inject;

public class BossRepository {
    private final FirebaseFirestore db;
    private final CollectionReference bossesRef;

    @Inject
    public BossRepository() {
        db = FirebaseFirestore.getInstance();
        bossesRef = db.collection("bosses");
    }

    public void createBoss(Boss boss, OnCompleteListener<Void> listener) {
        bossesRef.document(boss.getUserId())
                .set(boss)
                .addOnCompleteListener(listener);
    }

    public void getBossByUserId(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        bossesRef.document(userId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateBoss(Boss boss, OnCompleteListener<Void> listener) {
        bossesRef.document(boss.getUserId())
                .set(boss)
                .addOnCompleteListener(listener);
    }

    public ListenerRegistration listenBossByUserId(String userId, EventListener<DocumentSnapshot> listener) {
        return bossesRef.document(userId)
                .addSnapshotListener(listener);
    }

    public void deleteBossByUserId(String userId, OnCompleteListener<Void> listener) {
        bossesRef.document(userId)
                .delete()
                .addOnCompleteListener(listener);
    }
}
