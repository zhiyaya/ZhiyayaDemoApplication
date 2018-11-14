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
            Log.v(TAG, "TimerTask: " + isListenOn);
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

    private TestBean verifyData(byte[] bytes) {
        if (bytes[0] != 0x05 || bytes[1] != 0x0e || bytes[2] != 0x02) {
            return null;
        }
        TestBean testBean = new TestBean();
        testBean.setLight((bytes[4] & 0xFF) << 8 | (bytes[3] & 0xFF));
        testBean.setT((float) (((bytes[6] & 0xFF) << 8 | (bytes[5] & 0xFF)) / 100.0));
        testBean.setHumi((bytes[8] & 0xFF) << 8 | (bytes[7] & 0xFF));
        testBean.setF((bytes[10] & 0xFF) << 8 | (bytes[9] & 0xFF));
        testBean.setAir((bytes[12] & 0xFF) << 8 | (bytes[11] & 0xFF));
        testBean.setTouch((bytes[14] & 0xFF) << 8 | (bytes[13] & 0xFF));
        return testBean;
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
            byte[] bytes = characteristic.getValue();
            String data = new String(bytes);
            if (data.contains("ERR")) {
                Log.e(TAG, "onCharacteristicChangedError: " + data);
            } else {
                Log.v(TAG, "data: " + bytesToHexString(bytes));
                TestBean testBean = verifyData(bytes);
                if (onDataCallBack != null && testBean != null) {
                    onDataCallBack.onDataReceive(testBean);
                }
            }
        }

        public String bytesToHexString(byte[] bArr) {
            StringBuffer sb = new StringBuffer(bArr.length);
            String sTmp;

            for (int i = 0; i < bArr.length; i++) {
                sb.append(i).append(":");
                sTmp = Integer.toHexString(0xFF & bArr[i]);
                if (sTmp.length() < 2)
                    sb.append(0);
                sb.append(sTmp.toUpperCase()).append(" ");
            }

            return sb.toString();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "发送成功");
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
        timer.schedule(task, 2000, 500);
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
