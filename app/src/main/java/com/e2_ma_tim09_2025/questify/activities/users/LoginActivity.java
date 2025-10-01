package com.e2_ma_tim09_2025.questify.activities.users;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.tasks.TasksMainActivity;
import com.e2_ma_tim09_2025.questify.services.FCMTokenService;
import com.e2_ma_tim09_2025.questify.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private UserViewModel userViewModel;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Inject
    FCMTokenService fcmTokenService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        // Hilt ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Login button listener
        loginButton.setOnClickListener(v -> loginUser());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // Korisnik je već ulogovan → idi na glavni ekran
            startActivity(new Intent(this, TasksMainActivity.class));
            finish();
        } else {
            // Korisnik nije ulogovan → ostani na login ekranu
        }

    }

//    private void loginUser() {
//        String email = emailEditText.getText().toString().trim();
//        String password = passwordEditText.getText().toString().trim();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = auth.getCurrentUser();
//                        if (user != null && user.isEmailVerified()) {
//                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
//
//                            // Example: Navigate to MainActivity (replace with your actual main screen)
//                            Intent intent = new Intent(this, TasksMainActivity.class);
//                            startActivity(intent);
//                            finish();
//
//                        } else {
//                            Toast.makeText(this, "Please verify your email before logging in", Toast.LENGTH_LONG).show();
//                            if (user != null) {
//                                user.sendEmailVerification(); // optional: resend verification
//                            }
//                        }
//                    } else {
//                        String errorMessage = (task.getException() != null) ?
//                                task.getException().getMessage() : "Login failed";
//                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
private void loginUser() {
    String email = emailEditText.getText().toString().trim();
    String password = passwordEditText.getText().toString().trim();

    if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
        return;
    }

    auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        if (user.isEmailVerified()) {
                            // CASE 1: User is logged in and email is verified.
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            
                            // Generate and store FCM token for this device
                            if (fcmTokenService != null) {
                                fcmTokenService.generateAndStoreToken(fcmTask -> {
                                    if (fcmTask.isSuccessful()) {
                                        android.util.Log.d("LoginActivity", "FCM token generated and stored successfully");
                                    } else {
                                        android.util.Log.e("LoginActivity", "Failed to generate FCM token", fcmTask.getException());
                                    }
                                });
                            }
                            
                            Intent intent = new Intent(this, TasksMainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // CASE 2: User is logged in but email is NOT verified.
                            checkUserCreationTime(user);
                        }
                    }
                } else {
                    // Login failed (e.g., wrong password, user not found)
                    String errorMessage = "Login failed. Please check your email and password.";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
}

    private void checkUserCreationTime(FirebaseUser user) {
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve the timestamp from the document
                            long creationTimestamp = document.getTimestamp("created_at").toDate().getTime();
                            long currentTime = System.currentTimeMillis();
                            long timeElapsed = currentTime - creationTimestamp;

                            // 24 hours in milliseconds
                            long twentyFourHours = 24 * 60 * 60 * 1000L;

                            if (timeElapsed > twentyFourHours) {
                                // CASE 2.1: More than 24 hours have passed. Force re-registration.
                                Toast.makeText(this, "Account activation link has expired. Please register again.", Toast.LENGTH_LONG).show();

                                // Delete the user from Firebase Authentication
                                user.delete()
                                        .addOnCompleteListener(deleteTask -> {
                                            if (deleteTask.isSuccessful()) {
                                                // Optional: Delete the user's document from Firestore
                                                db.collection("users").document(user.getUid()).delete();
                                                Toast.makeText(this, "Your old account has been deleted.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                // CASE 2.2: Less than 24 hours have passed. Prompt to verify email.
                                Toast.makeText(this, "Please verify your email before logging in. A new link has been sent.", Toast.LENGTH_LONG).show();
                                //user.sendEmailVerification();
                            }
                        } else {
                            // User document not found in Firestore. Handle as a failure.
                            Toast.makeText(this, "User data not found. Please register again.", Toast.LENGTH_LONG).show();
                            user.delete();
                        }
                    } else {
                        // Failed to read user data from Firestore
                        Toast.makeText(this, "Failed to check account status. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
