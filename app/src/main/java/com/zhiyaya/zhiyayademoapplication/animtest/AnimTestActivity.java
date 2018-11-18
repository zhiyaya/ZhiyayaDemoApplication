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
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zhiyaya.zhiyayademoapplication.R;
import com.zhiyaya.zhiyayademoapplication.bttest.OnDataCallBack;
import com.zhiyaya.zhiyayademoapplication.bttest.TestBean;
import com.zhiyaya.zhiyayademoapplication.bttest.TestService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimTestActivity extends AppCompatActivity {
    private final static String TAG = AnimTestActivity.class.getSimpleName();

    RelativeLayout rl_back;
    private ImageView iv_anim;
    private TestService testService;
    private List<TestBean> testBeans = new ArrayList<>();

    private TextView tv_light;
    private TextView tv_temp;
    private TextView tv_water;
    private TextView tv_air;


    private TextView tv_label_light;
    private TextView tv_label_temp;
    private TextView tv_label_water;
    private TextView tv_label_air;

    private TextView tv_dialog;

    private int continuouslyTouch = 0;
    private int lastType = -1;

    private Map<Integer, List<String>> paperwork = new HashMap<>();

    private ImageView iv_label_light;
    private ImageView iv_label_temp;
    private ImageView iv_label_water;
    private ImageView iv_label_air;

    private List<TextView> textViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim_test);
        rl_back = findViewById(R.id.rl_back);
        tv_light = findViewById(R.id.tv_light);
        tv_temp = findViewById(R.id.tv_temp);
        tv_water = findViewById(R.id.tv_water);
        tv_air = findViewById(R.id.tv_air);

        tv_label_light = findViewById(R.id.tv_label_light);
        tv_label_temp = findViewById(R.id.tv_label_temp);
        tv_label_water = findViewById(R.id.tv_label_water);
        tv_label_air = findViewById(R.id.tv_label_air);

        textViews.add(tv_light);
        textViews.add(tv_temp);
        textViews.add(tv_water);
        textViews.add(tv_air);
        textViews.add(tv_label_light);
        textViews.add(tv_label_temp);
        textViews.add(tv_label_water);
        textViews.add(tv_label_air);


        tv_dialog = findViewById(R.id.tv_dialog);

        iv_label_light = findViewById(R.id.iv_label_light);
        iv_label_temp = findViewById(R.id.iv_label_temp);
        iv_label_water = findViewById(R.id.iv_label_water);
        iv_label_air = findViewById(R.id.iv_label_air);

        iv_anim = findViewById(R.id.iv_anim);
        iv_anim.setImageResource(R.drawable.sleep_anim);
        AnimationDrawable animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
        animationDrawable.start();
        initPaperworkData();
        String address = getIntent().getStringExtra("address");
        startService(address);
    }

    private void initPaperworkData() {
        List<String> untouch = new ArrayList<>();
        untouch.add("你猜，多肉的梦是什么颜色的？");
        untouch.add("熬夜看星星，需要补个觉");
        untouch.add("睡个美容觉，醒来又是元气美少女");
        untouch.add("寂寞空庭春欲晚，梨花满地不开门");
        untouch.add("你来或不来，都在我梦里");
        paperwork.put(0, untouch);
        List<String> strings = new ArrayList<>();
        strings.add("好朋友，快快靠近我，我想跟你说个小秘密");
        strings.add("我梦到你的笑，睁眼亲吻你的手");
        strings.add("我猜你一定有世界上最好看的手，不然怎么能抚摸得如此温柔");
        strings.add("你难道是我的骑士，手握长剑守卫熟睡的我");
        strings.add("我猜你是不舍我睡去，好与我彻夜长谈");
        paperwork.put(1, strings);
        List<String> exciting = new ArrayList<>();
        exciting.add("你不过用指尖轻轻触碰了我，我却幸福地全身颤抖");
        exciting.add("如果你驯服了我，我们就会彼此需要");
        exciting.add("来呀来呀，一起摇摆~");
        exciting.add("Skr~这是我的swag style~");
        exciting.add("别挠我痒痒，我不要面子的嘛！");
        paperwork.put(2, exciting);
        List<String> sleeping = new ArrayList<>();
        sleeping.add("我聆听你的沉默，并想象你有一天对我滔滔不绝的样子");
        sleeping.add("想对着你讲，说无论如何，阴天快乐");
        sleeping.add("忘掉你的钢筋水泥森林吧，来我这里，自由呼吸");
        sleeping.add("你一离开，我就得了失语症");
        sleeping.add("你是不是也像我一样，把自己幻想成了睡美人");
        paperwork.put(3, sleeping);
    }

    private String getRandowPaperwork(int type) {
        List<String> strings = paperwork.get(type);
        if (strings == null) {
            return "";
        }
        int index = (int) (Math.random() * strings.size());
        return strings.get(index);
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
                public void onDataReceive(final TestBean testBean) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh(testBean);
                        }
                    });
                }

                @Override
                public void onConnectionStateChange(boolean isConnect) {

                }
            });

        }
    };

    private void setLight() {
        rl_back.setBackgroundResource(R.drawable.background);
        iv_label_air.setImageResource(R.drawable.label_air);
        iv_label_temp.setImageResource(R.drawable.label_temp);
        iv_label_light.setImageResource(R.drawable.label_light);
        iv_label_water.setImageResource(R.drawable.label_water);
        tv_dialog.setBackgroundResource(R.drawable.dialog_corners);
        for (TextView textView : textViews) {
            textView.setTextColor(getResources().getColor(R.color.text_primary));
        }

    }

    private void setNight() {
        rl_back.setBackgroundResource(R.drawable.background_night);
        iv_label_air.setImageResource(R.drawable.label_air_night);
        iv_label_temp.setImageResource(R.drawable.label_temp_night);
        iv_label_light.setImageResource(R.drawable.label_light_night);
        iv_label_water.setImageResource(R.drawable.label_water_night);
        tv_dialog.setBackgroundResource(R.drawable.dialog_corners_night);
        for (TextView textView : textViews) {
            textView.setTextColor(getResources().getColor(R.color.common_white));
        }
    }

    private void refresh(final TestBean testBean) {
        testBeans.add(testBean);
        while (testBeans.size() > 100) {
            testBeans.remove(0);
        }
        Log.i(TAG, "refresh: " + testBean.toString());
        if (testBean.getLight() < 10) {
            setNight();
        } else {
            setLight();
        }
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
                    setStatus(2);
                } else {
                    setStatus(1);
                }
            } else {
                if (continuouslyTouch > 0) {
                    continuouslyTouch--;
                }
                if (continuouslyTouch == 0) {
                    setStatus(0);
                }

            }
        }
        Log.d(TAG, "refresh: continuouslyTouch" + continuouslyTouch);
    }

    private void setStatus(final int type) {
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
                tv_dialog.setText(getRandowPaperwork(type));
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
