package com.newchar.touchrestore;

import android.app.Activity;
import android.os.Bundle;

import com.newchar.debugview.lifecycle.DefaultActivityCallback;

/**
 * @author newChar
 * date 2023/9/14
 * @since 当前版本，（以及描述）
 * @since 迭代版本，（以及描述）
 */
public final class MotionManager extends DefaultActivityCallback {

    public MotionManager() {
    }

    @Override
    public void onActivityCreated( Activity activity, Bundle savedInstanceState) {
        super.onActivityCreated(activity, savedInstanceState);
        activity.getWindow().getDecorView().post(new RecordTouchEventTask(activity));
    }

    @Override
    public void onActivityResumed( Activity activity) {
        super.onActivityResumed(activity);

    }

    @Override
    public void onActivityStopped( Activity activity) {
        super.onActivityStopped(activity);
        // 暂停采集
    }

    @Override
    public void onActivityDestroyed( Activity activity) {
        super.onActivityDestroyed(activity);
        // 完全停止，回收资源
    }

}
