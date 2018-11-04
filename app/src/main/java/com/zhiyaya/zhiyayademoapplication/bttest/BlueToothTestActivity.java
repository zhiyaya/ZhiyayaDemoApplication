package com.zhiyaya.zhiyayademoapplication.bttest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.zhiyaya.zhiyayademoapplication.R;
import com.zhiyaya.zhiyayademoapplication.bttest.adapter.BluetoothDeviceListAdapter;

import java.util.ArrayList;
import java.util.List;

public class BlueToothTestActivity extends AppCompatActivity {
    private static final int offset = 10;
    private final static String TAG = BlueToothTestActivity.class.getSimpleName();
    private Button btn_search;
    private Button btn_clear;

    private TextView tv_text;
    private TextView tv_content;

    private RecyclerView rv_device_list;
    private BluetoothDeviceListAdapter adapter;
    BluetoothLeScanner bluetoothLeScanner;
    private boolean isScanning = false;

    private TestService testService;
    private List<TestBean> testBeans = new ArrayList<>();

    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_test);
        initView();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void startService(BluetoothDevice device) {
        //连接服务
        Intent intent = new Intent(this, TestService.class);
        intent.putExtra("address", device.getAddress());
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    public void stopScan() {
        bluetoothLeScanner.stopScan(scanCallback);
        bluetoothDevices.clear();
        isScanning = false;
        btn_search.setText("开始搜索");
        adapter.notifyDataSetChanged();
    }

    public void startScan() {
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(TestService.UUID_SERVICE)).build();
        filters.add(filter);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        bluetoothLeScanner.startScan(null, settings, scanCallback);
        isScanning = true;
        btn_search.setText("停止搜索");
    }

    private void initView() {
        btn_search = findViewById(R.id.btn_search);
        btn_clear = findViewById(R.id.btn_clear);
        tv_text = findViewById(R.id.tv_text);
        tv_content = findViewById(R.id.tv_content);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScanning) {
                    stopScan();
                } else {
                    startScan();
                }
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testBeans.clear();
                Intent intent = new Intent(BlueToothTestActivity.this, TestService.class);
                stopService(intent);
                setText("");
            }
        });

        rv_device_list = findViewById(R.id.rv_device_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv_device_list.setLayoutManager(linearLayoutManager);
        adapter = new BluetoothDeviceListAdapter(bluetoothDevices, this);
        rv_device_list.setAdapter(adapter);
        rv_device_list.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = offset;
                outRect.right = offset;
                outRect.bottom = offset;
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top = offset;
                }
            }
        });

        adapter.setOnItemClickListener(new BluetoothDeviceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, BluetoothDevice item) {
                stopScan();
                startService(item);
                btn_search.setText("已连接");
                btn_search.setEnabled(false);
            }
        });
    }

    private void setText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_text.setText(text);
            }
        });
    }

    private void setDataText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_content.setText(text);
            }
        });
    }

    private void refreshDevices(BluetoothDevice device) {
        boolean flag = true;
        for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
            if (bluetoothDevice.getAddress().equals(device.getAddress())) {
                flag = false;
                break;
            }
        }
        if (flag && isScanning) {
            bluetoothDevices.add(device);
            adapter.notifyDataSetChanged();
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult: ");
            refreshDevices(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: ");
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "onScanFailed: ");
            super.onScanFailed(errorCode);
        }
    };

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            testService = ((TestService.MsgBinder) service).getService();
            //注册回调接口来接收下载进度的变化
            testService.setOnDataCallBack(new OnDataCallBack() {
                @Override
                public void onDataReceive(TestBean testBean) {
                    testBeans.add(testBean);
                    setDataText(testBean.toString() + " " + testBeans.size());
                }

                @Override
                public void onConnectionStateChange(boolean isConnect) {
                    if (isConnect) {
                        setText("连接成功");
                    } else {
                        setText("连接失败");
                    }
                }
            });

        }
    };

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }
}
