package com.wsg.kuxuanmedia.activity;

/*
 * @创建者     兰昱
 * @创建时间  2016/9/28 11:57
 * @描述	      
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.wsg.kuxuanmedia.R;

public class SplashActivity extends Activity {

    private static final int REQUECT_CODE_SDCARD = 2;
    private Handler mHandler=new Handler();
    private Button mSplash_button;
    private boolean isMainActivity=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);





        mSplash_button = (Button) findViewById(R.id.activity_splash_button);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
            }
        },2000);

        mSplash_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });






    }

    private void startMainActivity() {
        if (!isMainActivity){
            isMainActivity=true;
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();

        }
    }

    @Override
    protected void onDestroy() {
        //不移除handler进去后还会反弹
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    //触摸事件
    /*
    如果用户等不及，点击闪屏页任意位置，都可以进入主页面
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startMainActivity();
        return super.onTouchEvent(event);
    }
}
