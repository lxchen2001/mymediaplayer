package com.wsg.kuxuanmedia.activity;

/*
 * @创建者     兰昱
 * @创建时间  2016/9/28 20:17
 * @描述	      
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wsg.kuxuanmedia.R;
import com.wsg.kuxuanmedia.domain.MediaItem;
import com.wsg.kuxuanmedia.utils.CommonUtils;
import com.wsg.kuxuanmedia.view.MyVideoView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.wsg.kuxuanmedia.utils.CommonUtils.getSystemTime;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {


    @InjectView(R.id.media_tv_name)
    TextView mMediaTvName;
    @InjectView(R.id.media_im_battery)
    ImageView mMediaImBattery;
    @InjectView(R.id.media_tv_systemTime)
    TextView mMediaTvsSystemTime;
    @InjectView(R.id.media_bt_voice)
    Button mMediaBtVoice;
    @InjectView(R.id.media_pb_voice)
    AppCompatSeekBar mMediaPbVoice;
    @InjectView(R.id.media_bt_info)
    Button mMediaBtInfo;
    @InjectView(R.id.media_tv_currentTime)
    TextView mMediaTvCurrentTime;
    @InjectView(R.id.media_pb_time)
    AppCompatSeekBar mMediaPbTime;
    @InjectView(R.id.media_tv_totalTime)
    TextView mMediaTvTotalTime;
    @InjectView(R.id.btn_exit)
    Button mBtnExit;
    @InjectView(R.id.btn_video_pre)
    Button mBtnVideoPre;
    @InjectView(R.id.btn_video_start_pause)
    Button mBtnVideoStartPause;
    @InjectView(R.id.btn_video_next)
    Button mBtnVideoNext;
    @InjectView(R.id.btn_video_siwch_screen)
    Button mBtnVideoSiwchScreen;
    @InjectView(R.id.activity_videoview)
    MyVideoView mActivityVideoview;
    @InjectView(R.id.ll_buffer_tv)
    TextView mLlBufferTv;
    @InjectView(R.id.ll_buffer)
    LinearLayout mLlBuffer;
    @InjectView(R.id.ll_loading)
    LinearLayout mLlLoading;

    private int mScreenWidth;
    private int mScreenHeight;
    private AudioManager mAm;
    //最大音量
    private int mMaxVolume;
    //当前音量
    private int mCurrentVolume;
    private ArrayList<MediaItem> mMediaItems;
    private int mPosition;
    private boolean isFullScreen = false;
    private boolean isShowController = false;
    private boolean isNetUrl;
    private boolean isUseSystem = false;//是否用系统本身的来检测

    private int VideoWidth;
    private int VideoHeight;
    private static final int DEFAULT_SCREEN = 1;
    private static final int FULL_SCREEN = 2;
    private static final int SHOW_SPEED = 3;

    private static final int PROGRESS = 1;
    private static final int HIDE_CONTROLLER = 2;
    private BatteryReceiver mBatteryReceiver;
    private RelativeLayout mMedia_rl_controller;
    //手势识别器
    /*
    1、定义
    2、实例化
    3、重写单击，双击，长按
    4、ontouchevent（）传递
     */
    private GestureDetector mGestureDetector;
    private int preCurrentPositon;//上一秒视频播放的位置
    private float mStarY = 0;//手指滑动的初始距离
    private float mStarX = 0;
    int totalDistance = 0;
    private int mDownVoice;
    private Uri mUri;//判断是否为网络uri
    private Vibrator vibrator;//振动器

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HIDE_CONTROLLER:
                    //隐藏控制栏
                    //                    Toast.makeText(VideoPlayerActivity.this, "hiden", Toast.LENGTH_SHORT).show();
                    hideMediaController();
                    break;
                case PROGRESS:
                    //设置进度条
                    int currentPosition = mActivityVideoview.getCurrentPosition();
                    mMediaPbTime.setProgress(currentPosition);

                    mMediaTvCurrentTime.setText(CommonUtils.timeToString(currentPosition));
                    //设置系统时间
                    mMediaTvsSystemTime.setText(getSystemTime());

                    //如果是网络视频，设置缓冲
                    if (isNetUrl) {
                        //只有网络资源才有缓存效果,实际测试，这个缓冲是假的，断了网根本用不了，对于大文件视频也没法播放

                        /*
                        以下信息可以再官方文档或者源码中找到
                         */

                        int bufferPercentage = mActivityVideoview.getBufferPercentage();//默认0到100，并不是百分号，所以一会要除100
                        int totalBuffer = bufferPercentage * mMediaPbTime.getMax() / 100;
                        //                        Toast.makeText(VideoPlayerActivity.this,
                        //                                "bufferPercentage:" + bufferPercentage + "totalbuffer" + totalBuffer
                        //                                , Toast.LENGTH_SHORT).show();
                        mMediaPbTime.setSecondaryProgress((totalBuffer / 100));
                    } else {
                        //本地视频
                        mMediaPbTime.setSecondaryProgress(0);
                    }

                    //自定义测试视频卡不卡，算一下单位时间内是否走过500的距离，走不过说明比较卡
                    //前辈666
                    //实践证明，系统自己的根本不靠谱，还是自己写比较好
                    if (!isUseSystem && mActivityVideoview.isPlaying()) {

                        int buffer = currentPosition - preCurrentPositon;
                        if (buffer < 500) {
                            String netSpeed = CommonUtils.getNetSpeed(VideoPlayerActivity.this);
                            if (isNetUrl) {
                                mLlBufferTv.setText("加载中" + netSpeed);
                            } else {
                                mLlBufferTv.setText("拼命加载中");
                            }

                            mLlBuffer.setVisibility(View.VISIBLE);
                        } else {
                            mLlBuffer.setVisibility(View.GONE);
                        }
                        preCurrentPositon = currentPosition;
                    }


                    //每秒更新一次,坑爹啊，这里忘记改了
                    mHandler.removeMessages(PROGRESS);
                    //在发消息，实现循环，自己掉自己，实现每秒更新
                    mHandler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
                //展示网速
                case SHOW_SPEED:
                    //1、拿到网络速度
                    String netSpeed = CommonUtils.getNetSpeed(VideoPlayerActivity.this);
                    //2、设置TextView进行显示
                    mLlBufferTv.setText("玩命加载中..."+netSpeed);
                    //3、每两秒刷新一次
                    //这样写比较消耗性能，看视频我关注网速干毛啊，写到卡顿里
                    mHandler.removeMessages(SHOW_SPEED);
                    mHandler.sendEmptyMessageDelayed(SHOW_SPEED,2000);
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        ButterKnife.inject(this);
        mMedia_rl_controller = (RelativeLayout) findViewById(R.id.media_rl_controller);
        initData();
        initListener();

    }

    private void initData() {
        hideMediaController();
        //拿取播放数据可以从自己的应用内部，也可能是外部调用的
        mUri = getIntent().getData();
        mMediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("media");
        mPosition = getIntent().getIntExtra("position", 0);


        if (mUri != null) {
            mUri = getIntent().getData();
            //判断一下是不是网路地址
            isNetUrl = CommonUtils.isNetUrl(mUri.toString());

            //文件夹，图片浏览器，早期的QQ空间
            mMediaTvName.setText(mUri.toString());

            mActivityVideoview.setVideoURI(mUri);
        } else if (mMediaItems != null && mMediaItems.size() > 0) {
            MediaItem mediaItem = mMediaItems.get(mPosition);
            isNetUrl = CommonUtils.isNetUrl(mediaItem.getData());
            mActivityVideoview.setVideoURI(Uri.parse(mediaItem.getData()));
            mMediaTvName.setText(mediaItem.getName());
        } else {
            Toast.makeText(this, "no data bayby", Toast.LENGTH_SHORT).show();
        }


        //设置按钮的点击状态
        setButtonState();
        //设置系统时间
        mMediaTvsSystemTime.setText(CommonUtils.getSystemTime());


        //注册电量广播
        mBatteryReceiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryReceiver, intentFilter);




        //得到屏幕宽高,旧方法过时，推荐新技术
        //        getWindowManager().getDefaultDisplay().getWidth();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;



        //得到音量
        mAm = (AudioManager) getSystemService(AUDIO_SERVICE);
        //最大音量
        mMaxVolume = mAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        mCurrentVolume = mAm.getStreamVolume(AudioManager.STREAM_MUSIC);


        //seekbar设置最大音量
        mMediaPbVoice.setMax(mMaxVolume);
        //seekbar 设置当前音量
        mMediaPbVoice.setProgress(mCurrentVolume);







    }

    private void setButtonState() {
        //1.没有数据，全灭2.一条数据，全灭3.两条数据，第一条下灭，第二条前灭4多条数据，第一条，最后一条，中间
        if (mMediaItems == null || mMediaItems.size() <= 1) {
            setEnable(false);
        } else {
            if (mPosition == 0) {
                mBtnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                mBtnVideoPre.setEnabled(false);

                mBtnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                mBtnVideoNext.setEnabled(true);

            } else if (mMediaItems.size() - 1 == mPosition) {
                mBtnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                mBtnVideoNext.setEnabled(false);
                mBtnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                mBtnVideoPre.setEnabled(true);

            } else {
                mBtnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                mBtnVideoNext.setEnabled(true);
                mBtnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                mBtnVideoPre.setEnabled(true);

            }
        }
    }

    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);//0~100;
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            mMediaImBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            mMediaImBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            mMediaImBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            mMediaImBattery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            mMediaImBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            mMediaImBattery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            mMediaImBattery.setImageResource(R.drawable.ic_battery_100);
        } else {
            mMediaImBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private void initListener() {

        //点击事件
        mBtnExit.setOnClickListener(this);
        mBtnVideoNext.setOnClickListener(this);
        mBtnVideoPre.setOnClickListener(this);
        mBtnVideoSiwchScreen.setOnClickListener(this);
        mBtnVideoStartPause.setOnClickListener(this);
        mMediaBtInfo.setOnClickListener(this);
        mMediaBtVoice.setOnClickListener(this);
        //监听事件
        mActivityVideoview.setOnPreparedListener(new MyVideoPreparedListener());
        mActivityVideoview.setOnErrorListener(new MyVideoErrorListener());
        mActivityVideoview.setOnCompletionListener(new MyVideoCompletionListener());


        if (isUseSystem) {
            //只有4.2的系统才支持自带的这个方法  实际体验，不靠谱，视频停顿也没有出现效果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                //监听网络视频卡
                mActivityVideoview.setOnInfoListener(new MyVideoInfoListener());
            }
        }



        //影视进度条
        mMediaPbTime.setOnSeekBarChangeListener(new MySeekbarDurationListener());
        //声音进度条
        mMediaPbVoice.setOnSeekBarChangeListener(new MySeekbarVoiceListener());



        //手势识别器
        //实例化手势识别器，并重写方法
        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            //长按
            /*
            播放和暂停
             */
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            //双击
            /*
            开启全屏和关闭全屏
             */

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setFullScreenAndDefault();
                return super.onDoubleTap(e);
            }

            //单击
            /*
            显示和隐藏状态栏，若显示等4秒自动隐藏，用handler实现
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isShowController) {
                    hideMediaController();
                    mHandler.removeMessages(HIDE_CONTROLLER);
                } else {
                    showMediaController();
                    mHandler.sendEmptyMessageDelayed(HIDE_CONTROLLER, 4000);
                    //                    mHandler.sendEmptyMessage(HIDE_CONTROLLER);
                }
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit:
                finish();
                break;
            case R.id.btn_video_pre:
                playPre();
                break;
            case R.id.btn_video_next:
                playNext();
                break;
            case R.id.btn_video_siwch_screen:
                setFullScreenAndDefault();
                break;
            case R.id.btn_video_start_pause:
                startAndPause();
                break;
            case R.id.media_bt_info:
                //转换成万能播放器
                choiceVitamio();
                break;
            case R.id.media_bt_voice:
                //设置静音
                mAm.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                mMediaPbVoice.setProgress(0);
                break;

        }
        mHandler.removeMessages(HIDE_CONTROLLER);
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROLLER, 4000);
    }

    private void choiceVitamio() {

        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
        builder.setTitle("系统播放器");
        builder.setMessage("当您播放视频，有声音没有画面的时候，请切换万能播放器播放");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();

            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    //启动vitamio播放器
    private void startVitamioPlayer() {
        //先关闭自己的播放器
        if (mActivityVideoview.isPlaying()) {
            mActivityVideoview.stopPlayback();
        }

        Intent intent = new Intent(this, VitamioPlayerActivity.class);
        //数据就重新走一遍咯
        if (mUri != null) {
            intent.setData(mUri);
        } else if (mMediaItems != null && mMediaItems.size() > 0) {

            Bundle bundle = new Bundle();
            bundle.putSerializable("media", mMediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", mPosition);
        }
        startActivity(intent);
        finish();
    }

    private void setFullScreenAndDefault() {
        if (isFullScreen) {
            setVideoType(DEFAULT_SCREEN);

        } else {
            setVideoType(FULL_SCREEN);

        }

    }

    private void setVideoType(int defaultScreen) {

        if (defaultScreen == DEFAULT_SCREEN) {
            //1.设置视频画面的大小
            //视频真实的宽和高
            //1.设置视频画面的大小
            //视频真实的宽和高
            int mVideoWidth = VideoWidth;
            int mVideoHeight = VideoHeight;

            //屏幕的宽和高
            int width = mScreenWidth;
            int height = mScreenHeight;


            //等比例拉伸
            //

            // for compatibility, we adjust size based on aspect ratio
            if (mVideoWidth * height < width * mVideoHeight) {
                //Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else if (mVideoWidth * height > width * mVideoHeight) {
                //Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            }


            mActivityVideoview.setVideoSize(width, height);
            isFullScreen = false;
            mBtnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);

        } else if (defaultScreen == FULL_SCREEN) {
            mActivityVideoview.setVideoSize(mScreenWidth, mScreenHeight);
            isFullScreen = true;
            mBtnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
        }

    }

    private void playNext() {

        if (mPosition != mMediaItems.size() - 1) {
            mPosition++;
            //设置数据
            if (mMediaItems != null) {
                //显示加载进度条
                mLlLoading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mMediaItems.get(mPosition);
                isNetUrl = CommonUtils.isNetUrl(mediaItem.getData());

                mActivityVideoview.setVideoURI(Uri.parse(mediaItem.getData()));
                mMediaTvName.setText(mediaItem.getName());
            }
        }
        setButtonState();
        mBtnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
    }

    private void playPre() {

        if (mPosition != 0) {
            mPosition--;
            //设置数据
            if (mMediaItems != null) {
                //显示加载进度条
                mLlLoading.setVisibility(View.VISIBLE);
                MediaItem mediaItem = mMediaItems.get(mPosition);
                isNetUrl = CommonUtils.isNetUrl(mediaItem.getData());
                mActivityVideoview.setVideoURI(Uri.parse(mediaItem.getData()));
                mMediaTvName.setText(mediaItem.getName());
            }
        }
        setButtonState();
        mBtnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
    }

    private void startAndPause() {
        if (mActivityVideoview.isPlaying()) {
            mActivityVideoview.pause();
            mBtnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            mActivityVideoview.start();
            mBtnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    class MyVideoPreparedListener implements MediaPlayer.OnPreparedListener {


        @Override
        public void onPrepared(MediaPlayer mp) {

            mActivityVideoview.start();
                /*--------------- add begin  坑爹的bug啊，播放器参数就在括号里啊，乱用什么~~~~(>_<)~~~~---------------*/

                //得到真实的视频宽高
            VideoWidth = mp.getVideoWidth();
            VideoHeight = mp.getVideoHeight();
                /*--------------- add end  ---------------*/



            // /得到视频时长

            int duration = mActivityVideoview.getDuration();
            //设置总时长
            mMediaTvTotalTime.setText(CommonUtils.timeToString(duration));
            //设置seekbar总进度
            mMediaPbTime.setMax(duration);
            //自定义控件后默认全屏，所以要先保持小屏
            mHandler.sendEmptyMessage(PROGRESS);
            setVideoType(DEFAULT_SCREEN);
            //默认隐藏控制面板
            hideMediaController();



            //隐藏加载框
            //网速的加载效果
            mLlLoading.setVisibility(View.GONE);

