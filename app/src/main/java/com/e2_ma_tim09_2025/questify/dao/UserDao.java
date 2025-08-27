package com.e2_ma_tim09_2025.questify.dao;

import androidx.annotation.NonNull;

import com.e2_ma_tim09_2025.questify.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserDao {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final CollectionReference usersRef;

    @Inject
    public UserDao(FirebaseFirestore firestore, FirebaseAuth auth) {
        this.firestore = firestore;
        this.auth = auth;
        this.usersRef = firestore.collection("users");
    }

    public void createUser(User user, OnCompleteListener<Void> listener) {
        usersRef.document(user.getId())
                .set(user)
                .addOnCompleteListener(listener);
    }

    public void getUser(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        usersRef.document(userId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateUser(User user, OnCompleteListener<Void> listener) {
        usersRef.document(user.getId())
                .set(user)
                .addOnCompleteListener(listener);
    }

    public void deleteUser(String userId, OnCompleteListener<Void> listener) {
        usersRef.document(userId)
                .delete()
                .addOnCompleteListener(listener);
    }
}
