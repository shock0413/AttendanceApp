package com.example.ysu.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;

import static java.lang.Thread.sleep;

public class IntroActivity extends AppCompatActivity {

    ProgressBar mProgressBar;
    TextView textView;
    ImageView imageView;
    GlideDrawableImageViewTarget gifImage;

    Thread progressThread, glideThread;

    int mProgressStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        gifImage = new GlideDrawableImageViewTarget(imageView);
        Glide.with(IntroActivity.this).load(R.drawable.intro).listener(new RequestListener<Integer, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(final GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                glideThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                if (resource.isRunning()) {
                                    resource.setLoopCount(1);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                glideThread.start();
                return false;
            }
        }).into(gifImage);

        progressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(mProgressStatus<100) {
                    try {
                        progressThread.sleep(40);
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(mProgressStatus+"%");
                            }
                        });
                        mProgressBar.setProgress(mProgressStatus);
                        mProgressStatus++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        progressThread.start();
    }


}
