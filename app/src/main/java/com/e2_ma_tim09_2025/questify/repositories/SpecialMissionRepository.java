package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.SpecialMission;
import com.e2_ma_tim09_2025.questify.models.enums.SpecialMissionStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpecialMissionRepository {
    private final FirebaseFirestore db;
    private final CollectionReference specialMissionsRef;

    @Inject
    public SpecialMissionRepository(FirebaseFirestore firestore) {
        this.db = firestore;
        this.specialMissionsRef = db.collection("specialMissions");
    }

    public void createSpecialMission(SpecialMission specialMission, OnCompleteListener<Void> listener) {
        specialMissionsRef.document(specialMission.getAllianceId())
                .set(specialMission)
                .addOnCompleteListener(listener);
    }

    public void getSpecialMissionByAllianceId(String allianceId, OnCompleteListener<DocumentSnapshot> listener) {
        specialMissionsRef.document(allianceId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateSpecialMission(SpecialMission specialMission, OnCompleteListener<Void> listener) {
        specialMissionsRef.document(specialMission.getAllianceId())
                .set(specialMission)
                .addOnCompleteListener(listener);
    }

    public void deleteSpecialMission(String allianceId, OnCompleteListener<Void> listener) {
        specialMissionsRef.document(allianceId)
                .delete()
                .addOnCompleteListener(listener);
    }

    public void getAllActiveMissions(OnCompleteListener<QuerySnapshot> listener) {
        specialMissionsRef.whereEqualTo("status", SpecialMissionStatus.ACTIVE)
                .get()
                .addOnCompleteListener(listener);
    }
}
