package com.e2_ma_tim09_2025.questify.services;

import android.util.Log;
import com.e2_ma_tim09_2025.questify.models.Alliance;
import com.e2_ma_tim09_2025.questify.models.AllianceConflictResult;
import com.e2_ma_tim09_2025.questify.models.AllianceInvite;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.AllianceInviteStatus;
import com.e2_ma_tim09_2025.questify.repositories.AllianceRepository;
import com.e2_ma_tim09_2025.questify.repositories.AllianceInviteRepository;
import com.e2_ma_tim09_2025.questify.repositories.UserRepository;
import com.e2_ma_tim09_2025.questify.services.SpecialMissionService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private final AllianceInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final SpecialMissionService specialMissionService;

    @Inject
    public AllianceService(AllianceInviteService inviteService, AllianceRepository allianceRepository, AllianceInviteRepository inviteRepository,
                           UserRepository userRepository, SpecialMissionService specialMissionService) {
        this.allianceRepository = allianceRepository;
        this.inviteService = inviteService;
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.specialMissionService = specialMissionService;
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
                
                // Create inactive special mission for this alliance
                Log.d("AllianceService", "🎯 Creating inactive special mission for alliance: " + allianceId);
                specialMissionService.createSpecialMission(allianceId, creatorUserId, missionTask -> {
                    if (missionTask.isSuccessful()) {
                        Log.d("AllianceService", "✅ Inactive special mission created successfully");
                    } else {
                        Log.e("AllianceService", "❌ Failed to create special mission", missionTask.getException());
                    }
                });
                
                // Add a small delay to ensure alliance is fully committed before sending invites
                Log.d("AllianceService", "⏳ Waiting for alliance to be fully committed...");
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("AllianceService", "🚀 Alliance committed, proceeding with invites");
                    
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
                    
                    // Complete the alliance creation process
                    Log.d("AllianceService", "=== ALLIANCE CREATION PROCESS COMPLETED ===");
                    listener.onComplete(task);
                }, 1000); // 1 second delay to ensure alliance is committed
            } else {
                Log.e("AllianceService", "❌ Failed to save alliance to Firestore", task.getException());
                listener.onComplete(task);
            }
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

    /**
     * Get alliance by ID
     */
    public void getAlliance(String allianceId, OnCompleteListener<Alliance> listener) {
        allianceRepository.getAlliance(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Alliance alliance = task.getResult().toObject(Alliance.class);
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(alliance));
            } else {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    task.getException() != null ? task.getException() : new Exception("Alliance not found")));
            }
        });
    }

    /**
     * Get alliances where the user is the leader
     */
    public void getAlliancesByLeader(String leaderId, OnCompleteListener<List<Alliance>> listener) {
        allianceRepository.getAlliancesByLeader(leaderId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Alliance> alliances = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    Alliance alliance = doc.toObject(Alliance.class);
                    if (alliance != null) {
                        alliances.add(alliance);
                    }
                }
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(alliances));
            } else {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    task.getException() != null ? task.getException() : new Exception("Failed to get alliances")));
            }
        });
    }

    /**
     * Get alliance where the user is a member (not necessarily leader)
     */
    public void getUserAlliance(String userId, OnCompleteListener<Alliance> listener) {
        // First check if user is a leader of any alliance
        getAlliancesByLeader(userId, leaderTask -> {
            if (leaderTask.isSuccessful() && leaderTask.getResult() != null && !leaderTask.getResult().isEmpty()) {
                // User is a leader, return their alliance
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(leaderTask.getResult().get(0)));
                return;
            }
            
            // If not a leader, search for alliances where user is a member
            allianceRepository.getAllAlliances(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        Alliance alliance = doc.toObject(Alliance.class);
                        if (alliance != null && alliance.getMemberIds() != null && 
                            alliance.getMemberIds().contains(userId)) {
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(alliance));
                            return;
                        }
                    }
                    // User is not a member of any alliance
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
                } else {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                        task.getException() != null ? task.getException() : new Exception("Failed to get alliances")));
                }
            });
        });
    }

    /**
     * Get alliance members with their user details
     */
    public void getAllianceMembers(String allianceId, OnCompleteListener<List<User>> listener) {
        allianceRepository.getAlliance(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Alliance alliance = task.getResult().toObject(Alliance.class);
                if (alliance != null && alliance.getMemberIds() != null) {
                    List<String> memberIds = alliance.getMemberIds();
                    List<User> members = new ArrayList<>();
                    
                    // Fetch user details for each member
                    List<com.google.android.gms.tasks.Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    for (String memberId : memberIds) {
                        userTasks.add(userRepository.getUserTask(memberId));
                    }
                    
                    com.google.android.gms.tasks.Tasks.whenAllSuccess(userTasks)
                        .addOnSuccessListener(results -> {
                            for (Object obj : results) {
                                DocumentSnapshot doc = (DocumentSnapshot) obj;
                                if (doc != null && doc.exists()) {
                                    User user = doc.toObject(User.class);
                                    if (user != null) {
                                        members.add(user);
                                    }
                                }
                            }
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(members));
                        })
                        .addOnFailureListener(e -> {
                            listener.onComplete(com.google.android.gms.tasks.Tasks.forException(e));
                        });
                } else {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(new ArrayList<>()));
                }
            } else {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    task.getException() != null ? task.getException() : new Exception("Alliance not found")));
            }
        });
    }

    /**
     * Get users who are eligible for alliance invitation (leader's friends who are not in alliance, not already invited)
     */
    public void getEligibleUsersForInvitation(String allianceId, String leaderId, OnCompleteListener<List<User>> listener) {
        Log.d("AllianceService", "=== GETTING ELIGIBLE USERS FOR INVITATION ===");
        Log.d("AllianceService", "Alliance ID: " + allianceId);
        Log.d("AllianceService", "Leader ID: " + leaderId);
        
        // First get the leader's friends (instead of all users)
        userRepository.getFriends(leaderId, friendsTask -> {
            if (!friendsTask.isSuccessful()) {
                Log.e("AllianceService", "❌ Failed to get leader's friends: " + 
                    (friendsTask.getException() != null ? friendsTask.getException().getMessage() : "Unknown error"));
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    friendsTask.getException() != null ? friendsTask.getException() : new Exception("Failed to get leader's friends")));
                return;
            }

            List<User> friends = new ArrayList<>();
            if (friendsTask.getResult() != null) {
                for (DocumentSnapshot doc : friendsTask.getResult().getDocuments()) {
                    User user = doc.toObject(User.class);
                    if (user != null) friends.add(user);
                }
            }

            if (friends.isEmpty()) {
                Log.d("AllianceService", "ℹ️ Leader has no friends to invite");
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(new ArrayList<>()));
                return;
            }

            Log.d("AllianceService", "Found " + friends.size() + " friends for leader");

            // Get alliance members to exclude them
            getAllianceMembers(allianceId, allianceMembersTask -> {
                if (!allianceMembersTask.isSuccessful()) {
                    listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                        allianceMembersTask.getException() != null ? allianceMembersTask.getException() : new Exception("Failed to get alliance members")));
                    return;
                }

                List<User> allianceMembers = allianceMembersTask.getResult();
                List<String> allianceMemberIds = new ArrayList<>();
                for (User member : allianceMembers) {
                    allianceMemberIds.add(member.getId());
                }

                Log.d("AllianceService", "Alliance has " + allianceMemberIds.size() + " members");

                // Get pending invites to exclude already invited users
                inviteRepository.getInvitesForAlliance(allianceId, invitesTask -> {
                    if (!invitesTask.isSuccessful()) {
                        Log.e("AllianceService", "❌ Failed to get invites: " + 
                            (invitesTask.getException() != null ? invitesTask.getException().getMessage() : "Unknown error"));
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            invitesTask.getException() != null ? invitesTask.getException() : new Exception("Failed to get invites")));
                        return;
                    }

                    List<String> invitedUserIds = new ArrayList<>();
                    for (DocumentSnapshot doc : invitesTask.getResult()) {
                        AllianceInvite invite = doc.toObject(AllianceInvite.class);
                        if (invite != null && invite.getStatus() == AllianceInviteStatus.PENDING) {
                            invitedUserIds.add(invite.getToUserId());
                        }
                    }

                    Log.d("AllianceService", "Found " + invitedUserIds.size() + " pending invites");

                    // Filter eligible users from friends list
                    List<User> eligibleUsers = new ArrayList<>();
                    for (User friend : friends) {
                        if (!friend.getId().equals(leaderId) // Not the leader
                            && !allianceMemberIds.contains(friend.getId()) // Not already a member
                            && !invitedUserIds.contains(friend.getId())) { // Not already invited
                            eligibleUsers.add(friend);
                            Log.d("AllianceService", "✅ Friend eligible for invitation: " + friend.getUsername());
                        } else {
                            Log.d("AllianceService", "❌ Friend not eligible: " + friend.getUsername() + 
                                " (leader: " + friend.getId().equals(leaderId) + 
                                ", member: " + allianceMemberIds.contains(friend.getId()) + 
                                ", invited: " + invitedUserIds.contains(friend.getId()) + ")");
                        }
                    }

                    Log.d("AllianceService", "=== ELIGIBLE USERS RESULT ===");
                    Log.d("AllianceService", "Total eligible friends: " + eligibleUsers.size());
                    for (User user : eligibleUsers) {
                        Log.d("AllianceService", "- " + user.getUsername() + " (" + user.getId() + ")");
                    }

                    listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(eligibleUsers));
                });
            });
        });
    }

    /**
     * Send invitation to a user for an alliance
     */
    public void sendAllianceInvitation(String allianceId, String fromUserId, String toUserId, OnCompleteListener<Void> listener) {
        String inviteId = UUID.randomUUID().toString();
        AllianceInvite invite = new AllianceInvite();
        invite.setId(inviteId);
        invite.setAllianceId(allianceId);
        invite.setFromUserId(fromUserId);
        invite.setToUserId(toUserId);
        invite.setStatus(AllianceInviteStatus.PENDING);
        invite.setTimestamp(System.currentTimeMillis());

        inviteService.sendInvite(invite, listener);
    }


    /**
     * Delete alliance and remove all members
     * Only the leader can delete the alliance
     */
    public void deleteAlliance(String allianceId, String leaderId, OnCompleteListener<Void> listener) {
        // First verify that the user is the leader of this alliance
        allianceRepository.getAlliance(allianceId, task -> {
            if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Alliance not found")));
                return;
            }

            Alliance alliance = task.getResult().toObject(Alliance.class);
            if (alliance == null) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Failed to parse alliance data")));
                return;
            }

            // Check if the user is the leader
            if (!leaderId.equals(alliance.getLeaderId())) {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Only the alliance leader can delete the alliance")));
                return;
            }

            // Check if mission is active - cannot delete alliance during active mission
            if (alliance.isMissionStarted()) {
                Log.w("AllianceService", "❌ Cannot delete alliance " + allianceId + " - mission is active");
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    new Exception("Cannot delete alliance while a mission is active. Please complete or cancel the current mission first.")));
                return;
            }

            // Delete the alliance and remove all members
            allianceRepository.deleteAllianceAndRemoveMembers(allianceId, deleteTask -> {
                if (deleteTask.isSuccessful()) {
                    Log.d("AllianceService", "✅ Alliance deleted successfully: " + allianceId);
                    // Also delete all pending invites for this alliance
                    deleteAllianceInvites(allianceId, () -> {
                        listener.onComplete(deleteTask);
                    });
                } else {
                    Log.e("AllianceService", "❌ Failed to delete alliance: " + allianceId, deleteTask.getException());
                    listener.onComplete(deleteTask);
                }
            });
        });
    }

    private void deleteAllianceInvites(String allianceId, Runnable onComplete) {
        inviteRepository.getInvitesForAlliance(allianceId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Delete all invites for this alliance
                List<com.google.android.gms.tasks.Task<Void>> deleteTasks = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    String inviteId = doc.getId();
                    deleteTasks.add(inviteRepository.deleteInviteTask(inviteId));
                }
                
                if (!deleteTasks.isEmpty()) {
                    com.google.android.gms.tasks.Tasks.whenAll(deleteTasks)
                            .addOnCompleteListener(deleteAllTask -> {
                                Log.d("AllianceService", "✅ Deleted " + deleteTasks.size() + " alliance invites");
                                onComplete.run();
                            });
                } else {
                    onComplete.run();
                }
            } else {
                onComplete.run();
            }
        });
    }

    /**
     * Get alliance where user is a member but NOT a leader
     */
    public void getUserMemberAlliance(String userId, OnCompleteListener<Alliance> listener) {
        // Get all alliances and find one where user is a member but not a leader
        allianceRepository.getAllAlliances(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) {
                    Alliance alliance = doc.toObject(Alliance.class);
                    if (alliance != null && alliance.getMemberIds() != null && 
                        alliance.getMemberIds().contains(userId) && 
                        !userId.equals(alliance.getLeaderId())) {
                        // User is a member but not a leader
                        listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(alliance));
                        return;
                    }
                }
                // User is not a member of any alliance (as non-leader)
                listener.onComplete(com.google.android.gms.tasks.Tasks.forResult(null));
            } else {
                listener.onComplete(com.google.android.gms.tasks.Tasks.forException(
                    task.getException() != null ? task.getException() : new Exception("Failed to get alliances")));
            }
        });
    }

}
