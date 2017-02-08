package com.andlot.newlqlibrary.base;

import android.app.Activity;
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
    private boolean isViewInit = false;
    public NewLqLazyFragment() {
    }

    @Override
    public void onAttach(Activity context) {
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
                onInVisibility();
            } else {
                onVisibility();
            }
        } else {
            //View 未初始化时，Fragment 被首次打开
        }

    }

    private void onInVisibility() {
    }

    protected abstract void onVisibility();

    public void initWidgetsBefore() {
    }

    protected abstract void initWidgets(View frgView);

    protected abstract void initData(Bundle savedInstanceState);

    protected abstract void initListener();

    protected abstract int getContentViewId();

}
