package com.newchar.jvmti;

import android.os.Build;
import android.os.Debug;
import android.util.Log;

import java.lang.reflect.Method;

public final class DebugStackMotion {

    private static int init;

    static {
        attachAgentSys("jvmti");
    }

    private static void attachAgentSys(String agentLib) {
        try {
            Log.i("AAA", "准备加载 agent");
            if (init > 0) {
                return;
            }
            init = 1;
            Log.i("AAA", "正在加载 agent");
            System.loadLibrary(agentLib);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Debug.attachJvmtiAgent("lib" + agentLib + ".so", null, Thread.currentThread().getContextClassLoader());
                Log.i("AAA", "加载完成 agent1");
            } else {
                // 三方反射库调用.
                Class<?> vmDebug = Class.forName("dalvik.system.VMDebug");
                java.lang.reflect.Method method = vmDebug.getDeclaredMethod("attachAgent", ClassLoader.class);
                method.setAccessible(true);
                method.invoke(vmDebug, "lib" + agentLib + ".so", Thread.currentThread().getContextClassLoader());
                method.setAccessible(false);
                Log.i("AAA", "加载完成 agent2");
            }
            init = 2;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("AAA", "加载结果 agent3 错误了 \n", e);
            init = -1;
        } finally {
            callbackAttachResult(init);
        }
    }

    /**
     * A native method that is implemented by the 'jvmti' native library,
     * which is packaged with this application.
     * 监听的方法
     */
    public static native void attachClass(Class<?> clazz, Method[] outMethod);

    public static void init(){

    }

    /**
     * 给 jni 提供是否 attach 成功的回调
     *
     * @param result 结果 2 成功
     */
    private static void callbackAttachResult(int result) {
        Log.i("AAA", "加载结果 agent3 " + result);
    }

}