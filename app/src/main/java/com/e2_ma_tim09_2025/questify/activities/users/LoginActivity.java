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
import com.e2_ma_tim09_2025.questify.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private UserViewModel userViewModel;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

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
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                            // Example: Navigate to MainActivity (replace with your actual main screen)
                            Intent intent = new Intent(this, TasksMainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(this, "Please verify your email before logging in", Toast.LENGTH_LONG).show();
                            if (user != null) {
                                user.sendEmailVerification(); // optional: resend verification
                            }
                        }
                    } else {
                        String errorMessage = (task.getException() != null) ?
                                task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
