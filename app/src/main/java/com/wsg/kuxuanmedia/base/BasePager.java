package com.wsg.kuxuanmedia.base;

/*
 * @创建者     兰昱
 * @创建时间  2016/9/28 15:04
 * @描述	   页面公共类
 */

import android.content.Context;
import android.view.View;

public abstract class BasePager {

    public View rootView;
    public Context mContext;
    public boolean isInitData;

    public BasePager(Context context) {
        mContext=context;
        rootView=initView();
    }
    //必须实现
    //初始化界面
    public abstract  View initView();
    //选择实现
    //初始化数据
    public  void initData(){

    };
}
