package com.andlot.newlqlibrary.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author NewLq
 * Fragment 基类 配合Viewpager使用， 懒加载数据 在第一次进入 或用户可见时， 加载最新数据
 */
public abstract class NewLqLazyFragment extends Fragment {

    protected View mView;
    protected Context mContext;
    private boolean isViewInit;

    public NewLqLazyFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        isViewInit = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWidgetsBefore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater,container,savedInstanceState);
        int layoutId = getContentViewId();
        if (0 < layoutId) {
            mView = inflater.inflate(layoutId, null);
            initWidgets(mView);
            isViewInit = true;
        }
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData(savedInstanceState);
        initListener();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isViewInit) {
            if (isVisibleToUser) {
                onVisibility();
            } else {
                onInVisibility();
            }
        } else {
            //View未初始化时, Fragment第一次进入, 可以初始创建一些目录或者文件
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (((NewLqActivity) mContext).isFinishing()) {
            onReleaseRes();
        }
    }

    /**
     * 后台不可见，并且Activity被关闭时 释放少量资源
     */
    protected void onReleaseRes(){

    }

    public void initWidgetsBefore() {
    }

    protected void onInVisibility() {
    }

    protected abstract void onVisibility();

    protected abstract void initWidgets(View frgView);

    protected abstract void initData(Bundle savedInstanceState);

    protected abstract void initListener();

    protected abstract int getContentViewId();

    protected abstract NewLqLazyFragment getInstance(Bundle bundle);

}
