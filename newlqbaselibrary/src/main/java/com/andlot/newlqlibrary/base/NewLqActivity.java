package com.andlot.newlqlibrary.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.andlot.newlqlibrary.helper.ActivityManager;
import com.andlot.newlqlibrary.utils.CommonUtils;

/**
 * Activity 开发模板
 *
 * @author Newlq
 */
public abstract class NewLqActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback{

    protected FragmentManager mFragmentManager;
    protected Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getContentViewId();
        if (0 >= layoutId) {
            throw new IllegalArgumentException("Activity布局文件有问题");
        }
        ActivityManager.getInstance().addActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layoutId);
        mHandler = new Handler(this);
        mFragmentManager = getSupportFragmentManager();

        initWidgets();
        handlerIntent(getIntent());
        initProjectFromAsync(savedInstanceState);
    }

    /**
     * 初始化界面控件View
     */
    protected abstract void initWidgets();

    /**
     * 处理被打开的Intent
     */
    protected void handlerIntent(Intent otherIntent){

    }

    /**
     * 初始化当前页数据
     *
     * @param savedInstanceState 保存状态数据的Bundle对象
     */
    protected abstract void initData(Bundle savedInstanceState);

    /**
     * 初始化监听器
     */
    protected abstract void initListener();

    /**
     * 设置当前Activity布局文件
     * 执行在setContentView();之前
     * @return 布局的资源Xml文件ID
     */
    protected abstract int getContentViewId();

    /**
     * 填充数据时机优化, 显示第一帧 填充数据
     */
    private void initProjectFromAsync(final Bundle savedInstanceState) {
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                initData(savedInstanceState);
                initListener();
            }
        });

    }

    protected void goAct(Class<?> clazz) {
        goActWithData(clazz, null);
    }

    protected void goActWithData(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (bundle != null && !bundle.isEmpty())
            intent.putExtras(bundle);
        startActivity(intent);
    }

    public void finish(int animIn, int animOut) {
        super.finish();
        this.overridePendingTransition(animIn, animOut);
    }

    /**
     * @return 当前Activity 的根View
     */
    protected View getRootView() {
        View rootView = ((ViewGroup) (getWindow().getDecorView()
                .findViewById(android.R.id.content)))
                .getChildAt(0);
        return rootView;
    }

    /**
     *  控制点击EditText以外的部分 收回软键盘
     * @param ev
     * @return
     */
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                CommonUtils.closeKeybord(getWindow().getCurrentFocus(), this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }



    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            // 获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            }
        }
        return true;
    }
	
    /**
     * Title 回退按钮的 点击事件
     * @param v 响应回退点击的View
     */
    protected void goBack(View v){
        onBackPressed();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.obj != null && msg.what != 0) {
            //删除了所有消息obj 会被置空, 再实际项目中 需要判断
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().removeActivity(this);
        mHandler.removeCallbacksAndMessages(null);
    }

}
