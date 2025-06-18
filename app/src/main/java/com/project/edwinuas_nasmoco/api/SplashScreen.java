package com.project.edwinuas_nasmoco.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.project.edwinuas_nasmoco.R;


public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView logo = findViewById(R.id.logoImageView);
        Animation blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink);
        logo.startAnimation(blinkAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Hentikan animasi kalau mau
                logo.clearAnimation();

                // Lanjut ke MainActivity
                Intent intent = new Intent(SplashScreen.this, login.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);
    }
}
