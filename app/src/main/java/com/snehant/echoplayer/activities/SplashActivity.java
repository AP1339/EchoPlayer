package com.snehant.echoplayer.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import com.snehant.echoplayer.activities.HomeActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.snehant.echoplayer.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500;
    private CardView logoCard;
    private TextView txtLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views - Fixed the findViewById calls
        logoCard = findViewById(R.id.logoCard);
        txtLogo = findViewById(R.id.txtLogo);

        // Start animations
        animateLogo();

        // Navigate to HomeActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Fade out animation before transition
            fadeOutAndNavigate();

        }, SPLASH_DELAY);
    }

    private void animateLogo() {
        // Pulse animation for logo
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoCard, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoCard, "scaleY", 1f, 1.05f, 1f);

        scaleX.setDuration(800);
        scaleY.setDuration(800);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();

        // Rotation animation for music note
        ObjectAnimator rotation = ObjectAnimator.ofFloat(txtLogo, "rotation", 0f, 10f, -10f, 0f);
        rotation.setDuration(1200);
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotation.start();
    }

    private void fadeOutAndNavigate() {
        // Fade out the entire layout
        android.view.View rootView = findViewById(android.R.id.content);
        rootView.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                })
                .start();
    }
}