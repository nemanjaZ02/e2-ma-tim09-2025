package com.e2_ma_tim09_2025.questify.activities.users;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.alliance.CreateAllianceActivity;
import com.e2_ma_tim09_2025.questify.activities.alliance.MemberAllianceActivity;
import com.e2_ma_tim09_2025.questify.activities.alliance.MyAllianceActivity;
import com.e2_ma_tim09_2025.questify.activities.ShopActivity;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.e2_ma_tim09_2025.questify.utils.QrCodeUtils;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {

    private ImageView profileAvatar,qrCodeImage;
    private TextView profileUsername, profileTitleText, profileLevel, profilePowerPoints, profileXP, profileCoins;
    private LinearLayout badgesContainer, equipmentContainer;
    private LinearLayout changePasswordContainer;
    private EditText editOldPassword, editNewPassword, editConfirmPassword;
    private Button btnChangePassword, btnSavePassword;


    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private UserViewModel viewModel;

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
        changePasswordContainer = findViewById(R.id.changePasswordContainer);
        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        qrCodeImage = findViewById(R.id.qrCodePlaceholder); // make this an ImageView in XML


        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Observe user data from ViewModel
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                bindUserData(user);
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            if (changePasswordContainer.getVisibility() == View.GONE) {
                changePasswordContainer.setVisibility(View.VISIBLE);
            } else {
                changePasswordContainer.setVisibility(View.GONE);
            }
        });

        // Save password button
        btnSavePassword.setOnClickListener(v -> {
            String oldPassword = editOldPassword.getText().toString().trim();
            String newPassword = editNewPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();

            // Call ViewModel method
            viewModel.changePassword(oldPassword, newPassword, confirmPassword);
        });

        // Observe result from ViewModel
        viewModel.getChangePasswordResult().observe(this, status -> {
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            if ("Password updated successfully".equals(status)) {
                // Hide password form after success
                changePasswordContainer.setVisibility(View.GONE);
                editOldPassword.setText("");
                editNewPassword.setText("");
                editConfirmPassword.setText("");
            }
        });
        ImageView menuIcon = findViewById(R.id.profileMenuIcon);

        menuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ProfileActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_all_users) {
                    startActivity(new Intent(ProfileActivity.this, AllUsersActivity.class));
                    return true;
                } else if (id == R.id.menu_friends) {
                    startActivity(new Intent(ProfileActivity.this, FriendsActivity.class));
                    return true;
                } else if (id == R.id.menu_create_alliance) {
                    startActivity(new Intent(ProfileActivity.this, CreateAllianceActivity.class));
                    return true;
                } else if (id == R.id.menu_my_alliance) {
                    startActivity(new Intent(ProfileActivity.this, MyAllianceActivity.class));
                    return true;
                } else if (id == R.id.menu_member_alliance) {
                    startActivity(new Intent(ProfileActivity.this, MemberAllianceActivity.class));
                    return true;
                } else if (id == R.id.menu_shop) {
                    startActivity(new Intent(ProfileActivity.this, ShopActivity.class));
                    return true;
                } else {
                    return false;
                }
            });

            popup.show();
        });


        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning from other activities (like Shop)
        loadUserData();
    }

    private void loadUserData() {
        // Use ViewModel to refresh current user data
        viewModel.refreshCurrentUser();
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
        List<MyEquipment> equipment = user.getEquipment();
        equipmentContainer.removeAllViews();
        if (equipment != null && !equipment.isEmpty()) {
            for (MyEquipment eq : equipment) {
                TextView eqView = new TextView(this);
                //eqView.setText(eq);
                eqView.setTextColor(getResources().getColor(R.color.black));
                eqView.setPadding(8, 8, 8, 8);
                equipmentContainer.addView(eqView);
            }
        }

        if (user.getQrCode() != null) {
            Bitmap qrBitmap = QrCodeUtils.generateQRCode(user.getQrCode(), 400); // 400x400 px
            if (qrBitmap != null) {
                qrCodeImage.setImageBitmap(qrBitmap);
            }
        }
    }
    private int getDrawableFromAvatarName(String avatarName) {
        int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
        return resId != 0 ? resId : R.drawable.ninja;
    }


}
