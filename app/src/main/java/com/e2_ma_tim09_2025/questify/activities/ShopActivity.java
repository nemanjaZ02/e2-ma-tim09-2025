package com.e2_ma_tim09_2025.questify.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;
import com.e2_ma_tim09_2025.questify.services.UserService;
import com.e2_ma_tim09_2025.questify.viewmodels.ShopViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ShopActivity extends AppCompatActivity {

    // Tab buttons
    private TextView tabPotions;
    private TextView tabClothes;
    private Button btnCloseShop;

    // Potions UI
    private ImageView imgPotion1, imgPotion2, imgPotion3, imgPotion4;
    private TextView tvPotion1Name;
    private TextView tvPotion2Name;
    private TextView tvPotion3Name;
    private TextView tvPotion4Name;
    private TextView tvHealthPotionPrice;
    private TextView tvStrengthElixirPrice;
    private TextView tvXPBoosterPrice;
    private TextView tvMysticPotionPrice;
    private Button btnPotion1, btnPotion2, btnPotion3, btnPotion4;

    // Clothes UI
    private ImageView imgGloves, imgShield, imgBoots;
    private TextView tvGlovesName;
    private TextView tvShieldName;
    private TextView tvBootsName;
    private TextView tvMagicGlovesPrice;
    private TextView tvIronShieldPrice;
    private TextView tvLeatherBootsPrice;
    private Button btnGloves, btnShield, btnBoots;

    // Data
    private ShopViewModel shopViewModel;
    
    @Inject
    EquipmentService equipmentService;
    
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        initializeViews();
        initializeViewModel();
        initializeCurrentUser();
        setupClickListeners();
        loadEquipment();
    }

    private void initializeViews() {
        // Tab buttons
        tabPotions = findViewById(R.id.tabPotions);
        tabClothes = findViewById(R.id.tabClothes);
        btnCloseShop = findViewById(R.id.btnCloseShop);

        // Potions UI
        imgPotion1 = findViewById(R.id.imgPotion1);
        imgPotion2 = findViewById(R.id.imgPotion2);
        imgPotion3 = findViewById(R.id.imgPotion3);
        imgPotion4 = findViewById(R.id.imgPotion4);
        tvPotion1Name = findViewById(R.id.tvPotion1Name);
        tvHealthPotionPrice = findViewById(R.id.tvHealthPotionPrice);
        tvPotion2Name = findViewById(R.id.tvPotion2Name);
        tvStrengthElixirPrice = findViewById(R.id.tvStrengthElixirPrice);
        tvPotion3Name = findViewById(R.id.tvPotion3Name);
        tvXPBoosterPrice = findViewById(R.id.tvXPBoosterPrice);
        tvPotion4Name = findViewById(R.id.tvPotion4Name);
        tvMysticPotionPrice = findViewById(R.id.tvMysticPotionPrice);
        btnPotion1 = findViewById(R.id.btnBuyHealthPotion);
        btnPotion2 = findViewById(R.id.btnBuyStrengthElixir);
        btnPotion3 = findViewById(R.id.btnBuyXPBooster);
        btnPotion4 = findViewById(R.id.btnBuyMysticPotion);

        // Clothes UI
        imgGloves = findViewById(R.id.imgGloves);
        imgShield = findViewById(R.id.imgShield);
        imgBoots = findViewById(R.id.imgBoots);
        tvGlovesName = findViewById(R.id.tvGlovesName);
        tvMagicGlovesPrice = findViewById(R.id.tvMagicGlovesPrice);
        tvShieldName = findViewById(R.id.tvShieldName);
        tvIronShieldPrice = findViewById(R.id.tvIronShieldPrice);
        tvBootsName = findViewById(R.id.tvBootsName);
        tvLeatherBootsPrice = findViewById(R.id.tvLeatherBootsPrice);
        btnGloves = findViewById(R.id.btnBuyMagicGloves);
        btnShield = findViewById(R.id.btnBuyIronShield);
        btnBoots = findViewById(R.id.btnBuyLeatherBoots);
    }

    private void initializeViewModel() {
        shopViewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        
        // Observe equipment data
        shopViewModel.getEquipmentList().observe(this, equipmentList -> {
            if (equipmentList != null) {
                updateEquipmentDisplay(equipmentList);
            }
        });
    }
    
    private void initializeCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }
    }

    private void setupClickListeners() {
        // Tab clicks
        tabPotions.setOnClickListener(v -> switchToPotions());
        tabClothes.setOnClickListener(v -> switchToClothes());
        btnCloseShop.setOnClickListener(v -> finish());

        // Buy buttons (no purchase logic for now)
        btnPotion1.setOnClickListener(v -> {});
        btnPotion2.setOnClickListener(v -> {});
        btnPotion3.setOnClickListener(v -> {});
        btnPotion4.setOnClickListener(v -> {});
        btnGloves.setOnClickListener(v -> {});
        btnShield.setOnClickListener(v -> {});
        btnBoots.setOnClickListener(v -> {});
    }

    private void loadEquipment() {
        shopViewModel.loadEquipment();
    }

    private void updateEquipmentDisplay(List<Equipment> equipmentList) {
        System.out.println("=== UPDATING EQUIPMENT DISPLAY ===");
        System.out.println("Total equipment loaded: " + equipmentList.size());

        // Filter equipment by type
        List<Equipment> potions = filterEquipmentByType(equipmentList, "POTION");
        List<Equipment> clothes = filterEquipmentByType(equipmentList, "CLOTHES");

        System.out.println("Potions found: " + potions.size());
        System.out.println("Clothes found: " + clothes.size());

        // Update potions display
        updatePotionsDisplay(potions);

        // Update clothes display
        updateClothesDisplay(clothes);
    }

    private List<Equipment> filterEquipmentByType(List<Equipment> equipmentList, String type) {
        return equipmentList.stream()
                .filter(equipment -> equipment.getType().toString().equals(type))
                .collect(java.util.stream.Collectors.toList());
    }

    private void updatePotionsDisplay(List<Equipment> potions) {
        // Ensure we have exactly 4 potions to display
        while (potions.size() < 4) {
            // Add placeholder potions if we don't have enough
            potions.add(null);
        }
        
        // Update potion 1
        Equipment potion1 = potions.get(0);
        updateEquipmentItem(potion1, tvPotion1Name, tvHealthPotionPrice, imgPotion1);

        // Update potion 2
        Equipment potion2 = potions.get(1);
        updateEquipmentItem(potion2, tvPotion2Name, tvStrengthElixirPrice, imgPotion2);

        // Update potion 3
        Equipment potion3 = potions.get(2);
        updateEquipmentItem(potion3, tvPotion3Name, tvXPBoosterPrice, imgPotion3);

        // Update potion 4
        Equipment potion4 = potions.get(3);
        updateEquipmentItem(potion4, tvPotion4Name, tvMysticPotionPrice, imgPotion4);
    }

    private void updateClothesDisplay(List<Equipment> clothes) {
        // Ensure we have exactly 3 clothes to display
        while (clothes.size() < 3) {
            // Add placeholder clothes if we don't have enough
            clothes.add(null);
        }
        
        // Update gloves
        Equipment gloves = clothes.get(0);
        updateEquipmentItem(gloves, tvGlovesName, tvMagicGlovesPrice, imgGloves);

        // Update shield
        Equipment shield = clothes.get(1);


        updateEquipmentItem(shield, tvShieldName, tvIronShieldPrice, imgShield);

        // Update boots
        Equipment boots = clothes.get(2);
        updateEquipmentItem(boots, tvBootsName, tvLeatherBootsPrice, imgBoots);
    }

    private void updateEquipmentItem(Equipment equipment, TextView nameView, TextView priceView, ImageView imageView) {
        if (equipment != null && currentUserId != null) {
            // Set equipment name
            nameView.setText(equipment.getName());
            
            // Set equipment image based on ID
            setEquipmentImage(imageView, equipment.getId());
            
            // Calculate and set price using EquipmentService
            equipmentService.getEquipmentPrice(currentUserId, equipment.getId(), priceTask -> {
                if (priceTask.isSuccessful()) {
                    double calculatedPrice = priceTask.getResult();
                    priceView.setText(String.valueOf((int)calculatedPrice));
                } else {
                    // Fallback to base price if calculation fails
                    priceView.setText(String.valueOf((int)equipment.getPrice()));
                }
            });
            
            System.out.println("Updated: " + equipment.getName() + " (ID: " + equipment.getId() + ")");
        } else {
            // Handle null equipment (placeholder slot) or no user
            nameView.setText("Coming Soon");
            priceView.setText("N/A");
            imageView.setImageResource(android.R.drawable.ic_menu_help); // Default placeholder image
            System.out.println("Updated: Placeholder slot or no user");
        }
    }

    private void setEquipmentImage(ImageView imageView, String equipmentId) {
        // Map equipment ID to drawable resource
        int drawableId = getDrawableIdForEquipment(equipmentId);
        if (drawableId != 0) {
            imageView.setImageResource(drawableId);
            System.out.println("Set image for " + equipmentId + " -> drawable ID: " + drawableId);
        } else {
            System.out.println("No image found for equipment ID: " + equipmentId);
        }
    }

    private int getDrawableIdForEquipment(String equipmentId) {
        // Map equipment IDs to drawable resources
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

    private void switchToPotions() {
        // Switch to potions tab
        tabPotions.setTextColor(0xFF5C4033); // Brown color
        tabClothes.setTextColor(getResources().getColor(android.R.color.white));
        
        // Show/hide appropriate content
        findViewById(R.id.potionsContainer).setVisibility(android.view.View.VISIBLE);
        findViewById(R.id.clothesContainer).setVisibility(android.view.View.GONE);
    }

    private void switchToClothes() {
        // Switch to clothes tab
        tabClothes.setTextColor(0xFF5C4033); // Brown color
        tabPotions.setTextColor(getResources().getColor(android.R.color.white));
        
        // Show/hide appropriate content
        findViewById(R.id.clothesContainer).setVisibility(android.view.View.VISIBLE);
        findViewById(R.id.potionsContainer).setVisibility(android.view.View.GONE);
    }
}
