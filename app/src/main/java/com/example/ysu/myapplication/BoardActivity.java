package com.example.ysu.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.zip.Inflater;

public class BoardActivity extends AppCompatActivity {
    private static final String SERVER_ADDRESS = "http://211.193.85.168";

    private ListView listView;
    private Button write_btn;
    private MyAdapter myAdapter;
    private String board_id;
    private String board_title;
    private String board_date;
    private String board_userid;
    private String user_id;
    private int count = 0;      // 게시글의 번호를 나타내기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("id");

        listView = findViewById(R.id.listView);
        write_btn = findViewById(R.id.write_btn);

        myAdapter = new MyAdapter();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(BoardActivity.this, PostActivity.class);
                intent.putExtra("id", user_id);
                intent.putExtra("BOARD_ID", myAdapter.getItem(i).boardId);
                intent.putExtra("BOARD_TITLE", myAdapter.getItem(i).boardTitle);
                startActivity(intent);
            }
        });

        write_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BoardActivity.this, WriteActivity.class);
                intent.putExtra("id", user_id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {         // onCreate() 후에 실행되고 다른 액티비티 전환 후 돌아왔을 때 실행되는 생명주기 함수
        super.onResume();
        myAdapter.clear();              // 리스트뷰를 비워주기 위해 어댑터의 리스트를 비워준다.
        new Thread(new Runnable() {
            @Override
            public void run() { // 서버와 통신으로 정보들을 가져온다.
                try {
                    URL url = new URL(SERVER_ADDRESS+"/android/board.php");
                    url.openStream();

                    getXMLData("/android/boardresult.xml", "board_id", "board_title", "board_date", "user_id");
                    count = myAdapter.getCount();   // 최상단의 게시글의 번호

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listView.setAdapter(myAdapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void getXMLData(String filename, String id, String title, String date, String userId) {
        String rss = SERVER_ADDRESS + "/";
        String ret = new String();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            URL server = new URL(rss+filename);
            InputStream inputStream = server.openStream();
            xpp.setInput(inputStream, "UTF-8");

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                MyItem myItem = new MyItem();
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals(id)) {
                        board_id = xpp.nextText();
                    }
                    if (xpp.getName().equals(title)) {
                        board_title = xpp.nextText();
                    }
                    if (xpp.getName().equals(date)) {
                        board_date = xpp.nextText();
                    }
                    if (xpp.getName().equals(userId)) {
                        board_userid = xpp.nextText();
                        myItem.boardId = board_id;
                        myItem.boardTitle = board_title;
                        myItem.boardDate = board_date;
                        myItem.boardUserId = board_userid;
                        Log.d("boardId", board_id);
                        Log.d("boardTitle", board_title);
                        myAdapter.addItem(myItem);
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyAdapter extends BaseAdapter {
        private ArrayList<MyItem> myItems;
        private LayoutInflater inflater;

        public MyAdapter() {
            super();
            myItems = new ArrayList<MyItem>();
            inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return myItems.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public MyItem getItem(int i) {
            return myItems.get(i);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                view = inflater.inflate(R.layout.listview_custom, null);
            }

            TextView text_id = view.findViewById(R.id.tv_id);
            TextView text_title = view.findViewById(R.id.tv_title);
            TextView text_userId = view.findViewById(R.id.tv_userId);
            TextView text_date = view.findViewById(R.id.tv_date);

            text_id.setText(count+"");                         // 최상단부터 1까지
            text_title.setText(myItems.get(i).boardTitle);
            text_userId.setText(myItems.get(i).boardUserId);
            text_date.setText(myItems.get(i).boardDate);

            count--;
            return view;
        }

        public void addItem(MyItem myItem) {
            myItems.add(myItem);
        }

        public void clear() {
            myItems.clear();
        }
    }

    class MyItem {
        String boardId = new String();
        String boardTitle = new String();
        String boardUserId = new String();
        String boardDate = new String();
    }
}