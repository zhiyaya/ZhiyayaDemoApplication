package com.zhiyaya.zhiyayademoapplication.bttest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.zhiyaya.zhiyayademoapplication.R;

import java.util.ArrayList;
import java.util.List;

public class BlueToothTestActivity extends AppCompatActivity {
    private final static String TAG = BlueToothTestActivity.class.getSimpleName();
    private Button btn_search;
    private Button btn_send;

    private TextView tv_text;
    private TextView tv_content;

    private TestService testService;
    private List<TestBean> testBeans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_test);
        initView();
        Intent intent = new Intent(this, TestService.class);
        //连接服务
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    private void initView() {
        btn_search = findViewById(R.id.btn_search);
        btn_send = findViewById(R.id.btn_send);
        tv_text = findViewById(R.id.tv_text);
        tv_content = findViewById(R.id.tv_content);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testBeans.clear();
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
