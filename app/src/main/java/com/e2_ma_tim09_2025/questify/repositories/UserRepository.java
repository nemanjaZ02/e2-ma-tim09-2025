package com.e2_ma_tim09_2025.questify.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.dao.UserDao;
import com.e2_ma_tim09_2025.questify.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository {

    private final UserDao userDAO;
    private final FirebaseAuth auth;

    @Inject
    public UserRepository(UserDao userDAO, FirebaseAuth auth) {
        this.userDAO = userDAO;
        this.auth = auth;
    }

    public void registerUser(String email, String password, User user,
                             OnCompleteListener<AuthResult> authListener,
                             OnCompleteListener<Void> userSaveListener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (authListener != null) authListener.onComplete(task);

                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (uid == null) {
                            if (userSaveListener != null) {
                                userSaveListener.onComplete(
                                        com.google.android.gms.tasks.Tasks.forException(
                                                new IllegalStateException("currentUser is null after auth"))
                                );
                            }
                            return;
                        }

                        user.setId(uid);
                        user.setCreatedAt(System.currentTimeMillis());
                        userDAO.createUser(user, userSaveListener);

                        firebaseUser.sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Log.d("REGISTER", "Verification email sent to " + firebaseUser.getEmail());
                                    } else {
                                        Log.e("REGISTER", "Failed to send verification email", emailTask.getException());
                                    }
                                });

                    } else {
                        if (userSaveListener != null) {
                            userSaveListener.onComplete(
                                    com.google.android.gms.tasks.Tasks.forException(
                                            task.getException() != null ? task.getException() : new Exception("Auth failed")
                                    )
                            );
                        }
                    }
                });
    }
    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void getUser(String uid, OnCompleteListener<com.google.firebase.firestore.DocumentSnapshot> listener) {
        userDAO.getUser(uid, listener);
    }

    public void updateUser(User user, OnCompleteListener<Void> listener) {
        userDAO.updateUser(user, listener);
    }

    public void logout() {
        auth.signOut();
    }

    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    public void deleteUser(String user, OnCompleteListener<Void> listener) {
        userDAO.deleteUser(user, listener);
    }

}
