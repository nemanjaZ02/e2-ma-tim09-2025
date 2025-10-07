package com.e2_ma_tim09_2025.questify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.bosses.BossMainActivity;
import com.e2_ma_tim09_2025.questify.activities.tasks.TasksMainActivity;
import com.e2_ma_tim09_2025.questify.activities.users.LoginActivity;
import com.e2_ma_tim09_2025.questify.activities.users.RegisterActivity;
import com.e2_ma_tim09_2025.questify.repositories.EquipmentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.HashMap;
import java.util.Map;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button tasksButton = findViewById(R.id.tasks_button);
        Button registerButton = findViewById(R.id.btnRegister);
        Button loginButton = findViewById(R.id.btnLogin);

        tasksButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TasksMainActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is logged in → go directly to TasksMainActivity
            Intent intent = new Intent(this, TasksMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // prevent back navigation
            startActivity(intent);
            finish();
        }
    }
}