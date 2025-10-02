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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.utils.QrCodeUtils;
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

import javax.annotation.Nullable;

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
                    viewModel.addFriend(myUserId, scannedUserId);
                    Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to read QR code", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void displayUser(User user) {
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

        // Load badges
        badgesContainer.removeAllViews();
        for (String badge : user.getBadges()) {
            ImageView badgeView = new ImageView(this);
            badgeView.setImageResource(getResources().getIdentifier(badge, "drawable", getPackageName()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
            params.setMargins(8, 0, 8, 0);
            badgeView.setLayoutParams(params);
            badgesContainer.addView(badgeView);
        }

        // Load equipment
        equipmentContainer.removeAllViews();
        for (MyEquipment item : user.getEquipment()) {
            ImageView itemView = new ImageView(this);
            //itemView.setImageResource(getResources().getIdentifier(item, "drawable", getPackageName()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
            params.setMargins(8, 0, 8, 0);
            itemView.setLayoutParams(params);
            equipmentContainer.addView(itemView);
        }
    }
    private void addFriendByQr(String scannedUserId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Call ViewModel or Service
        viewModel.addFriend(currentUserId, scannedUserId);

        Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show();
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