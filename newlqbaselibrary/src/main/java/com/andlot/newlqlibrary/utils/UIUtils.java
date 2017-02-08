package com.andlot.newlqlibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.andlot.newlqlibrary.base.NewLqApplication;

/**
 * Created by newlq on 2016/10/1.
 */

public class UIUtils {

    private UIUtils(){}

    public static Context getContext(){
        return NewLqApplication.getAppContext();
    }

    public static Resources getResources(){
        return getContext().getResources();
    }

    public static Handler getHandler(){
        return NewLqApplication.getmHandler();
    }
    public static boolean isMainThread(){
        return NewLqApplication.getmMainThreadId() == android.os.Process.myTid();
    }


    public static String getString(int strRes){
        return getContext().getString(strRes);
    }

    public static Drawable getDrawable(int DrawableId){
        return getResources().getDrawable(DrawableId);
    }

    /**
     * 获得字符自组
     *
     * @param resId
     * @return arrays.xml中的数组
     */
    public static String[] getStringArray(int resId) {
        return getResources().getStringArray(resId);
    }

    public static void runOnUiThread(Runnable r){
        if (isMainThread()){
            r.run();
        } else {
            getHandler().post(r);
        }
    }

    public static View inflate(int layoutId){
        return View.inflate(getContext(), layoutId, null);
    }

    /**
     * 获得屏幕高度
     * @param context 上下文
     * @return 屏幕高度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     *
     * @param context 上下文
     * @return 屏幕高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 获得状态栏的高度
     * @return 返回状态栏的高度
     */
    public static int getStatusHeight() {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = UIUtils.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 获取当前全屏截图，包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * 获取状态栏高度
     * @param act Activity 对象
     * @return 顶部Y坐标， 相当于高度
     */
    public static int getStatusBarHight(Activity act){
        Rect frame = new Rect();
        act.getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity 当前页面的Activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity)
    {
        View mDecorView = activity.getWindow().getDecorView();
        mDecorView.setDrawingCacheEnabled(true);
        mDecorView.buildDrawingCache();
        Bitmap bmp = mDecorView.getDrawingCache();
        Rect frame = new Rect();
        mDecorView.getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width,
                height - statusBarHeight);
        mDecorView.destroyDrawingCache();
        return bp;

    }
    /**
     * dp转px
     *
     * @param dpVal
     * @return
     */
    public static int dp2px(float dpVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, UIUtils.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @return
     */
    public static int sp2px(float spVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, UIUtils.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param pxVal
     * @return
     */
    public static float px2dp(float pxVal)
    {
        final float scale = UIUtils.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     *
     * @param pxVal
     * @return
     */
    public static float px2sp(float pxVal)   {
        return (pxVal / UIUtils.getResources().getDisplayMetrics().scaledDensity);
    }

    /**
     * 判断是否是平板
     * @return
     */
    public static boolean isTablet() {
        return (UIUtils.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
