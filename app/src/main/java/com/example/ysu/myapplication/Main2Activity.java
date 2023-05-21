package com.example.ysu.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

public class Main2Activity extends AppCompatActivity {
    String user_id, user_name;
    private static final String SERVER_ADDRESS = "http://211.193.85.168";
    private static final String BLUETOOTH_ADDRESS = "34:03:DE:3E:E4:3A";
    // F4:E1:1E:15:BB:80 (집)
    // 34:03:DE:3E:E4:3A (학교)
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    boolean mConnected = false;
    private boolean mScanning;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;

    private final static int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    Button logout_btn;
    ImageButton attend_btn;
    ImageButton board_btn;
    ImageButton mypage_btn;
    ImageButton setting_btn;

    private String meet_num;
    private int mLeaderChecked = isNotLeaderChecked;
    private final static int isNotLeaderChecked = 0;
    private final static int isLeaderChecked = 1;

    private Intent msgService;  // 회의에 늦는 사람들에게 문자 메세지를 보내기 위한 서비스 실행 인텐트

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            mBluetoothLeService.connect(BLUETOOTH_ADDRESS);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;

                mBluetoothLeService.connect(BLUETOOTH_ADDRESS);
                updateAttendance(meet_num);
                Toast.makeText(Main2Activity.this, "출석되었습니다.", Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            mBluetoothLeService.disconnect();
                            scanLeDevice(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mHandler = new Handler();

        Intent intent = getIntent();
        user_id = intent.getStringExtra("id");
        user_name = intent.getStringExtra("name");

        attend_btn = findViewById(R.id.attendance);
        board_btn = findViewById(R.id.board);
        mypage_btn = findViewById(R.id.mypage);
        setting_btn = findViewById(R.id.setting);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){                     // 현재 OS가 마쉬멜로우 이상일 때
            // Android M Permission check
            if(this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){  // 위치 권한이 허용되어 있지 않으면 알림창을 띄어서 확인을 누르면 권한 요청
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton("Ok", null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(Main2Activity.this, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();

            }
        }

        attend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this,AttendActivity.class);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        board_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this,BoardActivity.class);
                intent.putExtra("id", user_id);
                startActivity(intent);
            }
        });

        mypage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this,MypageActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (device.getAddress().equals(BLUETOOTH_ADDRESS)) {
                                scanLeDevice(false);
                                Intent gattServiceIntent = new Intent(Main2Activity.this, BluetoothLeService.class);
                                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                            }
                        }
                    });
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        if (mBluetoothAdapter != null) {
            isAttendance();
        }
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null) {
            scanLeDevice(false);
        }
        mLeDeviceListAdapter.clear();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter != null)
            scanLeDevice(false);
        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
        // stopService(msgService);
    }

    public void isAttendance() {       // 출석 여부를 데이터베이스를 통해 확인
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(SERVER_ADDRESS + "/android/attd_check.php?id="+URLEncoder.encode(user_id, "UTF-8"));
                    url.openStream();
                    meet_num = getXMLData("/android/attd_check.xml", "meet_num");

                    final String attd_check = getXMLData("/android/attd_check.xml", "check");

                    if (attd_check.equals("YES")) {
                        scanLeDevice(false);
                    } else if (attd_check.equals("NO")) {
                        scanLeDevice(true);
                    }

                    /*if (!meet_num.equals(""))
                        leaderCheck();*/

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    protected void updateAttendance(final String meet_num) {        // 데이터베이스에 출석 여부를 'yes'로 바꾸기
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_ADDRESS + "/android/update.php?id="+URLEncoder.encode(user_id, "UTF-8")
                            +"&num="+URLEncoder.encode(meet_num, "UTF-8"));
                    url.openStream();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private class LeDeviceListAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        public int getCount() {
            return mLeDevices.size();
        }

        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        public long getItemId(int i) {
            return i;
        }
    }

    public void leaderCheck() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_ADDRESS + "/android/leader_check.php?num=" +
                            URLEncoder.encode(meet_num,"UTF-8")+"&id=" + URLEncoder.encode(user_id, "UTF-8"));
                    url.openStream();

                    final String leader_check = getXMLData("/android/leaderresult/check_result" + URLEncoder.encode(meet_num, "UTF-8") + ".xml", "leader_check");

                    if (leader_check.equals("YES")) {
                        mLeaderChecked = isLeaderChecked;
                        msgService = new Intent(Main2Activity.this, MessageService.class);
                        msgService.putExtra("meet_num", meet_num);
                        startService(msgService);
                    }
                    else if (leader_check.equals("NO")) {
                        mLeaderChecked = isNotLeaderChecked;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int reqeustCode, String permission[], int[] grantResults){
        switch (reqeustCode){
            case PERMISSION_REQUEST_COARSE_LOCATION:{                       // 권한 요청이 위치 권한일 때
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){       // 권한을 허용한다
                    Log.d("permission", "coarse location permission granted");
                }else{
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, " +
                            "this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton("Ok", null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
}