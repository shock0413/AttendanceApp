package com.example.ysu.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class AttendActivity extends AppCompatActivity {

    private static final String SERVER_ADDRESS = "http://211.193.85.168";

    private TextView textView;
    private ListView listView;
    private String user_id;
    private String user_name;
    private MyAdapter myAdapter;
    String meet_date;
    String meet_name;
    String meet_start;
    String meet_end;
    String meet_check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend);

        textView = findViewById(R.id.textView);
        listView = findViewById(R.id.listView);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        user_name = intent.getStringExtra("user_name");

        textView.setText(user_name + "님의 출석현황입니다.");

        myAdapter = new MyAdapter();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_ADDRESS + "/android/attdlist.php?id=" + URLEncoder.encode(user_id, "UTF-8"));
                    url.openStream();

                    getXMLData("android/attdlistresult/attdlistresult(" + URLEncoder.encode(user_id, "UTF-8") + ").xml");

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

    public void getXMLData(String filename) {
        String rss = SERVER_ADDRESS + "/";
        String ret = new String();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            URL server = new URL(rss + filename);
            InputStream inputStream = server.openStream();
            xpp.setInput(inputStream, "UTF-8");

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                MyItem myItem = new MyItem();
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("meet_date")) {
                        meet_date = xpp.nextText();
                        Log.d("meet_date",meet_date);
                    }
                    if (xpp.getName().equals("meet_name")) {
                        meet_name = xpp.nextText();
                    }
                    if (xpp.getName().equals("meet_start")) {
                        meet_start = xpp.nextText();
                    }
                    if (xpp.getName().equals("meet_end")) {
                        meet_end = xpp.nextText();
                    }
                    if (xpp.getName().equals("meet_check")) {
                        meet_check = xpp.nextText();
                        myItem.meet_date = meet_date;
                        myItem.meet_name = meet_name;
                        myItem.meet_start = meet_start;
                        myItem.meet_end = meet_end;
                        myItem.meet_check = meet_check;
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
            myItems = new ArrayList<>();
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
        public View getView(int i, View view, ViewGroup parent) {

            if (view == null) {
                view = inflater.inflate(R.layout.listview_custom2, null);
            }

            TextView text_date = view.findViewById(R.id.tv_date);
            TextView text_name = view.findViewById(R.id.tv_name);
            TextView text_start = view.findViewById(R.id.tv_start);
            TextView text_end = view.findViewById(R.id.tv_end);
            TextView text_check = view.findViewById(R.id.tv_check);

            text_date.setText(myItems.get(i).meet_date);
            text_name.setText(myItems.get(i).meet_name);
            text_start.setText(myItems.get(i).meet_start);
            text_end.setText(myItems.get(i).meet_end);
            text_check.setText(myItems.get(i).meet_check);

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
        String meet_date;
        String meet_name;
        String meet_start;
        String meet_end;
        String meet_check;
    }
}
