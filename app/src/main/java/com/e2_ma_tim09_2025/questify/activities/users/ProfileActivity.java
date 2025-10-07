package com.e2_ma_tim09_2025.questify.activities.users;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.alliance.CreateAllianceActivity;
import com.e2_ma_tim09_2025.questify.utils.BadgeMapper;
import com.e2_ma_tim09_2025.questify.activities.alliance.MemberAllianceActivity;
import com.e2_ma_tim09_2025.questify.activities.alliance.MyAllianceActivity;
import com.e2_ma_tim09_2025.questify.activities.ShopActivity;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.Equipment;
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
    private TextView profileUsername, profileTitleText, profileLevel, profilePowerPoints, profileXP, profileCoins, equipmentCount, badgeCount;
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
        equipmentCount = findViewById(R.id.equipmentCount);
        badgeCount = findViewById(R.id.badgeCount);
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
                // Load equipment details when user data is available
                viewModel.loadUserEquipmentDetails(user.getEquipment());
            }
        });
        
        // Observe equipment details with quantities
        viewModel.getUserEquipmentWithQuantities().observe(this, equipmentWithQuantities -> {
            if (equipmentWithQuantities != null) {
                displayEquipmentWithQuantities(equipmentWithQuantities);
            }
        });
        
        // Observe equipment count
        viewModel.getEquipmentCount().observe(this, count -> {
            if (count != null) {
                equipmentCount.setText("(" + count + " total items)");
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


        // Display badges as images
        List<String> badges = user.getBadges();
        badgesContainer.removeAllViews();
        
        // Update badge count
        int badgeCountValue = (badges != null) ? badges.size() : 0;
        badgeCount.setText("(" + badgeCountValue + " badges)");
        
        if (badges != null && !badges.isEmpty()) {
            for (String badge : badges) {
                addBadgeToView(badge);
            }
        }

        // Equipment display is now handled by ViewModel observers

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
    
    /**
     * Add a badge to the badges container with image and tooltip
     */
    private void addBadgeToView(String badgeString) {
        // Create a container for the badge (image + tooltip)
        LinearLayout badgeContainer = new LinearLayout(this);
        badgeContainer.setOrientation(LinearLayout.VERTICAL);
        badgeContainer.setPadding(4, 4, 4, 4); // Minimal padding for very compact layout
        badgeContainer.setGravity(android.view.Gravity.CENTER);
        
        // Create ImageView for the badge
        ImageView badgeImage = new ImageView(this);
        badgeImage.setLayoutParams(new LinearLayout.LayoutParams(120, 120)); // 120x120 dp - bigger badges
        badgeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Get the drawable resource for the badge
        int badgeDrawableId = BadgeMapper.getBadgeDrawable(badgeString);
        if (badgeDrawableId != -1) {
            badgeImage.setImageResource(badgeDrawableId);
        } else {
            // Fallback to a default badge icon if the badge type is not found
            badgeImage.setImageResource(android.R.drawable.ic_menu_help);
        }
        
        // Create TextView for the badge name (tooltip)
        TextView badgeName = new TextView(this);
        badgeName.setText(BadgeMapper.getBadgeDisplayName(badgeString));
        badgeName.setTextSize(12); // Slightly larger text to match bigger badges
        badgeName.setTextColor(getResources().getColor(R.color.text_secondary));
        badgeName.setGravity(android.view.Gravity.CENTER);
        badgeName.setPadding(0, 2, 0, 0); // Minimal padding for very compact layout
        
        // Add views to container
        badgeContainer.addView(badgeImage);
        badgeContainer.addView(badgeName);
        
        // Add container to badges container
        badgesContainer.addView(badgeContainer);
    }

    /**
     * Display equipment details from ViewModel
     */
    private void displayEquipmentWithQuantities(List<UserViewModel.EquipmentWithQuantity> equipmentWithQuantities) {
        equipmentContainer.removeAllViews();
        
        if (equipmentWithQuantities == null || equipmentWithQuantities.isEmpty()) {
            // Show "No equipment" message
            TextView noEquipmentView = new TextView(this);
            noEquipmentView.setText("No equipment owned");
            noEquipmentView.setTextColor(getResources().getColor(R.color.black));
            noEquipmentView.setTextSize(16);
            noEquipmentView.setPadding(16, 16, 16, 16);
            noEquipmentView.setGravity(android.view.Gravity.CENTER);
            equipmentContainer.addView(noEquipmentView);
            return;
        }
        
        // Display each unique equipment item with its quantity
        for (UserViewModel.EquipmentWithQuantity eq : equipmentWithQuantities) {
            addEquipmentItemWithQuantityToView(eq.equipment, eq.quantity);
        }
        
        // Add some extra padding at the bottom to ensure scrolling works
        View bottomPadding = new View(this);
        bottomPadding.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 
            20 // 20dp bottom padding
        ));
        equipmentContainer.addView(bottomPadding);
    }
    
    /**
     * Add equipment item to the display with full details
     */
    private void addEquipmentItemToView(Equipment equipment, int itemNumber) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(8, 8, 8, 8);
        
        // Equipment image
        ImageView itemImage = new ImageView(this);
        itemImage.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
        int drawableId = getDrawableIdForEquipment(equipment.getId());
        if (drawableId != 0) {
            itemImage.setImageResource(drawableId);
        } else {
            itemImage.setImageResource(android.R.drawable.ic_menu_help);
        }
        itemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Equipment details
        LinearLayout detailsLayout = new LinearLayout(this);
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        detailsLayout.setPadding(12, 0, 0, 0);
        
        TextView nameView = new TextView(this);
        nameView.setText(equipment.getName());
        nameView.setTextSize(16);
        nameView.setTextColor(getResources().getColor(R.color.black));
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        TextView typeView = new TextView(this);
        typeView.setText("Type: " + equipment.getType().toString());
        typeView.setTextSize(12);
        typeView.setTextColor(getResources().getColor(R.color.text_secondary));
        
        TextView itemNumberView = new TextView(this);
        itemNumberView.setText("Item #" + itemNumber);
        itemNumberView.setTextSize(10);
        itemNumberView.setTextColor(getResources().getColor(R.color.text_secondary));
        
        detailsLayout.addView(nameView);
        detailsLayout.addView(typeView);
        detailsLayout.addView(itemNumberView);
        
        itemLayout.addView(itemImage);
        itemLayout.addView(detailsLayout);
        
        equipmentContainer.addView(itemLayout);
    }
    
    /**
     * Add equipment item to the display with quantity
     */
    private void addEquipmentItemWithQuantityToView(Equipment equipment, int quantity) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(8, 8, 8, 8);
        
        // Equipment image
        ImageView itemImage = new ImageView(this);
        itemImage.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
        int drawableId = getDrawableIdForEquipment(equipment.getId());
        if (drawableId != 0) {
            itemImage.setImageResource(drawableId);
        } else {
            itemImage.setImageResource(android.R.drawable.ic_menu_help);
        }
        itemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Equipment details
        LinearLayout detailsLayout = new LinearLayout(this);
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        detailsLayout.setPadding(12, 0, 0, 0);
        
        TextView nameView = new TextView(this);
        nameView.setText(equipment.getName());
        nameView.setTextSize(16);
        nameView.setTextColor(getResources().getColor(R.color.black));
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        TextView typeView = new TextView(this);
        typeView.setText("Type: " + equipment.getType().toString());
        typeView.setTextSize(12);
        typeView.setTextColor(getResources().getColor(R.color.text_secondary));
        
        TextView quantityView = new TextView(this);
        quantityView.setText("Quantity: " + quantity);
        quantityView.setTextSize(12);
        quantityView.setTextColor(getResources().getColor(R.color.text_secondary));
        quantityView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        detailsLayout.addView(nameView);
        detailsLayout.addView(typeView);
        detailsLayout.addView(quantityView);
        
        itemLayout.addView(itemImage);
        itemLayout.addView(detailsLayout);
        
        equipmentContainer.addView(itemLayout);
    }
    
    
    /**
     * Get drawable ID for equipment based on equipment ID
     */
    private int getDrawableIdForEquipment(String equipmentId) {
        switch (equipmentId) {
            case "potion1":
                return getResources().getIdentifier("potion1", "drawable", getPackageName());
            case "potion2":
                return getResources().getIdentifier("potion2", "drawable", getPackageName());
            case "potion3":
                return getResources().getIdentifier("potion3", "drawable", getPackageName());
            case "potion4":
                return getResources().getIdentifier("potion4", "drawable", getPackageName());
            case "Gloves":
                return getResources().getIdentifier("gloves", "drawable", getPackageName());
            case "Shield":
                return getResources().getIdentifier("shield", "drawable", getPackageName());
            case "Boots":
                return getResources().getIdentifier("boots", "drawable", getPackageName());
            default:
                return 0; // No image found
        }
    }


}
