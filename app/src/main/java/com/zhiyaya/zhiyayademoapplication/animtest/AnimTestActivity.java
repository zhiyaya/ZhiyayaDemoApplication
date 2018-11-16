package com.zhiyaya.zhiyayademoapplication.animtest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.zhiyaya.zhiyayademoapplication.R;
import com.zhiyaya.zhiyayademoapplication.bttest.OnDataCallBack;
import com.zhiyaya.zhiyayademoapplication.bttest.TestBean;
import com.zhiyaya.zhiyayademoapplication.bttest.TestService;

import java.util.ArrayList;
import java.util.List;

public class AnimTestActivity extends AppCompatActivity {
    private final static String TAG = AnimTestActivity.class.getSimpleName();

    private ImageView iv_anim;
    private TestService testService;
    private List<TestBean> testBeans = new ArrayList<>();

    private TextView tv_light;
    private TextView tv_temp;
    private TextView tv_water;
    private TextView tv_air;

    private int continuouslyTouch = 0;
    private int lastType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim_test);

        tv_light = findViewById(R.id.tv_light);
        tv_temp = findViewById(R.id.tv_temp);
        tv_water = findViewById(R.id.tv_water);
        tv_air = findViewById(R.id.tv_air);

        iv_anim = findViewById(R.id.iv_anim);
        iv_anim.setImageResource(R.drawable.sleep_anim);
        AnimationDrawable animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
        animationDrawable.start();

        String address = getIntent().getStringExtra("address");
        startService(address);
    }

    private void startService(String address) {
        //连接服务
        Intent intent = new Intent(this, TestService.class);
        intent.putExtra("address", address);
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);
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
                    refresh(testBean);
                }

                @Override
                public void onConnectionStateChange(boolean isConnect) {

                }
            });

        }
    };

    private void refresh(TestBean testBean) {
        testBeans.add(testBean);
        while (testBeans.size() > 100) {
            testBeans.remove(0);
        }
        Log.i(TAG, "refresh: " + testBean.toString());
        tv_light.setText(String.valueOf(testBean.getLight()));
        tv_air.setText(String.valueOf(testBean.getAir()));
        tv_water.setText(String.valueOf(testBean.getHumi()));
        tv_temp.setText(String.valueOf(testBean.getT()));
        //setDataText(testBean.toString() + " " + testBeans.size());
        if (testBeans.size() > 1) {
            TestBean old = testBeans.get(testBeans.size() - 2);
            if (old.getTouch() < testBean.getTouch()) {
                if (continuouslyTouch <= 6) {
                    continuouslyTouch += 2;
                }
                if (continuouslyTouch > 5) {
                    setAnim(2);
                } else {
                    setAnim(1);
                }
            } else {
                if (continuouslyTouch > 0) {
                    continuouslyTouch--;
                }
                if (continuouslyTouch == 0) {
                    setAnim(0);
                }

            }
        }
        Log.i(TAG, "refresh: continuouslyTouch" + continuouslyTouch);
    }

    private void setAnim(final int type) {
        if (type == lastType) {
            return;
        }
        lastType = type;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_anim.setVisibility(View.VISIBLE);
                AnimationDrawable animationDrawable;
                switch (type) {
                    case 1:
                        iv_anim.setImageResource(R.drawable.blink_anim);
                        animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
                        break;
                    case 2:
                        iv_anim.setImageResource(R.drawable.excite_anim);
                        animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
                        break;
                    default:
                        iv_anim.setImageResource(R.drawable.sleep_anim);
                        animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
                        break;
                }
                animationDrawable.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        Intent intent = new Intent(this, TestService.class);
        stopService(intent);
        super.onDestroy();
    }
}
