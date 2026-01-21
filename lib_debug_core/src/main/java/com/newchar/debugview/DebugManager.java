package com.newchar.debugview;

import android.app.Activity;
import android.app.Application;

import com.newchar.debugview.api.PluginManager;
import com.newchar.debugview.api.ScreenDisplayPlugin;
import com.newchar.debugview.lifecycle.AppLifecycleManager;
import com.newchar.debugview.utils.DebugUtils;
import com.newchar.debugview.view.DebugView;
import com.newchar.debugview.view.DebugViewAddRemoveHooker;

/**
 * @author newChar
 * date 2022/6/15
 * @since 调试工具入口。
 * @since 迭代版本，（以及描述）
 */
public class DebugManager {


    private static volatile DebugManager mDebugManager;
    private DebugViewAddRemoveHooker mDebugViewAddRemoveHooker;

    private DebugManager() {
    }

    public static DebugManager getInstance() {
        if (mDebugManager == null) {
            synchronized (DebugManager.class) {
                if (mDebugManager == null) {
                    mDebugManager = new DebugManager();
                }
            }
        }
        return mDebugManager;
    }

    /**
     * 需要在第一个Activity露出之前，进行初始化
     *
     * @param app Application
     */
    public void initialize(Application app) {
        DebugUtils.attachApp(app);
        app.registerActivityLifecycleCallbacks(AppLifecycleManager.getInstance());
        if (mDebugViewAddRemoveHooker == null) {
            mDebugViewAddRemoveHooker = new DebugViewAddRemoveHooker();
        }
        AppLifecycleManager.getInstance().addLifecycleCallback(mDebugViewAddRemoveHooker);
        AppLifecycleManager.getInstance().addAppCloseCallback(mPluginRegisterCallback);
    }

//    public DebugView getLogView() {
//        return FloatViewService.mCurrFlowState.getDebugView();
//    }

    public DebugView getLogView() {
        return mDebugViewAddRemoveHooker.getLogView();
    }

    public <T extends ScreenDisplayPlugin> T getPlugin(Class<T> pluginClass) {
        return (T) getLogView().getPlugin(pluginClass);
    }

    public void destroy() {
        mDebugManager = null;

//        if (mDebugViewAddRemoveHooker != null) {
//            mDebugViewAddRemoveHooker.release();
//            mDebugViewAddRemoveHooker = null;
//        }
    }

    private final AppLifecycleManager.AppCloseListener mPluginRegisterCallback = new AppLifecycleManager.AppCloseListener() {
        @Override
        public void onAppOpen(Activity firstActivity) {
            try {
                Class<ScreenDisplayPlugin> clazz = (Class<ScreenDisplayPlugin>) Class.forName("com.newchar.debug.logview.LogViewPlugin");
                PluginManager.getInstance().registerOnce(clazz.getSimpleName(), clazz);

                Class<ScreenDisplayPlugin> clazz2 = (Class<ScreenDisplayPlugin>) Class.forName("com.newchar.plugin.PageTaskTopPlugin");
                PluginManager.getInstance().registerOnce(clazz2.getSimpleName(), clazz2);

                Class<ScreenDisplayPlugin> clazz3 = (Class<ScreenDisplayPlugin>) Class.forName("com.newchar.deviceview.DevicesInfoPlugin");
                PluginManager.getInstance().registerOnce(clazz3.getSimpleName(), clazz3);

                // 启动服务
//                FloatViewService.startWindowService(firstActivity.getApplicationContext(), false);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAppClose(Activity lastActivity) {
//                FloatViewService.startWindowService(firstActivity.getApplicationContext(), false);

        }
    };


}
