package com.newcharbase.jvmti;

import java.lang.reflect.Method;

public interface DebugStackMotionCallback {
    void onMethodEnter(Class<?> clazz, Method method);
    void onMethodExit(Class<?> clazz, Method method);
    void onVariableChanged(Class<?> clazz, Method method, String varName, Object newValue);
}
