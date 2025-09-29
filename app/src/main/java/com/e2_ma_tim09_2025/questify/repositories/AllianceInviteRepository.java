package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.AllianceInvite;
import com.e2_ma_tim09_2025.questify.models.enums.AllianceInviteStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AllianceInviteRepository {
    private final FirebaseFirestore db;
    private final CollectionReference invitesRef;

    public AllianceInviteRepository() {
        db = FirebaseFirestore.getInstance();
        invitesRef = db.collection("allianceInvites");
    }

    // Send invite
    public void sendInvite(AllianceInvite invite, OnCompleteListener<Void> listener) {
        invitesRef.document(invite.getId())
                .set(invite)
                .addOnCompleteListener(listener);
    }

    // Get invite by ID
    public void getInvite(String inviteId, OnCompleteListener<DocumentSnapshot> listener) {
        invitesRef.document(inviteId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Update invite (e.g., accepted)
    public void updateInvite(AllianceInvite invite, OnCompleteListener<Void> listener) {
        invitesRef.document(invite.getId())
                .set(invite)
                .addOnCompleteListener(listener);
    }

    // Delete invite
    public void deleteInvite(String inviteId, OnCompleteListener<Void> listener) {
        invitesRef.document(inviteId)
                .delete()
                .addOnCompleteListener(listener);
    }

    // Get all invites for a user
    public void getInvitesForUser(String userId, OnCompleteListener<QuerySnapshot> listener) {
        invitesRef.whereEqualTo("toUserId", userId)
                .get()
                .addOnCompleteListener(listener);
    }
    public void updateInviteStatus(String inviteId, AllianceInviteStatus status, OnCompleteListener<Void> listener) {
        invitesRef.document(inviteId)
                .update("status", status.name()) // Firestore čuva string
                .addOnCompleteListener(listener);
    }

}
