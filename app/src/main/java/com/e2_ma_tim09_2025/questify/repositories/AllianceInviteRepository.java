package com.e2_ma_tim09_2025.questify.repositories;

import android.util.Log;

import com.e2_ma_tim09_2025.questify.models.AllianceInvite;
import com.e2_ma_tim09_2025.questify.models.enums.AllianceInviteStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AllianceInviteRepository {
    private final FirebaseFirestore db;
    private final CollectionReference invitesRef;

    @Inject
    public AllianceInviteRepository(FirebaseFirestore firestore) {
        this.db = firestore;
        this.invitesRef = db.collection("allianceInvites");
    }

    // Send invite
    public void sendInvite(AllianceInvite invite, OnCompleteListener<Void> listener) {
        Log.d("AllianceInviteRepository", "=== SAVING INVITE TO FIRESTORE ===");
        Log.d("AllianceInviteRepository", "Invite ID: " + invite.getId());
        Log.d("AllianceInviteRepository", "From User: " + invite.getFromUserId());
        Log.d("AllianceInviteRepository", "To User: " + invite.getToUserId());
        Log.d("AllianceInviteRepository", "Alliance ID: " + invite.getAllianceId());
        Log.d("AllianceInviteRepository", "Status: " + invite.getStatus());
        
        // Check for null ID
        if (invite.getId() == null || invite.getId().isEmpty()) {
            Log.e("AllianceInviteRepository", "❌ CRITICAL ERROR: Invite ID is null or empty!");
            Log.e("AllianceInviteRepository", "Invite object: " + invite.toString());
            if (listener != null) {
                // Create a failed task
                com.google.android.gms.tasks.Tasks.<Void>forException(new Exception("Invite ID is null"))
                    .addOnCompleteListener(listener);
            }
            return;
        }
        
        Log.d("AllianceInviteRepository", "🔥 Saving invite with ID: " + invite.getId());
        invitesRef.document(invite.getId())
                .set(invite)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AllianceInviteRepository", "✅ Invite saved successfully to Firestore");
                    } else {
                        Log.e("AllianceInviteRepository", "❌ Failed to save invite to Firestore", task.getException());
                    }
                    listener.onComplete(task);
                });
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

    // Get all invites for a specific alliance
    public void getInvitesForAlliance(String allianceId, OnCompleteListener<QuerySnapshot> listener) {
        invitesRef.whereEqualTo("allianceId", allianceId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Get all invites sent by a user (for filtering out users who already have pending invites)
    public void getInvitesSentByUser(String fromUserId, OnCompleteListener<QuerySnapshot> listener) {
        invitesRef.whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("status", AllianceInviteStatus.PENDING.name())
                .get()
                .addOnCompleteListener(listener);
    }

    // Delete invite task for use with Tasks.whenAll
    public com.google.android.gms.tasks.Task<Void> deleteInviteTask(String inviteId) {
        return invitesRef.document(inviteId).delete();
    }

}
