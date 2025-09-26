package com.e2_ma_tim09_2025.questify.activities.bosses;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.e2_ma_tim09_2025.questify.R;
import com.e2_ma_tim09_2025.questify.models.User;
import com.e2_ma_tim09_2025.questify.viewmodels.BossViewModel;

import java.util.Random;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BossMainActivity extends AppCompatActivity {

    private VideoView bossVideoView;
    private Button attackButton;
    private ProgressBar healthBar, ppBar;
    private TextView healthTextView, ppText, attacksLeftText;
    private boolean isPlayingAction = false;
    private Random random = new Random();
    private BossViewModel bossViewModel;
    private User currentUser;
    private Handler attackHandler = new Handler();
    private static final long BOSS_ATTACK_DELAY_MS = 4000;

    private Runnable bossAttackRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPlayingAction && bossViewModel.getCurrentHealth().getValue() > 0) {
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

        bossViewModel = new ViewModelProvider(this).get(BossViewModel.class);

        currentUser = bossViewModel.getCurrentUser();

        ppBar.setMax(currentUser.getPowerPoints());
        ppBar.setProgress(currentUser.getPowerPoints());
        attacksLeftText.setText("Attacks left: " + 5);
        healthBar.setMax(bossViewModel.getMaxHealth());
        bossViewModel.getCurrentHealth().observe(this, health -> {
            healthBar.setProgress(health);
            String healthText = health + " / " + bossViewModel.getMaxHealth();
            healthTextView.setText(healthText);
            if (health <= 0) {
                if (!isPlayingAction) {
                    Log.d("BossArena", "Boss Killed!");
                    playDeathAnimation();
                }
            }
        });

        playIdleAnimation();
        startBossAttackTimer();

        attackButton.setOnClickListener(v -> {
            if (!isPlayingAction && bossViewModel.getCurrentHealth().getValue() > 0) {
                bossViewModel.damage(currentUser.getPowerPoints());
                int remainingAttacks = bossViewModel.attackUsed();
                attacksLeftText.setText("Attacks left: " + remainingAttacks);
                playRandomHitAnimation();
            } else {
                Log.d("BossArena", "Boss is occupied.");
            }
        });
    }

    private void playDeathAnimation() {
        isPlayingAction = true;

        stopBossAttackTimer();
        attackButton.setEnabled(false);

        int deathNumber = random.nextInt(3) + 1;
        String clipName = "boss1_death" + deathNumber;

        Log.d("BossArena", "BOSS DEAD. " + clipName);

        int resourceId = getResources().getIdentifier(
                clipName,
                "raw",
                getPackageName()
        );

        playOneShotAnimation(resourceId, true);
    }

    private void playRandomBossAttack() {
        isPlayingAction = true;

        int attackNumber = random.nextInt(4) + 1;
        String clipName = "boss1_attack" + attackNumber;

        Log.d("BossArena", "BOSS ATTACK! " + clipName);

        int resourceId = getResources().getIdentifier(
                clipName,
                "raw",
                getPackageName()
        );

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
        String clipName = "boss1_hit" + hitNumber;

        Log.d("BossArena", "ATTACK! Boss hit! Anim: " + clipName);

        int resourceId = getResources().getIdentifier(
                clipName,
                "raw",
                getPackageName()
        );

        playOneShotAnimation(resourceId, false);
    }

    private void playIdleAnimation() {
        int resourceId = getResources().getIdentifier(
                "boss1_idle",
                "raw",
                getPackageName()
        );

        if (resourceId != 0) {
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);

            bossVideoView.stopPlayback();
            bossVideoView.setVideoURI(videoUri);

            bossVideoView.setOnCompletionListener(null);

            bossVideoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                bossVideoView.start();
            });

        } else {
            Log.e("BossArena", "ERROR: boss1_idle video not found in res/raw.");
        }
    }

    private void playOneShotAnimation(int resourceId, boolean isDeathAnimation) {
        if (resourceId == 0) {
            Log.e("BossArena", "Animation ID not found.");
            isPlayingAction = false;
            if (bossViewModel.getCurrentHealth().getValue() > 0) {
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
                Log.d("BossArena", "Death animacija završena. Kraj borbe.");
            } else {
                if (bossViewModel.getCurrentHealth().getValue() > 0) {
                    playIdleAnimation();
                } else {
                    playDeathAnimation();
                }
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
}