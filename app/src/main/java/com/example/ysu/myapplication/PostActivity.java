package com.example.ysu.myapplication;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.Encoder;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

public class PostActivity extends AppCompatActivity {
    private static final String SERVER_ADDRESS = "http://211.193.85.168";
    String userId = new String();
    String boardId = new String();
    String boardTitle = new String();

    TextView posttext_id;
    TextView posttext_title;
    TextView posttext_content;

    Button modify_btn;
    Button delete_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent intent = getIntent();
        userId = intent.getStringExtra("id");
        boardId = intent.getStringExtra("BOARD_ID");
        boardTitle = intent.getStringExtra("BOARD_TITLE");

        posttext_id = findViewById(R.id.posttext_id);
        posttext_title = findViewById(R.id.posttext_title);
        posttext_content = findViewById(R.id.posttext_content);

        modify_btn = findViewById(R.id.modify_btn);
        delete_btn = findViewById(R.id.delete_btn);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_ADDRESS + "/android/post.php?board_id=" + URLEncoder.encode(boardId, "UTF-8"));
                    url.openStream();

                    getXmlData(boardId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        modify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostActivity.this, ModifyActivity.class);
                intent.putExtra("id", userId);
                intent.putExtra("board_id", boardId);
                intent.putExtra("board_title", posttext_title.getText().toString());
                intent.putExtra("board_content", posttext_content.getText().toString());
                startActivityForResult(intent, 0);
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        delete();
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_ADDRESS + "/android/post.php?board_id=" + URLEncoder.encode(boardId, "UTF-8"));
                    url.openStream();

                    getXmlData(boardId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getXmlData(String id) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            URL server = new URL(SERVER_ADDRESS+"/android/postresult/postresult"+URLEncoder.encode(id,"UTF-8")+".xml");
            InputStream inputStream = server.openStream();
            xpp.setInput(inputStream, "UTF-8");

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("board_id")) {
                        String board_Id = xpp.nextText();
                        Log.d("board_Id", board_Id);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                posttext_id.setText(boardId);
                            }
                        });
                    }
                    if (xpp.getName().equals("board_title")) {
                        final String title = xpp.nextText();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                posttext_title.setText(title);
                            }
                        });
                    }
                    if (xpp.getName().equals("board_content")) {
                        final String content = xpp.nextText();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                posttext_content.setText(content);
                            }
                        });
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void modify() {
        try {
            Intent intent = new Intent(PostActivity.this, ModifyActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("board_id", boardId);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        Log.d("userId", userId);
        Log.d("boardId", boardId);
        Log.d("boardTitle", boardTitle);
        try {
            URL url = new URL(SERVER_ADDRESS+"/android/delete.php?userId="+
                    URLEncoder.encode(userId, "UTF-8")+"&boardId="+
                    URLEncoder.encode(boardId, "UTF-8"));
            url.openStream();

            getDeleteResultXmlData(boardId);;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDeleteResultXmlData(String id) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            URL server = new URL(SERVER_ADDRESS+"/android/deleteresult/deleteresult"+URLEncoder.encode(id,"UTF-8")+".xml");
            InputStream inputStream = server.openStream();
            xpp.setInput(inputStream, "UTF-8");

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("check")) {
                        String check = xpp.nextText();
                        if (check.equals("pass")) {
                            finish();
                        } else if (check.equals("fail")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PostActivity.this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
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
