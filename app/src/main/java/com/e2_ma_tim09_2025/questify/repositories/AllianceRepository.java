package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AllianceRepository {
    private final FirebaseFirestore db;
    private final CollectionReference alliancesRef;

    public AllianceRepository() {
        db = FirebaseFirestore.getInstance();
        alliancesRef = db.collection("alliances");
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
}

