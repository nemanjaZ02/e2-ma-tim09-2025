package com.e2_ma_tim09_2025.questify.activities.bosses;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.models.enums.BossStatus;
import com.e2_ma_tim09_2025.questify.viewmodels.BossViewModel;

import java.util.Random;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BossMainActivity extends AppCompatActivity {

    private VideoView bossVideoView;
    private Button attackButton;
    private ProgressBar healthBar, ppBar;
    private TextView healthTextView, ppText, attacksLeftText;
    TextView hitChanceText;
    private boolean isPlayingAction = false;
    private Random random = new Random();
    private BossViewModel bossViewModel;
    private User currentUser;
    private Handler attackHandler = new Handler();
    private TextView hitResultText;
    private static final long BOSS_ATTACK_DELAY_MS = 4000;
    private boolean isBossDead = false;

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

        bossViewModel = new ViewModelProvider(this).get(BossViewModel.class);

        bossViewModel.getCurrentUserLiveData().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                ppBar.setMax(currentUser.getPowerPoints());
                ppBar.setProgress(currentUser.getPowerPoints());
                ppText.setText(currentUser.getPowerPoints() + " / " + currentUser.getPowerPoints());
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
                attackButton.setEnabled(true);
                healthBar.setVisibility(View.VISIBLE);
                ppBar.setVisibility(View.VISIBLE);
                healthTextView.setVisibility(View.VISIBLE);
                attacksLeftText.setVisibility(View.VISIBLE);
                ppText.setVisibility(View.VISIBLE);
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

                        Integer attacksLeft = bossViewModel.getAttacksLeft().getValue() - 1;
                        if (attacksLeft != null && attacksLeft <= 0) {
                            ppBar.setVisibility(View.GONE);
                            healthBar.setVisibility(View.GONE);
                            healthTextView.setVisibility(View.GONE);
                            attacksLeftText.setVisibility(View.GONE);
                            hitChanceText.setVisibility(View.GONE);
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

                        Integer attacksLeft = bossViewModel.getAttacksLeft().getValue() - 1;
                        if (attacksLeft != null && attacksLeft <= 0) {
                            bossViewModel.rewardUser();
                            // Sakrij UI elemente
                            ppBar.setVisibility(View.GONE);
                            healthBar.setVisibility(View.GONE);
                            healthTextView.setVisibility(View.GONE);
                            attacksLeftText.setVisibility(View.GONE);
                            hitChanceText.setVisibility(View.GONE);
                        }
                    }
                });
            }
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
            if (isDeathAnimation) return;
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bossVideoView != null && bossVideoView.isPlaying()) {
            bossVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBossAttackTimer();
    }
}
