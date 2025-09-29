package com.e2_ma_tim09_2025.questify.services;


import android.util.Log;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.models.AllianceInvite;
import com.e2_ma_tim09_2025.questify.models.enums.AllianceInviteStatus;
import com.e2_ma_tim09_2025.questify.repositories.AllianceInviteRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import javax.inject.Inject;

public class AllianceInviteService {

    private final AllianceInviteRepository inviteRepository;

    @Inject
    public AllianceInviteService(AllianceInviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    /**
     * Send a new invite to a user.
     *
     * @param invite   AllianceInvite object containing sender, receiver, allianceId
     * @param listener OnCompleteListener<Void> to handle success/failure
     */
    public void sendInvite(@NonNull AllianceInvite invite, @NonNull OnCompleteListener<Void> listener) {
        if (invite.getFromUserId() == null || invite.getToUserId() == null || invite.getAllianceId() == null) {
            Log.e("AllianceInviteService", "Invalid invite data");
            // Ne pozivamo listener sa Task.forException, već samo logujemo i izlazimo
            return;
        }

        // Pozivamo repository
        inviteRepository.sendInvite(invite, listener);
    }

    /**
     * Accept an invite: mark invite as accepted.
     *
     * @param inviteId Invite ID
     * @param listener OnCompleteListener<Void> to handle success/failure
     */
    public void acceptInvite(@NonNull String inviteId, @NonNull OnCompleteListener<Void> listener) {
        inviteRepository.updateInviteStatus(inviteId, AllianceInviteStatus.ACCEPTED, listener);
    }

    public void declineInvite(@NonNull String inviteId, @NonNull OnCompleteListener<Void> listener) {
        inviteRepository.updateInviteStatus(inviteId, AllianceInviteStatus.REJECTED, listener);
    }


    public void getInvitesForUser(@NonNull String userId, @NonNull OnCompleteListener taskListener) {
        inviteRepository.getInvitesForUser(userId, taskListener);
    }
}
