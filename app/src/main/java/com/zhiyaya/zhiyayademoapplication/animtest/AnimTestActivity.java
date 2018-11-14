package com.zhiyaya.zhiyayademoapplication.animtest;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.zhiyaya.zhiyayademoapplication.R;

public class AnimTestActivity extends AppCompatActivity {

    private ImageView iv_anim;

    private Button btn_blink;
    private Button btn_excite;
    private Button btn_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim_test);

        iv_anim = findViewById(R.id.iv_anim);
        iv_anim.setImageResource(R.drawable.sleep_anim);
        AnimationDrawable animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
        animationDrawable.start();

        /*iv_blink_anim = findViewById(R.id.iv_blink_anim);
        iv_blink_anim.setImageResource(R.drawable.blink_anim);
        AnimationDrawable animationDrawable1 = (AnimationDrawable) iv_blink_anim.getDrawable();
        animationDrawable1.start();

        iv_excite_anim = findViewById(R.id.iv_excite_anim);
        iv_excite_anim.setImageResource(R.drawable.excite_anim);
        AnimationDrawable animationDrawable2 = (AnimationDrawable) iv_excite_anim.getDrawable();
        animationDrawable2.start();*/

        btn_blink = findViewById(R.id.btn_blink);
        btn_blink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_anim.setImageResource(R.drawable.blink_anim);
                AnimationDrawable animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
                animationDrawable.start();
            }
        });
        btn_excite = findViewById(R.id.btn_excite);
        btn_excite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_anim.setImageResource(R.drawable.excite_anim);
                AnimationDrawable animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
                animationDrawable.start();
            }
        });
        btn_reset = findViewById(R.id.btn_reset);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_anim.setImageResource(R.drawable.sleep_anim);
                AnimationDrawable animationDrawable = (AnimationDrawable) iv_anim.getDrawable();
                animationDrawable.start();
            }
        });
        /*iv_sleep_anim = findViewById(R.id.iv_sleep_anim);
        iv_sleep_anim.setImageResource(R.drawable.sleep_anim);
        AnimationDrawable animationDrawable = (AnimationDrawable) iv_sleep_anim.getDrawable();
        animationDrawable.start();*/


    }
}
