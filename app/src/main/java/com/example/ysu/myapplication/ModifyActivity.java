package com.example.ysu.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

public class ModifyActivity extends AppCompatActivity {

    private static final String SERVER_ADDRESS = "http://211.193.85.168";

    private EditText title_text;
    private EditText content_text;
    private Button submit_btn;
    private Button cancel_btn;

    private String userId;
    private String boardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);

        title_text = findViewById(R.id.text_title);
        content_text = findViewById(R.id.text_content);
        submit_btn = findViewById(R.id.submit_btn);
        cancel_btn = findViewById(R.id.cancel_btn);

        Intent intent = getIntent();
        userId = intent.getStringExtra("id");
        boardId = intent.getStringExtra("board_id");
        title_text.setText(intent.getStringExtra("board_title"));
        content_text.setText(intent.getStringExtra("board_content"));

        Log.d("userId", userId);
        Log.d("boardId", boardId);

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        submit();
                    }
                }).start();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void submit() {
        try {
            URL url = new URL(SERVER_ADDRESS+"/android/modify.php?userId=" +
                    URLEncoder.encode(userId, "UTF-8")+"&boardId=" +
                    URLEncoder.encode(boardId, "UTF-8")+"&boardTitle=" +
                    URLEncoder.encode(title_text.getText().toString(), "UTF-8")+"&boardContent=" +
                    URLEncoder.encode(content_text.getText().toString(), "UTF-8"));
            Log.d("url", url.toString());
            url.openStream();

            getModifyResultXmlData(boardId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getModifyResultXmlData(String id) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            URL server = new URL(SERVER_ADDRESS+"/android/modifyresult/modifyresult"+URLEncoder.encode(id,"UTF-8")+".xml");
            InputStream inputStream = server.openStream();
            xpp.setInput(inputStream, "UTF-8");

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("check")) {
                        String check = xpp.nextText();
                        if (check.equals("pass")) {
                            setResult(1);
                            finish();
                        } else if (check.equals("fail")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ModifyActivity.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
