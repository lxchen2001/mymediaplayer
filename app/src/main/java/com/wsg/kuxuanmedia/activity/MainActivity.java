package com.wsg.kuxuanmedia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wsg.kuxuanmedia.R;
import com.wsg.kuxuanmedia.base.BasePager;
import com.wsg.kuxuanmedia.fragment.ReplaceFragment;
import com.wsg.kuxuanmedia.pager.AudioPager;
import com.wsg.kuxuanmedia.pager.NetAudioPager;
import com.wsg.kuxuanmedia.pager.NetVideoPager;
import com.wsg.kuxuanmedia.pager.VideoPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @InjectView(R.id.activity_main_framelayout)
    FrameLayout mActivityMainFramelayout;
    @InjectView(R.id.activity_main_radionGroup)
    RadioGroup mActivityMainRadionGroup;
    @InjectView(R.id.titlebar_tv_search)
    TextView mTitlebarTvSearch;

    private List<BasePager> mBasePagers;
    public int checkPosition;
    //储存点击的时间
    private long preTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);


        mBasePagers = new ArrayList<BasePager>();
        mBasePagers.add(new VideoPager(this));//添加本地视频页面-0
        mBasePagers.add(new AudioPager(this));//添加本地音乐页面-1
        mBasePagers.add(new NetVideoPager(this));//添加网络视频页面-2
        mBasePagers.add(new NetAudioPager(this));//添加网络音频页面-3

        //设置监听，默认选择第一个
        mActivityMainRadionGroup.setOnCheckedChangeListener(new RadioGroupListener());
        //默认选中首页
        mActivityMainRadionGroup.check(R.id.rb_video);
        //点击事件
        mTitlebarTvSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.titlebar_tv_search:
                //跳转到搜索界面
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
        }
    }

    class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rb_audio:
                    checkPosition = 1;
                    break;
                case R.id.rb_netvideo:
                    checkPosition = 2;
                    break;
                case R.id.rb_netaudio:
                    checkPosition = 3;
                    break;
                default:
                    checkPosition = 0;
                    break;
            }
            setFrament();
        }
    }

    //添加碎片的方法
    public void setFrament() {

        //得到manager
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        //开启事务
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        //替换
        fragmentTransaction.replace(R.id.activity_main_framelayout,new ReplaceFragment(getBasePager()));
        //提交
        fragmentTransaction.commit();
    }

    public BasePager getBasePager() {
        BasePager basePager = mBasePagers.get(checkPosition);
        if (basePager != null && !basePager.isInitData) {
            basePager.isInitData = true;
            basePager.initData();
            //联网请求，请求数据
        }
        return basePager;
    }

    @Override
    public void onBackPressed() {
        if (checkPosition != 0) {
            checkPosition = 0;
            setFrament();
            mActivityMainRadionGroup.check(R.id.rb_video);
            return;
        }
        if (System.currentTimeMillis() - preTime > 2000) {
            preTime = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }
}
