package com.example.ysu.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

public class MypageActivity extends AppCompatActivity {

    private static final String SERVER_ADDRESS = "http://211.193.85.168";

    EditText edit_name;
    EditText edit_phone;
    EditText edit_pw;
    EditText edit_pw_confirm;
    Button btn_confirm;
    Button btn_cancel;

    private String user_id;
    private String user_name;
    private String user_phone;
    private String user_pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        edit_name = findViewById(R.id.text_name);
        edit_phone = findViewById(R.id.text_phone);
        edit_pw = findViewById(R.id.text_pw);
        edit_pw_confirm = findViewById(R.id.text_pw_confirm);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_cancel = findViewById(R.id.btn_cancel);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_ADDRESS + "/android/getuser.php?id=" + URLEncoder.encode(user_id, "UTF-8"));
                    url.openStream();

                    user_name = getXMLData("android/getuserresult/result(" + user_id + ").xml", "user_name");
                    user_pw = getXMLData("android/getuserresult/result(" + user_id + ").xml", "user_pw");
                    user_phone = getXMLData("android/getuserresult/result(" + user_id + ").xml", "user_phone");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            edit_name.setText(user_name);
                            edit_pw.setText(user_pw);
                            edit_pw_confirm.setText(user_pw);
                            edit_phone.setText(user_phone);
                        }
                    });
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_name.equals("") || edit_pw.equals("") || edit_pw_confirm.equals("") || edit_phone.equals("")) {
                    Toast.makeText(MypageActivity.this, "입력 칸을 모두 채워주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!edit_pw.getText().toString().equals(edit_pw_confirm.getText().toString())) {
                    Toast.makeText(MypageActivity.this, "비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(SERVER_ADDRESS + "/android/userupdate.php?id=" + URLEncoder.encode(user_id, "UTF-8") + "&name=" +
                                            URLEncoder.encode(edit_name.getText().toString(), "UTF-8") + "&password=" + URLEncoder.encode(edit_pw.getText().toString(), "UTF-8") +
                                            "&phone=" + URLEncoder.encode(edit_phone.getText().toString(), "UTF-8"));
                            url.openStream();
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
