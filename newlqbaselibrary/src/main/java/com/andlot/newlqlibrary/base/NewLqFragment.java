package com.andlot.newlqlibrary.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andlot.newlqlibrary.utils.L;

/**
 * @author NewLq
 * 简单封装了Fragment 基类， 懒加载模式
 */
public abstract class NewLqFragment extends Fragment implements View.OnClickListener{

    protected View mView;
    protected Context mContext;
    protected Activity fragActivity;

    private boolean isViewInit;

    public NewLqFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
        fragActivity = activity;
        isViewInit = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWidgetsBefore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);
        int layoutId = getContentViewId();
        if (0 > layoutId) {
            TextView errorText = new TextView(mContext);
            errorText.setText(this.getClass().getSimpleName() + "的Fragment_LayoutId出现错误");
            errorText.setGravity(Gravity.CENTER);
            errorText.setTextSize(30);
            errorText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            ViewGroup.LayoutParams errorTextLayoutParams = errorText.getLayoutParams();
            errorTextLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            errorTextLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            errorText.setLayoutParams(errorTextLayoutParams);
            mView = errorText;
        } else {
            mView = View.inflate(mContext, layoutId, null);
            try {
                return mView;
            } finally {
                initWidgets(mView);
                isViewInit = true;
            }
        }
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData(savedInstanceState);
        initListener();
    }

    //	事务切换Fragment，需要重写方法
    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        View fragView = getView();
        if(fragView != null)
            fragView.setVisibility(menuVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (isViewInit) {
            if (hidden) {
                onInVisibility();
            } else {
                onVisibility();
            }
        } else {
            //View未初始化时, Fragment第一次进入, 可以初始创建一些目录或者文件
        }
    }

    /**
     * 加载页面可见,显示
     * 参数 没有savedInstanceState 所以请求网络在此， 不处理其他信息
     * 当Frag 可见时调用
     */
    protected abstract void onVisibility();

    /**
     * 页面不可见, 隐藏后
     * 回收可在屏幕不可见时, 回收资源操作
     * 当Frag 不可见时调用
     */
    protected void onInVisibility(){
    }

    public void initWidgetsBefore() {
    }

    protected abstract void initWidgets(View frgView);

    /**
     * 操作本地数据 例如 恢复状态 恢复数据
     * @param savedInstanceState
     */
    protected abstract void initData(Bundle savedInstanceState);

    protected abstract void initListener();

    protected abstract int getContentViewId();

    protected abstract NewLqFragment getInstance(Bundle bundle);

    @Override
    public void onClick(View v) {
    }
}
