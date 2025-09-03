package com.e2_ma_tim09_2025.questify.activities.users;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.viewmodels.UserViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, usernameEditText;
    private String selectedAvatar = null;
    private Button registerButton;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        registerButton = findViewById(R.id.registerButton);

        // Hilt ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Avatar selection
        int[] avatarIds = {R.id.barbarian, R.id.dwarf, R.id.fighter, R.id.healer, R.id.ninja};
        for (int id : avatarIds) {
            findViewById(id).setOnClickListener(v -> {
                selectedAvatar = String.valueOf(id); // or store resource name
                highlightSelectedAvatar(id, avatarIds);
            });
        }

        registerButton.setOnClickListener(v -> registerUser());

        // Observe registration status
        userViewModel.getRegistrationStatus().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Registration successful! Check your email to verify.", Toast.LENGTH_LONG).show();
                finish(); // go back to login
            } else {
                Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_LONG).show();
            }
        });

        // Observe registration error details for user-friendly feedback
        userViewModel.getRegistrationError().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void highlightSelectedAvatar(int selectedId, int[] avatarIds) {
        for (int id : avatarIds) {
            findViewById(id).setBackground(null);
        }
        findViewById(selectedId).setBackgroundColor(Color.LTGRAY);
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty() || selectedAvatar == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call UserViewModel to register
        userViewModel.registerUser(email, password, username, selectedAvatar);
    }
}
