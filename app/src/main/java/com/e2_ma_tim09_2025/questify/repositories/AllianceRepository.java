package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AllianceRepository {
    private final FirebaseFirestore db;
    private final CollectionReference alliancesRef;

    @Inject
    public AllianceRepository(FirebaseFirestore firestore) {
        this.db = firestore;
        this.alliancesRef = db.collection("alliances");
    }

    // Create a new alliance
    public void createAlliance(Alliance alliance, OnCompleteListener<Void> listener) {
        alliancesRef.document(alliance.getId())
                .set(alliance)
                .addOnCompleteListener(listener);
    }

    // Get alliance by ID
    public void getAlliance(String allianceId, OnCompleteListener<DocumentSnapshot> listener) {
        alliancesRef.document(allianceId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Update alliance (e.g., add member, change missionStarted)
    public void updateAlliance(Alliance alliance, OnCompleteListener<Void> listener) {
        alliancesRef.document(alliance.getId())
                .set(alliance)
                .addOnCompleteListener(listener);
    }

    // Delete alliance
    public void deleteAlliance(String allianceId, OnCompleteListener<Void> listener) {
        alliancesRef.document(allianceId)
                .delete()
                .addOnCompleteListener(listener);
    }

    // Get alliances where user is the leader
    public void getAlliancesByLeader(String leaderId, OnCompleteListener<QuerySnapshot> listener) {
        alliancesRef.whereEqualTo("leaderId", leaderId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Delete alliance and remove all members
    public void deleteAllianceAndRemoveMembers(String allianceId, OnCompleteListener<Void> listener) {
        // First get the alliance to get member list
        alliancesRef.document(allianceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Alliance alliance = documentSnapshot.toObject(Alliance.class);
                        if (alliance != null && alliance.getMemberIds() != null) {
                            // Remove allianceId from all member users
                            removeAllianceFromUsers(alliance.getMemberIds(), () -> {
                                // Then delete the alliance document
                                alliancesRef.document(allianceId)
                                        .delete()
                                        .addOnCompleteListener(listener);
                            });
                        } else {
                            // No members, just delete alliance
                            alliancesRef.document(allianceId)
                                    .delete()
                                    .addOnCompleteListener(listener);
                        }
                    } else {
                        // Alliance doesn't exist, consider it successful
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(e));
                });
    }

    private void removeAllianceFromUsers(List<String> memberIds, Runnable onComplete) {
        if (memberIds == null || memberIds.isEmpty()) {
            onComplete.run();
            return;
        }

        // Use batch write to update all users atomically
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        
        for (String memberId : memberIds) {
            com.google.firebase.firestore.DocumentReference userRef = db.collection("users").document(memberId);
            batch.update(userRef, "allianceId", null);
        }
        
        batch.commit()
                .addOnSuccessListener(aVoid -> onComplete.run())
                .addOnFailureListener(e -> {
                    // Even if some updates fail, continue with alliance deletion
                    onComplete.run();
                });
    }

    // Get all alliances
    public void getAllAlliances(OnCompleteListener<QuerySnapshot> listener) {
        alliancesRef.get().addOnCompleteListener(listener);
    }
}

