package com.example.ysu.myapplication;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.DocumentsContract;
import android.util.Xml;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MessageService extends Service {
    private static final String SERVER_ADDRESS = "http://211.193.85.168";

    Handler mHandler;
    String meet_num;
    boolean mRunning = true;
    ArrayList<String> late_user_phone_list;
    private String number;

    public MessageService() {
        mHandler = new Handler();
        late_user_phone_list = new ArrayList<>();   // 늦은 사람들의 핸드폰 번호 리스트

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRunning) {
                    try {
                        late_user_phone_list.clear();

                        URL url = new URL(SERVER_ADDRESS + "/android/late_check.php?num=" + URLEncoder.encode(meet_num, "UTF-8"));
                        url.openStream();

                        getXMLData("/android/latecheckresult/latecheckresult" +
                                URLEncoder.encode(meet_num, "UTF-8") + ".xml");

                        if (late_user_phone_list.size() == 0) {
                            mRunning = false;
                            break;
                        }

                        for(int i=0; i<late_user_phone_list.size(); i++) {
                            number = late_user_phone_list.get(i);

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MessageService.this, number, Toast.LENGTH_SHORT).show();
                                }
                            });

                            Thread.sleep(3000);

                            Uri n = Uri.parse("smsto: " + number);
                            Intent intent = new Intent(Intent.ACTION_SENDTO, n);
                            intent.setPackage("com.example.ysu.myapplication");
                            intent.putExtra("sms_body", "회의가 시작하였습니다. 어서 오십시오.");
                            startService(intent);

                            url = new URL(SERVER_ADDRESS + "/android/sendupdate.php?num=" + URLEncoder.encode(meet_num, "UTF-8")
                                    + "&phone=" + URLEncoder.encode(number, "UTF-8"));
                            url.openStream();
                        }

                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "서비스 종료", Toast.LENGTH_SHORT).show();
        mRunning = false;
        super.onDestroy();
    }

    @Override
    public boolean stopService(Intent name) {
        Toast.makeText(this, "서비스 종료", Toast.LENGTH_SHORT).show();
        mRunning = false;
        return super.stopService(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {      // Main2Activity에서 인텐트를 넘겨받음.
        meet_num = intent.getStringExtra("meet_num");
        return startId;
    }

    public void getXMLData(String filename) {
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
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("phone")) {
                        number = xpp.nextText();
                        late_user_phone_list.add(number);
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        try {
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            Document doc = builder.parse(SERVER_ADDRESS + filename);
            Element root = doc.getDocumentElement();
            NodeList list = root.getElementsByTagName("phone");
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                Node temp = node.getFirstChild();
                String value = temp.getNodeValue();
                late_user_phone_list.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }

    private void sendMsgToActivity(int sendValue) {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("formService", sendValue);
            bundle.putString("test", "abcdefg");
            Message msg = Message.obtain(null, 4);
            msg.setData(bundle);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
