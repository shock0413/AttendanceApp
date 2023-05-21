package com.example.ysu.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_ADDRESS = "http://211.193.85.168";

    EditText id_text, pw_text;
    ImageButton login_btn;
    CheckBox checkBox;
    ArrayList data;
    Thread loginThread;
    SharedPreferences loginInformation;     // 로그인 정보 담을 SharedPreferences 변수 선언
    SharedPreferences.Editor editor;        // 로그인 정보 받을 Editor 변수 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id_text = findViewById(R.id.idText);
        pw_text = findViewById(R.id.passwordText);
        login_btn = findViewById(R.id.login_btn);
        checkBox = findViewById(R.id.checkBox);
        data = new ArrayList<String>();
        loginInformation = getSharedPreferences("setting", Activity.MODE_PRIVATE);      // 이름은 setting
        editor = loginInformation.edit();           // SharedPreferences에 기록 가능하도록 함

        id_text.setImeOptions(EditorInfo.IME_ACTION_NEXT);  // 키보드의 엔터 키를 다음 키로 바꾸기
        id_text.setInputType(InputType.TYPE_CLASS_TEXT);    // 입력 모드를 텍스트로 바꾸기
        id_text.setOnKeyListener(new View.OnKeyListener() {     // 키보드의 키를 눌렀을 때
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) { // 엔터 키 눌렀을 때
                    pw_text.requestFocus();
                    return true;
                }
                return false;
            }
        });

        loginThread = new Thread(new Runnable() {   // 쓰레드의 재사용 불가함으로 인해 클릭할 때마다 초기화하도록 함
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_ADDRESS + "/android/search.php?"
                            + "id=" + URLEncoder.encode(id_text.getText().toString(), "UTF-8")
                            + "&password=" + URLEncoder.encode(pw_text.getText().toString(), "UTF-8"));
                    url.openStream();

                    String id = getXMLData("/android/searchresult.xml", "id");
                    String password = getXMLData("/android/searchresult.xml", "password");
                    String name = getXMLData("/android/searchresult.xml", "name");
                    if(id_text.getText().toString().equals(id) && pw_text.getText().toString().equals(password)) {
                        if (checkBox.isChecked()) {
                            editor.putBoolean("checkBox", true);
                            editor.putString("id", id_text.getText().toString());
                            editor.putString("password", pw_text.getText().toString());
                            editor.commit();
                        }
                        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                        intent.putExtra("id", id);
                        intent.putExtra("password", password);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        MainActivity.this.finish();
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "일치하는 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "인터넷이 연결되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
        });

        if (loginInformation.getString("id",null) != null && loginInformation.getString("password", null) != null) {    // SharedPreferences에 로그인 정보 내용 있을 시
            id_text.setText(loginInformation.getString("id", null));                     // 기본값은 null, SharedPreferences에 데이터가 있다면 그 데이터를 삽입
            pw_text.setText(loginInformation.getString("password", null));              // 기본값은 null, SharedPreferences에 데이터가 있다면 그 데이터를 삽입
            checkBox.setChecked(loginInformation.getBoolean("checkBox", false));        // 기본값은 false, SharedPreferences에 데이터가 있다면 그 데이터를 삽입
            loginThread.start();
        }

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*
                if(id_text.getText().toString().equals("") || pw_text.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for(int i=0; i<id_text.getText().toString().length(); i++) {
                    if(id_text.getText().toString().charAt(i) == ' ') {
                        Toast.makeText(MainActivity.this, "공백없이 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                for(int i=0; i<pw_text.getText().toString().length(); i++) {
                    if(pw_text.getText().toString().charAt(i) == ' ') {
                            Toast.makeText(MainActivity.this, "공백없이 입력해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                    }
                }

                if (loginThread.getState() == Thread.State.TERMINATED) {    // loginThread가 종료되면 초기화하도록 함. (실질적으론 두번째 버튼 클릭부터 적용됨)
                    loginThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(SERVER_ADDRESS + "/android/search.php?"
                                        + "id=" + URLEncoder.encode(id_text.getText().toString(), "UTF-8")
                                        + "&password=" + URLEncoder.encode(pw_text.getText().toString(), "UTF-8"));
                                url.openStream();
                                String id = getXMLData("/android/searchresult.xml", "id");
                                String password = getXMLData("/android/searchresult.xml", "password");
                                String name = getXMLData("/android/searchresult.xml", "name");
                                if (id_text.getText().toString().equals(id) && pw_text.getText().toString().equals(password)) {
                                    if (checkBox.isChecked()) {
                                        editor.putBoolean("checkBox", true);
                                        editor.putString("id", id_text.getText().toString());
                                        editor.putString("password", pw_text.getText().toString());
                                        editor.commit();
                                    }
                                    Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                                    intent.putExtra("id", id);
                                    intent.putExtra("password", password);
                                    intent.putExtra("name", name);
                                    startActivity(intent);
                                    MainActivity.this.finish();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "일치하는 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "인터넷이 연결되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                    loginThread.start();
                }

                if (loginThread.getState() == Thread.State.NEW) {   // loginThread 객체가 생성되었을 시
                    loginThread.start();
                }
                */
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
    }

    String getXMLData(String filename, String str) {
        String rss = SERVER_ADDRESS + "/";
        String ret = new String();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            URL server = new URL(rss + filename);
            InputStream is = server.openStream();
            xpp.setInput(is, "UTF-8");

            int eventType = xpp.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    if(xpp.getName().equals(str)) {
                        ret = xpp.nextText();
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }
}