//            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//                @Override
//                public void onSeekComplete(MediaPlayer mp) {
//                    Toast.makeText(VideoPlayerActivity.this, "拖动完成", Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    class MyVideoErrorListener implements MediaPlayer.OnErrorListener {


        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            /*
            1、播放的视频格式不支持                   ————跳转到万能播放器播放
            2、网络环境                                ————1、如果网络确实断开，可以提示用户网络断开2、网络断断续续，重新播放
            3、视频本身存在问题（缺省等）             ————下载做完成
             */
            startVitamioPlayer();
            return true;
        }
    }

    class MyVideoInfoListener implements MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，拖动卡了
//                    Toast.makeText(VideoPlayerActivity.this, "视频开始卡了", Toast.LENGTH_SHORT).show();
                    //显示进度条
                    mLlBuffer.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END://视频卡结束了
//                    Toast.makeText(VideoPlayerActivity.this, "视频卡结束了", Toast.LENGTH_SHORT).show();

                    mLlBuffer.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }


    //监听视频是否播放完成，若播放完成，则回调该方法
    class MyVideoCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            playNext();
        }
    }


    //拖动进度条实现视频的倒退或前进
    class MySeekbarDurationListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //如果是用户引起的
            if (fromUser) {
                mActivityVideoview.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    //声音大小随着seekbar的变化而改变
    class MySeekbarVoiceListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //如果是用户引起的
            if (fromUser) {
                mAm.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                mMediaPbVoice.setProgress(progress);
            }
        }

        //当手指触碰的时候触发该方法
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        //当手指离开时回调该方法
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    //设置前后按钮的点击状态
    private void setEnable(boolean isEnable) {
        if (isEnable) {
            mBtnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            mBtnVideoPre.setEnabled(true);
            mBtnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            mBtnVideoNext.setEnabled(true);
        } else {
            //两个按钮设置灰色
            mBtnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            mBtnVideoPre.setEnabled(false);
            mBtnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            mBtnVideoNext.setEnabled(false);
        }

    }

    /**
     * 显示控制面板
     */
    private void showMediaController() {
        mMedia_rl_controller.setVisibility(View.VISIBLE);
        isShowController = true;

    }

    /**
     * 隐藏控制面板
     */
    private void hideMediaController() {
        mMedia_rl_controller.setVisibility(View.GONE);
        isShowController = false;
    }


    //屏幕旋转的时不行驶生命周期调用,先放下，完了再研究
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //视频宽高
        int mVideoWidth = VideoWidth;
        int mVideoHeight = VideoHeight;

        //屏幕旋转交换宽高
        int temp;
        temp = mScreenWidth;
        mScreenWidth = mScreenHeight;
        mScreenHeight = temp;

        int height = mScreenHeight;
        int width = mScreenWidth;

        if (isFullScreen) {
            mActivityVideoview.setVideoSize(mScreenWidth, mScreenHeight);
        } else {
            // for compatibility, we adjust size based on aspect ratio
            if (mVideoWidth * height < width * mVideoHeight) {
                //Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else if (mVideoWidth * height > width * mVideoHeight) {
                //Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            }
            mActivityVideoview.setVideoSize(width, height);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销电量监听者
        if (mBatteryReceiver != null) {
            unregisterReceiver(mBatteryReceiver);
            mBatteryReceiver = null;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    //上下滑动控制音量
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //3.把事件传递给手势识别器,非常重要，不写就不会有效果
        /*
        bingo
         */
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //起始位置
                mStarY = event.getY();
                mStarX = event.getX();

                //获取总距离，方便后面比例计算

                totalDistance = Math.min(mScreenHeight, mScreenWidth);

                //单独再拿取一次就是为了使得原有音量独立，如果用原先求得那个音量在move里用，因为move会不断执行
                //所以随着音量的增加会成倍增加，例如y=y+5；

                //获取当前声音
                mDownVoice = mAm.getStreamVolume(AudioManager.STREAM_MUSIC);

                mHandler.removeMessages(HIDE_CONTROLLER);
                break;

            case MotionEvent.ACTION_MOVE:
                //最终位置
                float endY = event.getY();
                float endX = event.getX();
                //如何在屏幕右半部滑动改变音量，左半部改变亮度
                float distance = mStarY - endY;


                if (endX > mScreenWidth / 2) {
                    //好聪明啊，屏幕的右边

                    //根据比例计算改变的声音大小
                    /*
                    改变的距离：总距离=改变的声音：总声音
                    所以改变的声音等于      改变的距离：总距离  乘  最大音量
                     */
                    float delVoice = (distance / totalDistance) * mMaxVolume;

                    /*
                    设置当前音量
                    不能小于0，也不能大于最大值，哈哈哈
                     */
                    mCurrentVolume = (int) Math.min(mMaxVolume, Math.max(0, delVoice + mDownVoice));//非常巧妙
                    //设置音量
                    mAm.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
                    //进度条随之改变
                    mMediaPbVoice.setProgress(mCurrentVolume);
                } else {
                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distance > FLING_MIN_DISTANCE
                            && Math.abs(distance) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (distance < FLING_MIN_DISTANCE
                            && Math.abs(distance) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                mHandler.sendEmptyMessageDelayed(HIDE_CONTROLLER, 4000);
                break;

        }
        return super.onTouchEvent(event);
    }

    //监听物理音量键的改变
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mCurrentVolume--;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mCurrentVolume++;
        }

        mAm.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
        mMediaPbVoice.setProgress(mCurrentVolume);

        mHandler.removeMessages(HIDE_CONTROLLER);
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROLLER,400);
        return super.onKeyDown(keyCode, event);
    }



    /*
    *
    * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
    */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // if (lp.screenBrightness <= 0.1) {
        // return;
        // }
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        //如果超过指定界限范围就开始在振动
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
//            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            long[] pattern = { 10, 200 }; // OFF/ON/OFF/ON...
//            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
//            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            long[] pattern = { 10, 200 }; // OFF/ON/OFF/ON...
//            vibrator.vibrate(pattern, -1);
        }
        //设置亮暗度
        getWindow().setAttributes(lp);
    }

}
