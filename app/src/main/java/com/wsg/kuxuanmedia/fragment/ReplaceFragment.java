package com.wsg.kuxuanmedia.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wsg.kuxuanmedia.base.BasePager;

/**
 * 项目名：ShangGuiGu321Meida-master
 * 包名：com.wsg.kuxuanmedia.fragment
 * 文件名：ReplaceFragment
 * 创建者：wsg
 * 创建时间：2017/6/3  21:10
 * 描述：TODO
 */

public class ReplaceFragment extends Fragment {

    private BasePager currPager;

    public ReplaceFragment(BasePager pager) {
        this.currPager=pager;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return currPager.rootView;

    }
}
