package com.newchar.debugview.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.LogPrinter;

/**
 * @author newChar
 * date 2024/12/21
 * @since 当前版本，（以及描述）
 * @since 迭代版本，（以及描述）
 */
public class HandleWrapper {

    private static HandlerThread mThread1 = new HandlerThread("default_HandlerThread", Process.THREAD_PRIORITY_DEFAULT);
    private static HandlerThread mThread2 = new HandlerThread("bg_HandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
    private static final Handler mMainThread = new Handler(Looper.getMainLooper()/*, (message) -> { return false }*/);

    public static Handler getHandler(Handler.Callback callback) {
        HandlerThread temp = getAliveThread();
        return new Handler(temp.getLooper(), callback);
    }

    private static HandlerThread getAliveThread() {
        if (!mThread1.isAlive()) {
            mThread1.start();
        }
        return mThread1;
    }

    private void sendMainMessage(Runnable r, long time) {
        mMainThread.postDelayed(r, time);
    }

    public void releaseH(Handler a) {
        a.removeCallbacksAndMessages(null);
    }

}
