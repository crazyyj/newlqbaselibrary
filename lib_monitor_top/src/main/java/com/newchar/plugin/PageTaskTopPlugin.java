package com.newchar.plugin;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.newchar.debug.common.utils.ViewUtils;
import com.newchar.debugview.api.PluginContext;
import com.newchar.debugview.api.ScreenDisplayPlugin;
import com.newchar.debugview.lifecycle.AppLifecycleManager;
import com.newchar.jvmti.DebugStackMotion;
import com.newchar.plugin.toppage.FragmentWrapper;
import com.newchar.plugin.toppage.FragmentXWrapper;
import com.newchar.plugin.view.TaskTopView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author newChar
 * date 2025/6/8
 * @since 当前版本，（以及描述）
 * @since 迭代版本，（以及描述）
 */
public class PageTaskTopPlugin extends ScreenDisplayPlugin {

    public static final String TAG_PLUGIN = "Task_Top";

    private Object mWindowManagerGlobal;
    private Field mWindowViewsField;
    private Field mWindowmParamsField;
    private TaskTopView mTaskTopView;

    @Override
    public String id() {
        return TAG_PLUGIN;
    }

    @Override
    public void onLoad(PluginContext ctx, ViewGroup pluginContainerView) {
        initTaskTopView(pluginContainerView);
        DebugStackMotion.init();
//        initGlobalData();
        initExistActivity();
//        AppLifecycleManager.getInstance().addLifecycleCallback(new DefaultActivityCallback() {
////            private final Map<View, ViewTreeObserver.OnWindowFocusChangeListener> map = new HashMap<>();
//
//            @Override
//            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                super.onActivityCreated(activity, savedInstanceState);
//                mTaskTopView.addActivity(activity);
//
//                hookFragmentLifecycle(activity);
//                hookDialogAndPopLifecycle(activity);
//            }
//
//
//            @Override
//            public void onActivityDestroyed(Activity activity) {
//                super.onActivityDestroyed(activity);
//
//            }
//        });
    }

    private void initTaskTopView(ViewGroup containerView) {
        if (mTaskTopView == null) {
            mTaskTopView = new TaskTopView(containerView.getContext());
        }
        if (mTaskTopView.getParent() instanceof ViewGroup) {
            ViewUtils.removeSelf(mTaskTopView);
        }
        containerView.addView(mTaskTopView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
    }


    private void hookFragmentLifecycle(Activity activity) {
        if (Utils.isAndroidXEnv(activity.getClass())) {
            FragmentXWrapper fragmentXWrapper = new FragmentXWrapper(lifecycle);
            fragmentXWrapper.setFragmentLifeCycle(activity);
        } else {
            FragmentWrapper fragmentWrapper = new FragmentWrapper(lifecycle);
            fragmentWrapper.setFragmentLifecycle(activity);
        }
    }

    private void hookDialogAndPopLifecycle(Activity activity) {
        if (Utils.isAndroidXDialog(activity.getClass())) {

        } else {

        }
    }

    private void initGlobalData() {
        try {
            Class<?> clazz = null;
            if (mWindowManagerGlobal == null) {
                clazz = Class.forName("android.view.WindowManagerGlobal");
                Method methodGetInstance = clazz.getMethod("getInstance");
                mWindowManagerGlobal = methodGetInstance.invoke(null);
            }
            if (mWindowViewsField == null) {
                mWindowViewsField = clazz.getDeclaredField("mViews");
            }
            if (mWindowmParamsField == null) {
                mWindowmParamsField = clazz.getDeclaredField("mParams");
            }
            Log.e("NewChar", "initGlobalData, mWindowManagerGlobal = " + mWindowManagerGlobal);
        } catch (Exception e) {
            // 获取失败. 寻找方案2
        }
    }

    private void initExistActivity() {
        Set<Activity> allActivity = AppLifecycleManager.getInstance().getAllActivity();
        for (Activity activity : allActivity) {
            mTaskTopView.addActivity(activity);
            hookFragmentLifecycle(activity);
            hookDialogAndPopLifecycle(activity);
        }
    }

    @Override
    public void onShow() {
        ViewUtils.setVisibility(mTaskTopView, View.VISIBLE);
    }

    @Override
    public void onHide() {
        ViewUtils.setVisibility(mTaskTopView, View.GONE);
    }

    @Override
    public void onUnload() {
        mWindowManagerGlobal = null;
        mWindowViewsField = null;
        mWindowmParamsField = null;
    }

    private List<View> getAllWindowsViews() {
        final List<View> mViews = new ArrayList<>();
        try {
            if (mWindowViewsField != null && mWindowManagerGlobal != null) {
                mWindowViewsField.setAccessible(true);
                Object viewList = mWindowViewsField.get(mWindowManagerGlobal);
                if (viewList instanceof List<?>) {
                    mViews.addAll(((List<View>) viewList));
                }
            }

        } catch (Exception e) {
        }
        return mViews;
    }

    private final IPageLifecycle lifecycle = new IPageLifecycle() {
        @Override
        public void onPageCreate(Context hostContext, int code, Class<?> pageClass) {
            String childText = pageClass.getCanonicalName() + "\n#" + code;
            mTaskTopView.addFragment(hostContext, childText);
        }

        @Override
        public void onPageVisible(Context hostContext, int code, Class<?> pageClass) {
//            String childText = pageClass.getCanonicalName() + "\n#" + code;
//            mTaskTopView.showFragment(hostContext, childText);
        }

        @Override
        public void onPageGone(Context hostContext, int code, Class<?> pageClass) {
            // 可根据需求更新 TaskTopView 的显示状态
        }

        @Override
        public void onPageDestroy(Context hostContext, int code, Class<?> pageClass) {
            String childText = pageClass.getCanonicalName() + "\n#" + code;
            mTaskTopView.removeFragment(hostContext, childText);
        }
    };

}
