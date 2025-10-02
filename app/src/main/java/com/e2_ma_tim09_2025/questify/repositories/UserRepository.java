package com.e2_ma_tim09_2025.questify.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.dao.UserDao;
import com.e2_ma_tim09_2025.questify.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final CollectionReference usersRef;

    @Inject
    public UserRepository(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
        this.usersRef = firestore.collection("users");
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
                        user.setQrCode(uid); // simple, unique QR code for this user


                        usersRef.document(uid)
                                .set(user)
                                .addOnCompleteListener(userSaveListener);

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

    // Get user from Firestore
    public void getUser(String uid, OnCompleteListener<DocumentSnapshot> listener) {
        usersRef.document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && task.getResult().exists()) {
                            Log.d("UserRepo", "User found: " + uid);
                        } else {
                            Log.e("UserRepo", "User not found: " + uid);
                        }
                    } else {
                        Log.e("UserRepo", "Failed to get user: " + task.getException());
                    }
                    listener.onComplete(task);
                });
    }

    // Get user task for use with Tasks.whenAllSuccess
    public com.google.android.gms.tasks.Task<DocumentSnapshot> getUserTask(String uid) {
        return usersRef.document(uid).get();
    }


    // Update user in Firestore
    public void updateUser(User user, OnCompleteListener<Void> listener) {
        usersRef.document(user.getId())
                .set(user)
                .addOnCompleteListener(listener);
    }

    // Delete user from Firestore + Auth
    public void deleteUser(String uid, OnCompleteListener<Void> listener) {
        // Delete Firestore document
        usersRef.document(uid)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Then delete Auth user
                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null && currentUser.getUid().equals(uid)) {
                            currentUser.delete()
                                    .addOnCompleteListener(listener);
                        } else {
                            if (listener != null) listener.onComplete(task);
                        }
                    } else {
                        if (listener != null) listener.onComplete(task);
                    }
                });
    }
    public void logout() {
        auth.signOut();
    }
    public String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    public void getAllUsers(OnCompleteListener<QuerySnapshot> listener) {
        firestore.collection("users").get().addOnCompleteListener(listener);
    }
    public interface UsersCallback {
        void onUsersFetched(List<User> users);
    }

    public void getAllNonFriendUsers(UsersCallback callback) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return;

        getUser(currentUserId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User currentUser = task.getResult().toObject(User.class);
                List<String> friendsIds = currentUser != null && currentUser.getFriends() != null
                        ? currentUser.getFriends()
                        : new ArrayList<>();

                getAllUsers(allTask -> {
                    if (allTask.isSuccessful() && allTask.getResult() != null) {
                        List<User> filteredUsers = new ArrayList<>();
                        for (DocumentSnapshot doc : allTask.getResult()) {
                            User user = doc.toObject(User.class);
                            if (user != null
                                    && !user.getId().equals(currentUserId)
                                    && !friendsIds.contains(user.getId())) {
                                filteredUsers.add(user);
                            }
                        }
                        callback.onUsersFetched(filteredUsers);
                    }
                });
            }
        });
    }


    public void getFriends(String userId, OnCompleteListener<QuerySnapshot> listener) {
        // Step 1: Get the user document
        usersRef.document(userId).get().addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful() && userTask.getResult() != null) {
                User user = userTask.getResult().toObject(User.class);

                if (user == null || user.getFriends() == null || user.getFriends().isEmpty()) {
                    // No friends, return empty QuerySnapshot-like task
                    listener.onComplete(Tasks.forResult(null)); // null QuerySnapshot
                    return;
                }

                List<String> friendIds = user.getFriends();

                // Step 2: Query the friends by document IDs
                usersRef.whereIn(FieldPath.documentId(), friendIds)
                        .get()
                        .addOnCompleteListener(listener); // This now matches QuerySnapshot
            } else {
                // Return failure to listener by creating a failed Task<QuerySnapshot>
                listener.onComplete(Tasks.forException(
                        userTask.getException() != null ? userTask.getException() : new Exception("Failed to get user")
                ));
            }
        });
    }

}
