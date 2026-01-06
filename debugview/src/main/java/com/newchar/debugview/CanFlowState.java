package com.newchar.debugview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;

import com.newchar.debug.common.utils.UIUtils;
import com.newchar.debug.common.utils.ViewUtils;
import com.newchar.debugview.view.DebugView;

/**
 * @author newChar
 * date 2025/6/18
 * @since 当前版本，（以及描述）
 * @since 迭代版本，（以及描述）
 */
class CanFlowState implements IFLowState {

    private DebugView mDebugView;

    public CanFlowState() {

    }

    @Override
    public void initFlowParams(Context service) {
        initDebugView(service);

        WindowManager windowManager = service.getSystemService(WindowManager.class);
        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, UIUtils.getScreenWidth() / 2,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        params.x = mWindowRect.left;
        params.y = mWindowRect.top;

        windowManager.addView(mDebugView, params);
    }

    @Override
    public DebugView getDebugView() {
        return mDebugView;
    }

    @Override
    public void loadPlugin() {

    }

    @Override
    public void showPlugin() {

    }

    @Override
    public void switchState(IFLowState newState) {

    }

    @Override
    public void onDestroy() {

    }

    private void initDebugView(Context service) {
        if (mDebugView == null) {
            mDebugView = new DebugView(service);
        }
        mDebugView.setLayoutParams(ViewUtils.getDebugViewLayoutParams());
    }

    // 加载插件流程


}
