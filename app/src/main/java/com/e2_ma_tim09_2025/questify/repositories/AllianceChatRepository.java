package com.e2_ma_tim09_2025.questify.repositories;

import com.e2_ma_tim09_2025.questify.models.AllianceMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AllianceChatRepository {
    private final FirebaseFirestore db;
    private final CollectionReference messagesRef;

    @Inject
    public AllianceChatRepository(FirebaseFirestore firestore) {
        this.db = firestore;
        this.messagesRef = db.collection("alliance_messages");
    }

    /**
     * Send a message to alliance chat
     */
    public void sendMessage(AllianceMessage message, OnCompleteListener<Void> listener) {
        messagesRef.document(message.getId())
                .set(message)
                .addOnCompleteListener(listener);
    }

    /**
     * Get all messages for an alliance, ordered by timestamp
     */
    public void getAllianceMessages(String allianceId, OnCompleteListener<QuerySnapshot> listener) {
        // Use a simpler query to avoid composite index requirement
        messagesRef.whereEqualTo("allianceId", allianceId)
                .get()
                .addOnCompleteListener(listener);
    }

    /**
     * Get recent messages for an alliance (last 50 messages)
     */
    public void getRecentAllianceMessages(String allianceId, OnCompleteListener<QuerySnapshot> listener) {
        // Use a simpler query to avoid composite index requirement
        messagesRef.whereEqualTo("allianceId", allianceId)
                .limit(50)
                .get()
                .addOnCompleteListener(listener);
    }

    /**
     * Listen to new messages in real-time
     */
    public void listenToNewMessages(String allianceId, long lastMessageTimestamp, OnCompleteListener<QuerySnapshot> listener) {
        // Use a simpler query to avoid composite index requirement
        messagesRef.whereEqualTo("allianceId", allianceId)
                .get()
                .addOnCompleteListener(listener);
    }

    /**
     * Delete a message (for moderation purposes)
     */
    public void deleteMessage(String messageId, OnCompleteListener<Void> listener) {
        messagesRef.document(messageId)
                .delete()
                .addOnCompleteListener(listener);
    }
}
