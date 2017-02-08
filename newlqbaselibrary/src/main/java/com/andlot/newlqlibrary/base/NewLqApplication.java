package com.andlot.newlqlibrary.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;

/**
 * Created by Newlq on 2015/12/30.
 */

public abstract class NewLqApplication extends Application {

    private static Context mAppContext;

    private static Handler mHandler;

    private static Thread mMainThread;

    private static int mMainThreadId;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();
        mHandler = new Handler();
		mMainThread = Thread.currentThread();
        mMainThreadId = android.os.Process.myTid();

        mMainThread.setUncaughtExceptionHandler(CrashHandler.getInstance());

        initOtherJar();

        initProject();
    }



    /**
     * 初始化核心第三方支持库一类的操作， 无返回值
     */
    public void initOtherJar() {}

    /**
     * 默认登录, 获取本地getFileDir()路径 中的 *.properties 文件中的 文件信息.去登录
     * 没有则默认进入 不登陆
     * 初始化当前工程 例如默认登录等。有一个Object返回值
     */
    protected abstract Object initProject();

    //低内存
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.exit(0);
    }

    //充足内存
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    public static Context getAppContext() {
        return mAppContext;
    }

    public static Handler getmHandler() {
        return mHandler;
    }

    public static Thread getmMainThread() {
        return mMainThread;
    }

    public static int getmMainThreadId() {
        return mMainThreadId;
    }
}
