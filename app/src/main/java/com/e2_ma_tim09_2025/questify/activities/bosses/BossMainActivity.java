package com.e2_ma_tim09_2025.questify.activities.bosses;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.e2_ma_tim09_2025.questify.R;

import java.util.Random;

public class BossMainActivity extends AppCompatActivity {

    private VideoView bossVideoView;
    private Button attackButton;
    private boolean isPlayingAction = false;
    private Random random = new Random();

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

        playIdleAnimation();

        attackButton.setOnClickListener(v -> {
            if (!isPlayingAction) {
                playRandomHitAnimation();
            } else {
                Log.d("BossArena", "Boss je trenutno zauzet i ne može biti pogođen.");
            }
        });
    }

    private void playRandomHitAnimation() {
        isPlayingAction = true;

        int hitNumber = random.nextInt(2) + 1;
        String clipName = "boss1_hit" + hitNumber;

        Log.d("BossArena", "ATTACK! Boss pogođen! Puštam: " + clipName);

        int resourceId = getResources().getIdentifier(
                clipName,
                "raw",
                getPackageName()
        );

        playOneShotAnimation(resourceId);
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

    private void playOneShotAnimation(int resourceId) {
        if (resourceId == 0) {
            Log.e("BossArena", "Neuspešno puštanje animacije: ID nije pronađen.");
            isPlayingAction = false;
            playIdleAnimation();
            return;
        }

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);

        bossVideoView.stopPlayback();
        bossVideoView.setVideoURI(videoUri);

        bossVideoView.setOnCompletionListener(mp -> {
            isPlayingAction = false;
            playIdleAnimation();
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