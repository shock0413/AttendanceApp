package com.example.ysu.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingActivity extends AppCompatActivity {

    private Button logout_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        logout_btn = findViewById(R.id.logout_btn);

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
                SharedPreferences loginInformation = getSharedPreferences("setting", Activity.MODE_PRIVATE);        // SharedPreferences는 모든 액티비티에 적용되기 때문에
                SharedPreferences.Editor editor = loginInformation.edit();                                      // 앞전에 사용했던 내용을 넣어주면 그대로 사용할 수 있으므로
                editor.clear();                                                                                 // MainActivity 액티비티에서 자동 로그인하지 못하도록 SharedPreferences를 비워준다.
                editor.commit();                                                                                // 비운 내용을 저장시킨다.
                ActivityCompat.finishAffinity(SettingActivity.this);            // 스택에 쌓인 액티비티를 깔끔하게 종료시켜준다.
                finish();
            }
        });

    }
}
