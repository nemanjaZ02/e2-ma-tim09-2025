package com.e2_ma_tim09_2025.questify.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.dao.UserDao;
import com.e2_ma_tim09_2025.questify.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;

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

    // Register user with email and password
//    public void registerUser(String email, String password, User user,
//                             OnCompleteListener<AuthResult> authListener,
//                             OnCompleteListener<Void> userSaveListener) {
//        auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(authListener)
//                .addOnCompleteListener(task -> {
//                    // Always notify the caller about auth result
//                    authListener.onComplete(task);
//
//                    if (task.isSuccessful()) {
//                        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
//                        if (uid == null) {
//                            // Extremely rare, but handle it
//                            Log.e("USER_REPO", "Auth success but currentUser is null");
//                            if (userSaveListener != null) {
//                                // propagate a failure
//                                userSaveListener.onComplete(
//                                        com.google.android.gms.tasks.Tasks.forException(
//                                                new IllegalStateException("currentUser is null after auth")
//                                        )
//                                );
//                            }
//                            return;
//                        }
//
//                        user.setId(uid);
//                        userDAO.createUser(user, userSaveListener);
//
//                    } else {
//                        Log.e("USER_REPO", "Auth failed", task.getException());
//                        if (userSaveListener != null) {
//                            userSaveListener.onComplete(
//                                    com.google.android.gms.tasks.Tasks.forException(
//                                            task.getException() != null ? task.getException()
//                                                    : new Exception("Auth failed")
//                                    )
//                            );
//                        }
//                    }
//                });
//    }

    public void registerUser(String email, String password, User user,
                             OnCompleteListener<AuthResult> authListener,
                             OnCompleteListener<Void> userSaveListener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (authListener != null) authListener.onComplete(task);

                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
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
                        userDAO.createUser(user, userSaveListener);
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

    // Login user
    public void loginUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // Get user by UID
    public void getUser(String uid, OnCompleteListener<com.google.firebase.firestore.DocumentSnapshot> listener) {
        userDAO.getUser(uid, listener);
    }

    // Update user
    public void updateUser(User user, OnCompleteListener<Void> listener) {
        userDAO.updateUser(user, listener);
    }

    // Logout
    public void logout() {
        auth.signOut();
    }

    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

}
