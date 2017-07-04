package com.wsg.kuxuanmedia.view;

/*
 * @创建者     兰昱
 * @创建时间  2016/9/29 16:04
 * @描述	   自定义控件为了控制大小
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.VideoView;


public class MyVideoView extends VideoView {
    /*
    在代码创建的时候调用
     */
    public MyVideoView(Context context) {
        this(context,null);
    }

    /*
    在布局文件的时候调用
     */
    public MyVideoView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    /*
    当需要设置样式的时候调用该方法
     */
    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
    测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /**
    * 设置视频的宽和高 \n
    * 简单的装个逼
     * by wsg
    * @param videoWidth 指定视频的宽
    * @param videoHeight 指定视频的高
    */
    public void setVideoSize(int videoWidth,int videoHeight){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = videoWidth;
        params.height = videoHeight;
        setLayoutParams(params);
    }

}
