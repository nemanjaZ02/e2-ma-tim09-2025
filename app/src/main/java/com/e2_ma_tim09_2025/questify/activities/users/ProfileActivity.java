package com.e2_ma_tim09_2025.questify.activities.users;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {

    private ImageView profileAvatar;
    private TextView profileUsername, profileTitleText, profileLevel, profilePowerPoints, profileXP, profileCoins;
    private LinearLayout badgesContainer, equipmentContainer;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views
        profileAvatar = findViewById(R.id.profileAvatar);
        profileUsername = findViewById(R.id.profileUsername);
        profileTitleText = findViewById(R.id.profileTitleText);
        profileLevel = findViewById(R.id.profileLevel);
        profilePowerPoints = findViewById(R.id.profilePowerPoints);
        profileXP = findViewById(R.id.profileXP);
        profileCoins = findViewById(R.id.profileCoins);
        badgesContainer = findViewById(R.id.badgesContainer);
        equipmentContainer = findViewById(R.id.equipmentContainer);

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) bindUserData(user);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }

    private void bindUserData(User user) {
        profileUsername.setText(user.getUsername());
        profileTitleText.setText(user.getTitle());
        profileLevel.setText("Level: " + user.getLevel());
        profilePowerPoints.setText("Power Points: " + user.getPowerPoints());
        profileXP.setText("Experience Points: " + user.getExperiencePoints());
        profileCoins.setText("Coins: " + user.getCoins());

        // Load avatar image with Glide or directly
        String avatarName = user.getAvatar(); // this should be the stored string name
        int drawableResId = getDrawableFromAvatarName(avatarName);
        profileAvatar.setImageResource(drawableResId);


        // Placeholder badges (for now just TextViews)
        List<String> badges = user.getBadges();
        badgesContainer.removeAllViews();
        if (badges != null && !badges.isEmpty()) {
            for (String badge : badges) {
                TextView badgeView = new TextView(this);
                badgeView.setText(badge);
                badgeView.setTextColor(getResources().getColor(R.color.black));
                badgeView.setPadding(8, 8, 8, 8);
                badgesContainer.addView(badgeView);
            }
        }

        // Placeholder equipment (for now just TextViews)
        List<String> equipment = user.getEquipment();
        equipmentContainer.removeAllViews();
        if (equipment != null && !equipment.isEmpty()) {
            for (String eq : equipment) {
                TextView eqView = new TextView(this);
                eqView.setText(eq);
                eqView.setTextColor(getResources().getColor(R.color.black));
                eqView.setPadding(8, 8, 8, 8);
                equipmentContainer.addView(eqView);
            }
        }

        // QR code placeholder (you can later generate it using a library like ZXing)
    }
    private int getDrawableFromAvatarName(String avatarName) {
        int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
        return resId != 0 ? resId : R.drawable.ninja;
    }


}
