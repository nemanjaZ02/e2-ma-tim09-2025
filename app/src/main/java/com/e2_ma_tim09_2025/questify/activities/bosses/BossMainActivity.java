package com.e2_ma_tim09_2025.questify.activities.bosses;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.view.LayoutInflater;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.activities.tasks.TasksMainActivity;
import com.e2_ma_tim09_2025.questify.fragments.boss.ActivatedEquipmentFragment;
import com.e2_ma_tim09_2025.questify.fragments.boss.EquipmentSelectionFragment;
import com.e2_ma_tim09_2025.questify.fragments.boss.PotentialRewardsFragment;
import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.Equipment;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.viewmodels.BossViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.Random;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BossMainActivity extends AppCompatActivity implements SensorEventListener {

    private VideoView bossVideoView;
    private Button attackButton;
    private ProgressBar healthBar, ppBar;
    private TextView healthTextView, ppText, attacksLeftText;
    TextView hitChanceText;
    private TextView rewardText;
    private MaterialButton btnEquipment;
    private MaterialButton btnRewards;
    private boolean isPlayingAction = false;
    private Random random = new Random();
    private BossViewModel bossViewModel;
    private User currentUser;
    private Handler attackHandler = new Handler();
    private TextView hitResultText;
    private static final long BOSS_ATTACK_DELAY_MS = 4000;
    private boolean isBossDead = false;
    
    // Shake detection
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 15;
    private static final int SHAKE_SLOP_TIME_MS = 500;

    private boolean isChestActive = false;
    private boolean isChestOpen = false;
    private String rewardType = "";
    
    // Equipment selection
    private boolean hasShownEquipmentSelection = false;
    private MyEquipment selectedEquipment = null;
    
    // Rewards overlay
    private View overlayRewards;
    private TextView tvRewardsTitle;
    private TextView tvCoinsReward;
    private LinearLayout layoutEquipmentRewards;
    private LinearLayout layoutEquipmentList;
    private TextView tvNoEquipment;
    private MaterialButton btnContinue;
    private int coinsEarned = 0;

    private final Runnable bossAttackRunnable = new Runnable() {
        @Override
        public void run() {
            Integer health = bossViewModel.getCurrentHealth().getValue();
            if (!isPlayingAction && health != null && health > 0) {
                playRandomBossAttack();
            }
            attackHandler.postDelayed(this, BOSS_ATTACK_DELAY_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_boss_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bossVideoView = findViewById(R.id.bossVideoView);
        attackButton = findViewById(R.id.btnAttack);
        healthBar = findViewById(R.id.bossHealthBar);
        healthTextView = findViewById(R.id.healthTextView);
        ppBar = findViewById(R.id.playerPPBar);
        ppText = findViewById(R.id.playerPPText);
        attacksLeftText = findViewById(R.id.attacksLeftText);
        hitResultText = findViewById(R.id.hitResultText);
        hitChanceText = findViewById(R.id.hitChanceText);
        rewardText = findViewById(R.id.rewardText);
        btnEquipment = findViewById(R.id.btnEquipment);
        btnRewards = findViewById(R.id.btnRewards);
        
        // Initialize rewards overlay
        overlayRewards = findViewById(R.id.overlayRewards);
        tvRewardsTitle = overlayRewards.findViewById(R.id.tvRewardsTitle);
        tvCoinsReward = overlayRewards.findViewById(R.id.tvCoinsReward);
        layoutEquipmentRewards = overlayRewards.findViewById(R.id.layoutEquipmentRewards);
        layoutEquipmentList = overlayRewards.findViewById(R.id.layoutEquipmentList);
        tvNoEquipment = overlayRewards.findViewById(R.id.tvNoEquipment);
        btnContinue = overlayRewards.findViewById(R.id.btnContinue);
        
        // Setup continue button
        btnContinue.setOnClickListener(v -> {
            hideRewardsOverlay();
            navigateToTasksMain();
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        bossViewModel = new ViewModelProvider(this).get(BossViewModel.class);

        bossViewModel.getCurrentUserLiveData().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                ppBar.setMax(currentUser.getPowerPoints());
                ppBar.setProgress(currentUser.getPowerPoints());
                ppText.setText(currentUser.getPowerPoints() + " / " + currentUser.getPowerPoints());
            }
        });

        // Observe equipment reward results
        bossViewModel.getRewardMessage().observe(this, message -> {
            if (message != null) {
                showRewardMessage(message);
            }
        });
        
        // Observe rewarded equipment
        bossViewModel.getRewardedEquipment().observe(this, equipmentList -> {
            if (equipmentList != null && !equipmentList.isEmpty()) {
                updateRewardsOverlay(equipmentList);
            }
        });

        bossViewModel.getAttacksLeft().observe(this, attacks -> {
            attacksLeftText.setText("Attacks left: " + (attacks != null ? attacks : 0));
        });

        bossViewModel.getBoss().observe(this, boss -> {
            if (boss != null) {
                int maxHealth = boss.getMaxHealth();
                healthBar.setMax(maxHealth);

                Integer currentHealth = bossViewModel.getCurrentHealth().getValue();
                if (currentHealth != null) {
                    healthBar.setProgress(currentHealth);
                    healthTextView.setText(currentHealth + " / " + maxHealth);
                }

                hitChanceText.setText("Hit chance: " + (int) boss.getHitChance() + "%");
            }
        });

        bossViewModel.getCurrentHealth().observe(this, health -> {
            if (health != null) {
                healthBar.setProgress(health);
                Integer maxHealth = bossViewModel.getBoss().getValue() != null ?
                        bossViewModel.getBoss().getValue().getMaxHealth() : 0;
                healthTextView.setText(health + " / " + maxHealth);
            }
        });

        bossViewModel.getBossStatus().observe(this, status -> {
            if (status == BossStatus.DEFEATED) {
                attackButton.setEnabled(false);
                attackButton.setVisibility(View.GONE);
                healthBar.setVisibility(View.GONE);
                ppBar.setVisibility(View.GONE);
                healthTextView.setVisibility(View.GONE);
                attacksLeftText.setVisibility(View.GONE);
                ppText.setVisibility(View.GONE);
                hitChanceText.setVisibility(View.GONE);
                if (!isBossDead) {
                    playDeathAnimation();
                }
                isBossDead = true;
            } else if(status == BossStatus.INACTIVE) {
                attackButton.setEnabled(false);
                attackButton.setVisibility(View.GONE);
                healthBar.setVisibility(View.GONE);
                ppBar.setVisibility(View.GONE);
                healthTextView.setVisibility(View.GONE);
                attacksLeftText.setVisibility(View.GONE);
                ppText.setVisibility(View.GONE);
                hitChanceText.setVisibility(View.GONE);
            }
            else
            {
                // Show equipment selection before starting battle
                if (!hasShownEquipmentSelection) {
                    showEquipmentSelection();
                } else {
                    attackButton.setEnabled(true);
                    healthBar.setVisibility(View.VISIBLE);
                    ppBar.setVisibility(View.VISIBLE);
                    healthTextView.setVisibility(View.VISIBLE);
                    attacksLeftText.setVisibility(View.VISIBLE);
                    ppText.setVisibility(View.VISIBLE);
                }
            }
        });

        attackButton.setOnClickListener(v -> {
            if (currentUser != null && bossViewModel.getCurrentHealth().getValue() != null
                    && bossViewModel.getCurrentHealth().getValue() > 0) {
                bossViewModel.damage(currentUser.getPowerPoints(), new BossViewModel.OnDamageCompleteListener() {
                    @Override
                    public void onSuccess() {
                        showHitResult(true);
                        playRandomHitAnimation();

                        Integer attacksLeft = bossViewModel.getAttacksLeft().getValue();
                        if (attacksLeft != null && attacksLeft <= 0) {
                            // Out of attacks - check health and decide what to do
                            checkAttacksExhausted();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("BossArena", "Error: " + error);
                    }

                    @Override
                    public void onMiss() {
                        showHitResult(false);
                        Log.e("BossArena", "Chances not on your side... You missed!");

                        Integer attacksLeft = bossViewModel.getAttacksLeft().getValue();
                        if (attacksLeft != null && attacksLeft <= 0) {
                            // Out of attacks - check health and decide what to do
                            checkAttacksExhausted();
                        }
                    }
                });
            }
        });

        btnEquipment.setOnClickListener(v -> {
            showActivatedEquipment();
        });

        btnRewards.setOnClickListener(v -> {
            showPotentialRewards();
        });

        playIdleAnimation();
        startBossAttackTimer();
    }

    private void playDeathAnimation() {
        isPlayingAction = true;
        stopBossAttackTimer();
        attackButton.setEnabled(false);
        int deathNumber = random.nextInt(3) + 1;
        int resourceId = getResources().getIdentifier("boss1_death" + deathNumber, "raw", getPackageName());
        playOneShotAnimation(resourceId, true);
    }

    private void showHitResult(boolean hit) {
        hitResultText.setText(hit ? "HIT!" : "MISS!");
        hitResultText.setTextColor(hit ? Color.GREEN : Color.RED);
        hitResultText.setVisibility(View.VISIBLE);

        hitResultText.postDelayed(() -> hitResultText.setVisibility(View.GONE), 800);
    }

    private void playRandomBossAttack() {
        isPlayingAction = true;
        int attackNumber = random.nextInt(4) + 1;
        int resourceId = getResources().getIdentifier("boss1_attack" + attackNumber, "raw", getPackageName());
        playOneShotAnimation(resourceId, false);
    }

    private void startBossAttackTimer() {
        attackHandler.postDelayed(bossAttackRunnable, BOSS_ATTACK_DELAY_MS);
    }

    private void stopBossAttackTimer() {
        attackHandler.removeCallbacks(bossAttackRunnable);
    }

    private void playRandomHitAnimation() {
        isPlayingAction = true;
        int hitNumber = random.nextInt(2) + 1;
        int resourceId = getResources().getIdentifier("boss1_hit" + hitNumber, "raw", getPackageName());
        playOneShotAnimation(resourceId, false);
    }

    private void playIdleAnimation() {
        int resourceId = getResources().getIdentifier("boss1_idle", "raw", getPackageName());
        if (resourceId != 0) {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
            bossVideoView.stopPlayback();
            bossVideoView.setVideoURI(videoUri);
            bossVideoView.setOnCompletionListener(null);
            bossVideoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                bossVideoView.start();
            });
        }
    }

    private void playOneShotAnimation(int resourceId, boolean isDeathAnimation) {
        if (resourceId == 0) {
            isPlayingAction = false;
            if (bossViewModel.getCurrentHealth().getValue() != null && bossViewModel.getCurrentHealth().getValue() > 0) {
                playIdleAnimation();
            }
            return;
        }
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
        bossVideoView.stopPlayback();
        bossVideoView.setVideoURI(videoUri);
        bossVideoView.setOnCompletionListener(mp -> {
            isPlayingAction = false;
            if (isDeathAnimation) {
                // Boss died - call rewardUser and show chest
                bossViewModel.rewardUser();
                showChest();
                return;
            }
            if (bossViewModel.getCurrentHealth().getValue() != null && bossViewModel.getCurrentHealth().getValue() > 0) {
                playIdleAnimation();
            } else {
                playDeathAnimation();
            }
        });
        bossVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            bossVideoView.start();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bossVideoView != null && !bossVideoView.isPlaying()) {
            bossVideoView.start();
        }
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bossVideoView != null && bossVideoView.isPlaying()) {
            bossVideoView.pause();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBossAttackTimer();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            
            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
            
            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > SHAKE_SLOP_TIME_MS) {
                    lastShakeTime = currentTime;
                    
                    if (isChestActive && !isChestOpen) {
                        openChest();
                    } else if (!isChestActive && attackButton.isEnabled() && attackButton.getVisibility() == View.VISIBLE) {
                        attackButton.performClick();
                    }
                }
            }
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void showChest() {
        Log.d("BossMainActivity", "showChest() called");
        isChestActive = true;
        isChestOpen = false;

        Integer currentHealth = bossViewModel.getCurrentHealth().getValue();
        Integer maxHealth = bossViewModel.getMaxHealth();
        if (currentHealth != null && maxHealth > 0) {
            if (currentHealth <= 0) {
                rewardType = "FULL REWARD :)";
                coinsEarned = bossViewModel.getCoinsDrop();
            } else if (currentHealth <= maxHealth / 2) {
                rewardType = "HALF REWARD :/";
                coinsEarned = bossViewModel.getCoinsDrop() / 2;
            } else {
                rewardType = "NO REWARD :(";
                coinsEarned = 0;
            }
        }

        Log.d("BossMainActivity", "Reward type: " + rewardType);
        rewardText.setVisibility(View.VISIBLE);
        rewardText.setText(rewardType);

        playChestAnimation(false);
    }
    
    private void openChest() {
        if (!isChestActive || isChestOpen) return;
        
        isChestOpen = true;
        playChestAnimation(true);
    }
    
    private void playChestAnimation(boolean isOpen) {
        String resourceName = isOpen ? "chest1_open" : "chest1_closed";
        int resourceId = getResources().getIdentifier(resourceName, "raw", getPackageName());
        
        if (resourceId == 0) {
            Log.e("BossMainActivity", "Chest video resource not found: " + resourceName);
            return;
        }
        
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
        bossVideoView.stopPlayback();
        bossVideoView.setVideoURI(videoUri);
        
        bossVideoView.setOnCompletionListener(mp -> {
            if (!isOpen) {
                playChestAnimation(false);
            } else {
                showRewardsOverlay();
            }
        });
        
        bossVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(!isOpen);
            bossVideoView.start();
        });
    }
    
    private void checkAttacksExhausted() {
        // Hide UI elements
        stopBossAttackTimer();
        attackButton.setEnabled(false);
        attackButton.setVisibility(View.GONE);
        ppBar.setVisibility(View.GONE);
        healthBar.setVisibility(View.GONE);
        healthTextView.setVisibility(View.GONE);
        attacksLeftText.setVisibility(View.GONE);
        hitChanceText.setVisibility(View.GONE);
        
        // Call rewardUser
        bossViewModel.rewardUser();
        
        // Check health to decide what to do
        Integer currentHealth = bossViewModel.getCurrentHealth().getValue();
        Integer maxHealth = bossViewModel.getMaxHealth();
        
        if (currentHealth != null && maxHealth > 0) {
            if (currentHealth <= maxHealth / 2) {
                showChest();
            } 
        } else {
            navigateToTasksMain();
        }
    }
    
    private void navigateToTasksMain() {
        Intent intent = new Intent(this, TasksMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showEquipmentSelection() {
        hasShownEquipmentSelection = true;
        
        // Hide boss UI elements
        attackButton.setVisibility(View.GONE);
        healthBar.setVisibility(View.GONE);
        ppBar.setVisibility(View.GONE);
        healthTextView.setVisibility(View.GONE);
        attacksLeftText.setVisibility(View.GONE);
        ppText.setVisibility(View.GONE);
        hitChanceText.setVisibility(View.GONE);
        
        // Create and show equipment selection fragment
        EquipmentSelectionFragment fragment = EquipmentSelectionFragment.newInstance();
        fragment.setOnEquipmentSelectionCompleteListener(new EquipmentSelectionFragment.OnEquipmentSelectionCompleteListener() {
            @Override
            public void onEquipmentSelected(MyEquipment equipment) {
                selectedEquipment = equipment;
                hideEquipmentSelection();
                startBossBattle();
            }

            @Override
            public void onEquipmentSkipped() {
                selectedEquipment = null;
                hideEquipmentSelection();
                startBossBattle();
            }

            @Override
            public void onFragmentClosed() {
                // User closed the fragment, go back to previous activity
                finish();
            }
        });
        
        // Replace the current content with equipment selection fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();
    }
    
    private void hideEquipmentSelection() {
        // Remove the equipment selection fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
        if (currentFragment instanceof EquipmentSelectionFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }
    
    private void startBossBattle() {
        // Clear previous rewards
        bossViewModel.clearRewardedEquipment();
        
        // Show boss UI elements and enable battle
        attackButton.setEnabled(true);
        attackButton.setVisibility(View.VISIBLE);
        healthBar.setVisibility(View.VISIBLE);
        ppBar.setVisibility(View.VISIBLE);
        healthTextView.setVisibility(View.VISIBLE);
        attacksLeftText.setVisibility(View.VISIBLE);
        ppText.setVisibility(View.VISIBLE);
        hitChanceText.setVisibility(View.VISIBLE);
        btnEquipment.setVisibility(View.VISIBLE);
        btnRewards.setVisibility(View.VISIBLE);
        
        // Start boss attack timer
        startBossAttackTimer();
        
        // Log selected equipment for debugging
        if (selectedEquipment != null) {
            Log.d("BossMainActivity", "Selected equipment: " + selectedEquipment.getEquipmentId() + 
                  " (Amount left: " + selectedEquipment.getLeftAmount() + ")");
        } else {
            Log.d("BossMainActivity", "No equipment selected");
        }
    }
    
    private void showActivatedEquipment() {
        // Create and show activated equipment fragment
        ActivatedEquipmentFragment fragment = ActivatedEquipmentFragment.newInstance();
        fragment.setOnFragmentClosedListener(() -> {
            hideActivatedEquipment();
        });
        
        // Replace the current content with activated equipment fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();
    }
    
    private void hideActivatedEquipment() {
        // Remove the activated equipment fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
        if (currentFragment instanceof ActivatedEquipmentFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }
    
    private void showPotentialRewards() {
        // Create and show potential rewards fragment
        PotentialRewardsFragment fragment = PotentialRewardsFragment.newInstance();
        fragment.setOnFragmentClosedListener(() -> {
            hidePotentialRewards();
        });
        
        // Replace the current content with potential rewards fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();
    }
    
    private void hidePotentialRewards() {
        // Remove the potential rewards fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
        if (currentFragment instanceof PotentialRewardsFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }

    private void showRewardMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d("BossMainActivity", "Reward message: " + message);
    }
    
    private void showRewardsOverlay() {
        // Calculate coins earned based on boss health
        Integer currentHealth = bossViewModel.getCurrentHealth().getValue();
        Integer maxHealth = bossViewModel.getMaxHealth();
        
        if (currentHealth != null && maxHealth > 0) {
            if (currentHealth <= 0) {
                coinsEarned = bossViewModel.getCoinsDrop();
            } else if (currentHealth <= maxHealth / 2) {
                coinsEarned = bossViewModel.getCoinsDrop() / 2;
            } else {
                coinsEarned = 0;
            }
        }
        
        // Update coins display
        tvCoinsReward.setText(coinsEarned + " coins");
        
        // Show overlay
        overlayRewards.setVisibility(View.VISIBLE);
    }
    
    private void hideRewardsOverlay() {
        overlayRewards.setVisibility(View.GONE);
    }
    
    private void updateRewardsOverlay(List<Equipment> equipmentList) {
        if (equipmentList == null || equipmentList.isEmpty()) {
            layoutEquipmentRewards.setVisibility(View.GONE);
            tvNoEquipment.setVisibility(View.VISIBLE);
            return;
        }
        
        layoutEquipmentRewards.setVisibility(View.VISIBLE);
        tvNoEquipment.setVisibility(View.GONE);
        
        // Clear existing equipment views
        layoutEquipmentList.removeAllViews();
        
        // Add equipment views
        for (Equipment equipment : equipmentList) {
            View equipmentView = LayoutInflater.from(this).inflate(R.layout.item_reward_equipment, layoutEquipmentList, false);
            
            ImageView ivEquipmentIcon = equipmentView.findViewById(R.id.ivEquipmentIcon);
            TextView tvEquipmentName = equipmentView.findViewById(R.id.tvEquipmentName);
            TextView tvEquipmentType = equipmentView.findViewById(R.id.tvEquipmentType);
            
            // Set equipment icon
            setEquipmentIcon(ivEquipmentIcon, equipment.getId());
            
            // Set equipment name
            tvEquipmentName.setText(equipment.getName());
            
            // Set equipment type
            String typeText = equipment.getType().toString();
            tvEquipmentType.setText(typeText);
            
            layoutEquipmentList.addView(equipmentView);
        }
    }
    
    private void setEquipmentIcon(ImageView imageView, String equipmentId) {
        String resourceName = equipmentId.toLowerCase();
        int resourceId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
        
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
        } else {
            // Fallback to default icon
            imageView.setImageResource(R.drawable.ic_equipment_default);
        }
    }
}
