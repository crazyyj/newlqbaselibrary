package com.newchar.touchrestore;

import android.app.Activity;
import android.os.Parcel;
import android.view.MotionEvent;
import android.view.Window;

import com.newchar.debugview.utils.DebugUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author newChar
 * date 2023/9/15
 * @since 当前版本，（以及描述）
 * @since 迭代版本，（以及描述）
 */
class RecordTouchEventTask implements Runnable {

    private final WeakReference<Activity> mHostCallback;

    public RecordTouchEventTask(Activity hostCallback) {
        mHostCallback = new WeakReference<>(hostCallback);
    }

    @Override
    public void run() {
        Activity activity = mHostCallback.get();
        if (activity == null) {
            return;
        }
        try {
            Window.Callback callbackProxy = (Window.Callback) Proxy.newProxyInstance(
                    Window.Callback.class.getClassLoader(),
                    new Class[]{Window.Callback.class}, new hookProxy(mHostCallback));
            activity.getWindow().setCallback(callbackProxy);
        } catch (Exception ignored) {
        }

    }

    final static class hookProxy implements InvocationHandler {
        private static final String METHOD_TOUCH_EVENT = "dispatchTouchEvent";
        private final WeakReference<Activity> mPagRef;
        /**
         * 原始 callback
         */
        private Window.Callback mCallback;
        private File file;
        private OutputStream mOutput;

        public hookProxy(WeakReference<Activity> actRef) throws FileNotFoundException {
            mPagRef = actRef;
            mCallback = actRef.get().getWindow().getCallback();

            file = new File(mPagRef.get().getExternalCacheDir(),
                    "motion");
            if ((file.exists() && file.isDirectory()) || file.mkdirs()) {
                file = new File(file, mPagRef.get().getClass().getSimpleName()
                        + "#" + mPagRef.get().hashCode() + ".rec");
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (METHOD_TOUCH_EVENT.equals(method.getName())
                    && (args != null && args[0] instanceof MotionEvent)) {
                Parcel obtain = Parcel.obtain();
                obtain.setDataPosition(0);
                ((MotionEvent) args[0]).writeToParcel(obtain, 0);
                byte[] marshall = obtain.marshall();
                obtain.recycle();

                DebugUtils.appendBytesToFile(marshall, file);
            }
            Activity activity = mPagRef.get();
            Object result = null;
            if (activity != null) {
                result = method.invoke(mCallback, args);
            } else {
                mCallback = null;
            }
            return result;
        }

    }

}
