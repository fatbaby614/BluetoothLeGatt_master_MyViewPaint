package com.example.android.bluetoothlegatt;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by lifangping on 16/5/5.
 */
public class NextActivity extends Activity{

    private MyView dv;
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private String mDeviceAddress;
    // 定义接收坐标
    public String pointX;
    public String pointY;

    // 定义压感值
    public String pressValue;

    public int pointIntX;
    public int pointIntY;

    //定义压感值
    public int pressIntValue;

    public int screenWidth;//屏幕宽度

    public int screenHeight;//屏幕高度

    public int status_bar_height;//状态栏的高度

    public int navigation_bar_height;//导航栏的高度

    public int action_bar_height;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or noa
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //将数据显示在mDataField上
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                //System.out.println("data----" + data);
                //将坐标打印出来
                pointX = intent.getStringExtra(BluetoothLeService.POINTX);
                pointY = intent.getStringExtra(BluetoothLeService.POINTY);
                pressValue = intent.getStringExtra(BluetoothLeService.PRESS);
               // System.out.println("Next--pointX---" + pointX);
                //System.out.println("Next--pointY---" + pointY);
               // System.out.println("Next--pressValue---" + pressValue);

               // System.out.println("screenWidth:"+screenWidth);
               // System.out.println("screenHeight:"+screenHeight);
               // Log.i("BLE Tablet", "point:"+ pointX+ pointX);
                pointIntX = Integer.parseInt(pointX);
                pointIntY = Integer.parseInt(pointY);
                pressIntValue = Integer.parseInt(pressValue);

                dv.pointX = (float) (pointIntX*screenWidth *1.0/65536);
                dv.pointY = (float) (pointIntY*(screenHeight-status_bar_height-navigation_bar_height+20)*1.0/65536);


                dv.pressValue = pressIntValue;

            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        dv = new MyView(this);
        setContentView(dv);

//        mConnectionState = (TextView) findViewById(R.id.connection_state);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        //状态栏的高度
        status_bar_height = resources.getDimensionPixelSize(resourceId);
      //  System.out.println("status_bar_height:"+status_bar_height);

        int resourceId1 = resources.getIdentifier("navigation_bar_height","dimen", "android");
        //导航栏的高度
        navigation_bar_height = resources.getDimensionPixelSize(resourceId1);
       // System.out.println("navigation_bar_height:"+navigation_bar_height);




    }



    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
          //  Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolsmenu, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        dv.paint.setXfermode(null);		//取消擦除效果
//        dv.paint.setStrokeWidth(4);		//初始化笔的宽度
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.color_red:
                dv.paint.setColor(Color.RED);		//设置画笔的颜色为红色
                item.setChecked(true);
                break;
            case R.id.color_green:
                dv.paint.setColor(Color.GREEN);		//设置画笔的颜色为绿色
                item.setChecked(true);
                break;
            case R.id.color_blue:
                dv.paint.setColor(Color.BLUE);		//设置画笔的颜色为蓝色
                item.setChecked(true);
                break;
            case R.id.colo_purper:
                dv.paint.setColor(Color.MAGENTA);
                break;
            case R.id.width_1:
                dv.paint.setStrokeWidth(4);		//设置笔触的宽度为1像素
                break;
            case R.id.width_2:
                dv.paint.setStrokeWidth(8);		//设置笔触的宽度为2像素
                break;
            case R.id.width_3:
                dv.paint.setStrokeWidth(10);		//设置笔触的宽度为3像素
                break;
            case R.id.clear:
                dv.clear();						//擦除绘图
                break;
            case R.id.save:
                dv.save();						//保存绘图
                break;
            case R.id.erase:
                dv.erase();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
