package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;
import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.AllianceInvite;
import com.e2_ma_tim09_2025.questify.models.enums.AllianceInviteStatus;
import com.e2_ma_tim09_2025.questify.repositories.AllianceRepository;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AllianceService {
    private final AllianceRepository allianceRepository;
    private final AllianceInviteService inviteService;

    @Inject
    public AllianceService(AllianceInviteService inviteService) {
        this.allianceRepository = new AllianceRepository();
        this.inviteService = inviteService;
    }

    public void createAllianceWithInvites(String allianceName, String creatorUserId, @Nullable List<String> invitedMemberIds, OnCompleteListener<Void> listener) {
        Log.d("AllianceService", "=== CREATING ALLIANCE WITH INVITES ===");
        Log.d("AllianceService", "Alliance Name: " + allianceName);
        Log.d("AllianceService", "Creator User ID: " + creatorUserId);
        Log.d("AllianceService", "Invited Member IDs: " + (invitedMemberIds != null ? invitedMemberIds.toString() : "NULL"));
        
        // 1. Generišemo jedinstveni ID saveza
        String allianceId = UUID.randomUUID().toString();
        Log.d("AllianceService", "Generated Alliance ID: " + allianceId);

        // 2. Kreiramo savez samo sa vođom
        List<String> members = new ArrayList<>();
        members.add(creatorUserId);

        Alliance alliance = new Alliance();
        alliance.setId(allianceId);
        alliance.setName(allianceName);
        alliance.setLeaderId(creatorUserId);
        alliance.setMemberIds(members);
        alliance.setMissionStarted(false);
        
        Log.d("AllianceService", "Created Alliance object: " + alliance.getName() + " (ID: " + alliance.getId() + ")");

        // 3. Sačuvamo savez u Firestore
        Log.d("AllianceService", "🔥 Saving alliance to Firestore...");
        allianceRepository.createAlliance(alliance, task -> {
            if (task.isSuccessful()) {
                Log.d("AllianceService", "✅ Alliance saved successfully to Firestore");
                
                if (invitedMemberIds != null && !invitedMemberIds.isEmpty()) {
                    Log.d("AllianceService", "📧 Sending invites to " + invitedMemberIds.size() + " members");
                    // 4. Pošalji pozive članovima
                    for (String friendId : invitedMemberIds) {
                        Log.d("AllianceService", "📧 Creating invite for friend: " + friendId);
                        
                        String inviteId = UUID.randomUUID().toString();
                        Log.d("AllianceService", "📧 Generated invite ID: " + inviteId);
                        
                        AllianceInvite invite = new AllianceInvite();
                        invite.setId(inviteId); // Generate invite ID
                        invite.setAllianceId(allianceId);
                        invite.setFromUserId(creatorUserId);
                        invite.setToUserId(friendId);
                        invite.setStatus(AllianceInviteStatus.PENDING); // PENDING = čekanje na prihvatanje
                        invite.setTimestamp(System.currentTimeMillis());
                        
                        Log.d("AllianceService", "📧 Invite object created: " + invite.toString());
                        Log.d("AllianceService", "📧 Invite ID after creation: " + invite.getId());

                        inviteService.sendInvite(invite, inviteTask -> {
                            if (inviteTask.isSuccessful()) {
                                Log.d("AllianceService", "✅ Invite sent successfully to " + friendId);
                            } else {
                                Log.e("AllianceService", "❌ Failed to send invite to " + friendId, inviteTask.getException());
                            }
                        });
                    }
                } else {
                    Log.d("AllianceService", "ℹ️ No members to invite");
                }
            } else {
                Log.e("AllianceService", "❌ Failed to save alliance to Firestore", task.getException());
            }
            
            Log.d("AllianceService", "=== ALLIANCE CREATION PROCESS COMPLETED ===");
            listener.onComplete(task);
        });
    }
//    public void disbandAlliance(@NonNull String allianceId, @NonNull String userId, @NonNull OnCompleteListener<Void> listener) {
//        // 1. Dohvati savez iz repozitorijuma
//        allianceRepository.getAlliance(allianceId, task -> {
//            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
//                Log.e("AllianceService", "Alliance not found: " + allianceId);
//                listener.onComplete(task);
//                return;
//            }
//
//            Alliance alliance = task.getResult().toObject(Alliance.class);
//
//            // 2. Proveri da li korisnik koji poziva je vođa saveza
//            if (!userId.equals(alliance.getLeaderId())) {
//                Log.e("AllianceService", "Only the leader can disband the alliance.");
//                listener.onComplete(task); // Možeš napraviti Task failure ili samo log
//                return;
//            }
//
//            // 3. Proveri da li je misija pokrenuta
//            if (alliance.isMissionStarted()) {
//                Log.e("AllianceService", "Cannot disband alliance during an active mission.");
//                listener.onComplete(task);
//                return;
//            }
//
//            // 4. Ukloni sve članove saveza (osim vođe ili zajedno sa njim)
//            List<String> members = new ArrayList<>(alliance.getMemberIds());
//            members.clear(); // svi članovi uklonjeni
//
//            alliance.setMemberIds(members);
//
//        });
//    }


}
