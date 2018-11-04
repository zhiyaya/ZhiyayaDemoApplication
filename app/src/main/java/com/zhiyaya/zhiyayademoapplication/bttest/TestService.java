package com.zhiyaya.zhiyayademoapplication.bttest;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class TestService extends Service {
    public final static UUID UUID_SERVICE = UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131");
    public final static UUID UUID_NOTIFY = UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131");
    public final static UUID UUID_NOTIFY_ = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_WRITE = UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131");

    private final static String TAG = BlueToothTestActivity.class.getSimpleName();
    private boolean isConnect = false;
    private StringBuilder stringBuilder = new StringBuilder();
    private Gson gson = new Gson();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private boolean isListenOn = false;

    private Timer timer = new Timer(true);

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Log.i(TAG, "TimerTask: " + isListenOn);
            if (isListenOn) {
                byte[] bytes = new byte[]{0x05, 0x02, 0x00, 0x00};
                write(bytes);
            }
        }
    };

    public TestService() {
    }

    private void write(byte[] byteArray) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || writeCharacteristic == null) {
            return;
        } else {
            byte[] valueByte = byteArray;
            writeCharacteristic.setValue(valueByte);
            mBluetoothGatt.writeCharacteristic(writeCharacteristic);
            stringBuilder = new StringBuilder();
        }
    }

    private void refreshData() {
        String s = stringBuilder.toString().trim();
        Log.d(TAG, "refreshData: " + s);
        if (s.endsWith("}")) {
            try {
                TestBean testBean = gson.fromJson(stringBuilder.toString(), TestBean.class);
                if (testBean != null) {
                    if (onDataCallBack != null) {
                        onDataCallBack.onDataReceive(testBean);
                    }
                    stringBuilder = new StringBuilder();
                }
            } catch (Exception e) {

            }
        }
    }

    private BluetoothGattCallback bluetoothGattCallbackOne = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    isConnect = true;
                    if (onDataCallBack != null) {
                        onDataCallBack.onConnectionStateChange(true);
                    }
                    //搜索Service
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    isConnect = false;
                    if (onDataCallBack != null) {
                        onDataCallBack.onConnectionStateChange(false);
                    }
                }
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //根据UUID获取Service中的Characteristic,并传入Gatt中
            bluetoothGattService = gatt.getService(UUID_SERVICE);
            readCharacteristic = bluetoothGattService.getCharacteristic(UUID_NOTIFY);
            mBluetoothGatt.readCharacteristic(readCharacteristic);
            writeCharacteristic = bluetoothGattService.getCharacteristic(UUID_WRITE);
            final int charaProp = readCharacteristic.getProperties();

            //Toast.makeText(BlueToothTestActivity.this, " " + charaProp, Toast.LENGTH_SHORT).show();

            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {

            }
            boolean isConnect = gatt.setCharacteristicNotification(readCharacteristic, true);
            BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(UUID_NOTIFY_);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean success = mBluetoothGatt.writeDescriptor(descriptor);
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {//数据改变
            super.onCharacteristicChanged(gatt, characteristic);
            String data = new String(characteristic.getValue());
            if (data.equals("OK")) {
                stringBuilder = new StringBuilder();
            } else if (data.contains("ERR")) {
                Log.e(TAG, "onCharacteristicChangedError: " + data);
            } else {
                stringBuilder.append(data);
                refreshData();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "发送成功");
            }
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //开启监听成功，可以像设备写入命令了
                Log.e(TAG, "开启监听成功");
                isListenOn = true;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //获取BluetoothAdapter
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //如果蓝牙没有打开 打开蓝牙
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String address = intent.getStringExtra("address");
        Log.d(TAG, "address: " + address);
        BluetoothDevice bluetoothDeviceOne = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothGatt = bluetoothDeviceOne.connectGatt(TestService.this, true, bluetoothGattCallbackOne);
        timer.schedule(task, 2000, 2000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        if (isConnect) {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
            }
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    private OnDataCallBack onDataCallBack = null;

    public void setOnDataCallBack(OnDataCallBack onDataCallBack) {
        this.onDataCallBack = onDataCallBack;
    }

    public class MsgBinder extends Binder {

        public TestService getService() {
            return TestService.this;
        }
    }
}
