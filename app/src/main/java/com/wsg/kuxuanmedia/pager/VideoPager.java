package com.wsg.kuxuanmedia.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wsg.kuxuanmedia.R;
import com.wsg.kuxuanmedia.activity.VideoPlayerActivity;
import com.wsg.kuxuanmedia.adapter.VideoAudioAdapter;
import com.wsg.kuxuanmedia.base.BasePager;
import com.wsg.kuxuanmedia.domain.MediaItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.view.View.inflate;

/**
 * 作者：杨光福 on 2016/7/16 11:48
 * 微信：yangguangfu520
 * QQ号：541433511
 * 作用：本地视频页面
 */
public class VideoPager extends BasePager implements AdapterView.OnItemClickListener {


    @InjectView(R.id.videopager_list)
    ListView mVideopagerList;
    @InjectView(R.id.videopager_pb_load)
    ProgressBar mVideopagerPbLoad;
    @InjectView(R.id.videopager_tv_load)
    TextView mVideopagerTvLoad;

    private List<MediaItem> mMediaItems;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mMediaItems != null && mMediaItems.size() > 0) {
                mVideopagerList.setAdapter(new VideoAudioAdapter(mContext,mMediaItems,true));
            } else {
                mVideopagerTvLoad.setVisibility(View.VISIBLE);
            }
            mVideopagerPbLoad.setVisibility(View.GONE);
        }
    };

    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        //为界面添加布局
        View inflate = inflate(mContext, R.layout.video_pager, null);
        ButterKnife.inject(this, inflate);
        mVideopagerList.setOnItemClickListener(this);
        return inflate;
    }

    @Override
    public void initData() {
        getDataFromLocal();
    }

    /**
     * 从本地的sdcard得到数据
     * //1.遍历sdcard,后缀名
     * //2.从内容提供者里面获取视频
     * //3.如果是6.0的系统，动态获取读取sdcard的权限
     */
    private void getDataFromLocal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMediaItems = new ArrayList<MediaItem>();
				//获取内容提供者
                ContentResolver resolver = mContext.getContentResolver();
				//系统视频的内容提供者
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                //具体需要获取的参数
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Video.Media.DURATION,//视频总时长
                        MediaStore.Video.Media.SIZE,//视频的文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址
                        MediaStore.Video.Media.ARTIST,//歌曲的演唱者
                };

                //访问内部信息
                //cursor 光标
                Cursor cursor = resolver.query(uri, objs, null, null, null);

                if(cursor!=null) {


                    while (cursor.moveToNext()) {

                        MediaItem mediaItem = new MediaItem();

                        String name = cursor.getString(0);//视频的名称
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);//视频的时长
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);//视频的文件大小
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);//视频的播放地址
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);//艺术家
                        mediaItem.setArtist(artist);

                        mMediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
                /*
                发消息，随便发一个，保证handler执行即可
                当有多种逻辑时，采区分
                 */
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        Intent intent=new Intent(mContext, VideoPlayerActivity.class);

        //1显然通过这种方法，只能传递播放地址，其他资料都获取不到，out
//        intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");




        Bundle bundle = new Bundle();
        bundle.putSerializable("media", (Serializable) mMediaItems);
        intent.putExtras(bundle);
        //文件位置
        intent.putExtra("position",position);
        mContext.startActivity(intent);
    }


}
