package com.example.ysu.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.URL;
import java.net.URLEncoder;

public class WriteActivity extends AppCompatActivity {

    private static final String SERVER_ADDRESS = "http://211.193.85.168";

    private String user_id = new String();

    private EditText text_title;
    private EditText text_content;

    private Button submit_btn;
    private Button cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        Intent intent = getIntent();

        user_id = intent.getStringExtra("id");

        text_title = findViewById(R.id.text_title);
        text_content = findViewById(R.id.text_content);

        submit_btn = findViewById(R.id.submit_btn);
        cancel_btn = findViewById(R.id.cancel_btn);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("content", text_content.getText().toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        write();
                    }
                }).start();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void write() {
        try {
            Log.d("id", user_id);
            URL url = new URL(SERVER_ADDRESS + "/android/write.php?title=" +
                    URLEncoder.encode(text_title.getText().toString(), "UTF-8") + "&content=" +
                    URLEncoder.encode(text_content.getText().toString(), "UTF-8") + "&id=" +
                    URLEncoder.encode(user_id, "UTF-8"));
            url.openStream();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
