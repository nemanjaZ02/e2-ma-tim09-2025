package com.e2_ma_tim09_2025.questify.activities.users;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.utils.QrCodeUtils;
import com.e2_ma_tim09_2025.questify.utils.BadgeMapper;
import com.e2_ma_tim09_2025.questify.viewmodels.OtherProfileViewModel;
import com.e2_ma_tim09_2025.questify.viewmodels.UserViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;



import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtherProfileActivity extends AppCompatActivity {
    private ShapeableImageView avatar;
    private TextView username, level, title, xp;
    private ImageView qrCode;
    private LinearLayout badgesContainer, equipmentContainer;

    private OtherProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        avatar = findViewById(R.id.otherProfileAvatar);
        username = findViewById(R.id.otherProfileUsername);
        level = findViewById(R.id.otherProfileLevel);
        title = findViewById(R.id.otherProfileTitle);
        xp = findViewById(R.id.otherProfileXP);
        qrCode = findViewById(R.id.otherProfileQRCode);
        badgesContainer = findViewById(R.id.otherProfileBadges);
        equipmentContainer = findViewById(R.id.otherProfileEquipment);

        viewModel = new ViewModelProvider(this).get(OtherProfileViewModel.class);
        String userId = getIntent().getStringExtra("userId");
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                displayUser(user);
            }
        });
        viewModel.fetchUser(userId);
        qrCode.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) qrCode.getDrawable()).getBitmap();
            String scannedUserId = decodeQRCode(bitmap);

            if (scannedUserId != null) {
                String myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (!scannedUserId.equals(myUserId)) {
                    viewModel.addFriend(myUserId, scannedUserId, task -> {
                        if (task.isSuccessful()) {
                            Boolean result = task.getResult();
                            if (result != null && result) {
                                Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "This user is already your friend!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Failed to add friend: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "You cannot add yourself as a friend!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to read QR code", Toast.LENGTH_SHORT).show();
            }
        });

        // Scan QR Code button functionality
        Button btnScanQRCode = findViewById(R.id.btnScanQRCode);
        btnScanQRCode.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan a QR code to add friend");
            integrator.setCameraId(0); // Use back camera
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        });

    }

    private void displayUser(User user) {
        // Debug logging
        System.out.println("DEBUG: Displaying user - " + user.getUsername());
        System.out.println("DEBUG: User badges count: " + (user.getBadges() != null ? user.getBadges().size() : 0));
        System.out.println("DEBUG: User equipment count: " + (user.getEquipment() != null ? user.getEquipment().size() : 0));
        
        username.setText(user.getUsername());
        level.setText("Level: " + user.getLevel());
        title.setText(user.getTitle());
        xp.setText("XP: " + user.getExperiencePoints());

        int avatarResId = getResources().getIdentifier(user.getAvatar(), "drawable", getPackageName());
        avatar.setImageResource(avatarResId != 0 ? avatarResId : R.drawable.ninja);

        // Generate QR code dynamically
        if (user.getQrCode() != null) {
            Bitmap qrBitmap = QrCodeUtils.generateQRCode(user.getQrCode(), 400); // 400x400 px
            if (qrBitmap != null) {
                qrCode.setImageBitmap(qrBitmap);
            }
        }

        // Load badges using BadgeMapper (same as ProfileActivity)
        badgesContainer.removeAllViews();
        if (user.getBadges() != null && !user.getBadges().isEmpty()) {
            for (String badge : user.getBadges()) {
                addBadgeToView(badge);
            }
        }

        // Load equipment using vertical layout like ProfileActivity
        equipmentContainer.removeAllViews();
        if (user.getEquipment() != null && !user.getEquipment().isEmpty()) {
            System.out.println("DEBUG: Loading " + user.getEquipment().size() + " equipment items");
            for (MyEquipment item : user.getEquipment()) {
                if (!item.isActivated())
                    continue;
                System.out.println("DEBUG: Loading equipment - ID: " + item.getEquipmentId() + ", Amount: " + item.getLeftAmount());
                addEquipmentItemToView(item);
            }
        } else {
            System.out.println("DEBUG: User has no equipment");
            // Show "No equipment" message
            TextView noEquipmentView = new TextView(this);
            noEquipmentView.setText("No equipment owned");
            noEquipmentView.setTextColor(getResources().getColor(R.color.black));
            noEquipmentView.setTextSize(16);
            noEquipmentView.setPadding(16, 16, 16, 16);
            noEquipmentView.setGravity(android.view.Gravity.CENTER);
            equipmentContainer.addView(noEquipmentView);
        }
    }
    
    /**
     * Add equipment item to the display with full details (similar to ProfileActivity)
     */
    private void addEquipmentItemToView(MyEquipment equipment) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(8, 8, 8, 8);
        
        // Equipment image
        ImageView itemImage = new ImageView(this);
        itemImage.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
        int drawableId = getDrawableIdForEquipment(equipment.getEquipmentId());
        if (drawableId != 0) {
            itemImage.setImageResource(drawableId);
            System.out.println("DEBUG: Loaded equipment image for: " + equipment.getEquipmentId());
        } else {
            itemImage.setImageResource(android.R.drawable.ic_menu_help);
            System.out.println("DEBUG: Using default equipment icon for: " + equipment.getEquipmentId());
        }
        itemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Equipment details
        LinearLayout detailsLayout = new LinearLayout(this);
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        detailsLayout.setPadding(12, 0, 0, 0);
        
        TextView nameView = new TextView(this);
        nameView.setText(equipment.getEquipmentId());
        nameView.setTextSize(16);
        nameView.setTextColor(getResources().getColor(R.color.black));
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        TextView amountView = new TextView(this);
        amountView.setText("Uses left: " + equipment.getLeftAmount());
        amountView.setTextSize(12);
        amountView.setTextColor(getResources().getColor(R.color.text_secondary));
        
        TextView activatedView = new TextView(this);
        activatedView.setText("Status: " + (equipment.isActivated() ? "Active" : "Inactive"));
        activatedView.setTextSize(10);
        activatedView.setTextColor(equipment.isActivated() ? 
            getResources().getColor(android.R.color.holo_green_dark) : 
            getResources().getColor(android.R.color.holo_red_dark));
        
        detailsLayout.addView(nameView);
        detailsLayout.addView(amountView);
        detailsLayout.addView(activatedView);
        
        itemLayout.addView(itemImage);
        itemLayout.addView(detailsLayout);
        
        equipmentContainer.addView(itemLayout);
    }
    
    /**
     * Add a badge to the badges container with image and tooltip (same as ProfileActivity)
     */
    private void addBadgeToView(String badgeString) {
        // Create a container for the badge (image + tooltip)
        LinearLayout badgeContainer = new LinearLayout(this);
        badgeContainer.setOrientation(LinearLayout.VERTICAL);
        badgeContainer.setPadding(4, 4, 4, 4); // Minimal padding for very compact layout
        badgeContainer.setGravity(android.view.Gravity.CENTER);
        
        // Create ImageView for the badge
        ImageView badgeImage = new ImageView(this);
        badgeImage.setLayoutParams(new LinearLayout.LayoutParams(80, 80)); // Smaller size for other profiles
        badgeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Get the drawable resource for the badge using BadgeMapper
        int badgeDrawableId = BadgeMapper.getBadgeDrawable(badgeString);
        if (badgeDrawableId != -1) {
            badgeImage.setImageResource(badgeDrawableId);
        } else {
            // Fallback to a default badge icon if the badge type is not found
            badgeImage.setImageResource(R.drawable.ic_badge_default);
        }
        
        // Set tooltip with badge name
        badgeImage.setContentDescription(BadgeMapper.getBadgeDisplayName(badgeString));
        
        // Add image to container
        badgeContainer.addView(badgeImage);
        
        // Add container to badges container
        badgesContainer.addView(badgeContainer);
    }
    
    /**
     * Get drawable ID for equipment based on equipment ID (same as ProfileActivity)
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
            case "Sword":
                return getResources().getIdentifier("ic_sword", "drawable", getPackageName());
            default:
                return 0; // No image found
        }
    }
    
    private void addFriendByQr(String scannedUserId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        if (scannedUserId != null && !scannedUserId.equals(currentUserId)) {
            viewModel.addFriend(currentUserId, scannedUserId, task -> {
                if (task.isSuccessful()) {
                    Boolean result = task.getResult();
                    if (result != null && result) {
                        Toast.makeText(this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "This user is already your friend!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to add friend: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        } else if (scannedUserId != null && scannedUserId.equals(currentUserId)) {
            Toast.makeText(this, "You cannot add yourself as a friend!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedUserId = result.getContents();
                addFriendByQr(scannedUserId);
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String decodeQRCode(Bitmap bitmap) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            com.google.zxing.Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText(); // This is the decoded text (userId)
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null; // QR code not found in bitmap
        }
    }
}