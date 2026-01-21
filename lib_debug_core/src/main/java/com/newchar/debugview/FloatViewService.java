package com.newchar.debugview;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.newchar.debug.common.utils.Prompt;
import com.newchar.debugview.utils.DebugUtils;

import java.util.Random;

/**
 * @author newChar
 * date 2025/6/10
 * @since 全局唯一调试 View 管理服务
 * @since 迭代版本，（以及描述）
 */
public class FloatViewService extends Service {

    private int mForegroundId = 0;

    /**
     * 暂时用静态描述
     */
    public static IFLowState mCurrFlowState;

    public static void startWindowService(Context context, boolean foreground) {
        Intent intent = new Intent(context, FloatViewService.class);
        intent.putExtra(DebugUtils.FLAG_NEED_FOREGROUND, foreground);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && foreground) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 状态模式对象创建
//        if (DebugUtils.isCanFLow(getApplicationContext())) {
//            mCurrFlowState = new CanFlowState();
//        } else {
//            // 1. 弹提示 Dialog
//            Prompt.show_long("无全局悬浮窗权限");
            mCurrFlowState = new CanNotFlowState();
//        }
        mCurrFlowState.initFlowParams(this);
        mCurrFlowState.loadPlugin();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tryStartForegroundService(intent);
        if (mCurrFlowState != null) {
            mCurrFlowState.showPlugin();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void tryStartForegroundService(Intent intent) {
        if (DebugUtils.isNeedForeground(intent)){
            if (mForegroundId == 0) {
                mForegroundId = new Random().nextInt(100);
            }
            startForeground(mForegroundId, DebugUtils.buildForegroundNotification(getApplicationContext()));
        }
    }

    private void switchState(){
        if (mCurrFlowState == null) {

        } else {
            // 切换另外的
            mCurrFlowState.switchState(new CanNotFlowState());
        }
    }

    private void ensureFlowState(){
        if (mCurrFlowState == null) {
            mCurrFlowState = new CanFlowState();
        }
    }

    private void ensureNotFlowState(){
        if (mCurrFlowState == null) {
            mCurrFlowState = new CanNotFlowState();
        }
    }

}
