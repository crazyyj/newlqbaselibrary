package com.newcharbase.jvmti;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class DebugStackMotion {

    static {
        System.loadLibrary("jvmti");
    }

    private static DebugStackMotionCallback callback;
    private static final Set<Method> monitoredMethods = new HashSet<>();

    public static void registerMethod(Method method) {
        registerMethod(method, false);
    }

    public static void registerMethod(Method method, boolean includeSubclasses) {
        if (method == null) {
            return;
        }
        registerMethod(method.getDeclaringClass(), method, includeSubclasses);
    }

    public static void registerMethod(Class<?> baseClass, Method method, boolean includeSubclasses) {
        if (method == null || baseClass == null) {
            return;
        }
        monitoredMethods.add(method);
        registerMethodNative(baseClass, method, includeSubclasses);
    }

    public static void setCallback(DebugStackMotionCallback cb) {
        callback = cb;
    }

    public static void release() {
        callback = null;
        monitoredMethods.clear();
        clearRegisteredMethodsNative();
        releaseNative();
    }

    // JNI 回调方法，供 native 层调用
    public static void onMethodEnter(String className, String methodName, String methodDesc) {
        if (callback != null) {
            try {
                Class<?> clazz = Class.forName(normalizeClassName(className));
                Method method = findMethod(clazz, methodName, methodDesc);
                if (method != null) {
                    callback.onMethodEnter(clazz, method);
                }
            } catch (ClassNotFoundException ignored) {}
        }
    }

    public static void onMethodExit(String className, String methodName, String methodDesc) {
        if (callback != null) {
            try {
                Class<?> clazz = Class.forName(normalizeClassName(className));
                Method method = findMethod(clazz, methodName, methodDesc);
                if (method != null) {
                    callback.onMethodExit(clazz, method);
                }
            } catch (ClassNotFoundException ignored) {}
        }
    }

    public static void onVariableChanged(String className, String methodName, String methodDesc, String varName, Object newValue) {
        if (callback != null) {
            try {
                Class<?> clazz = Class.forName(normalizeClassName(className));
                Method method = findMethod(clazz, methodName, methodDesc);
                if (method != null) {
                    callback.onVariableChanged(clazz, method, varName, newValue);
                }
            } catch (ClassNotFoundException ignored) {}
        }
    }

    private static String normalizeClassName(String signature) {
        if (signature == null || signature.isEmpty()) {
            return signature;
        }
        String className = signature;
        if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
            className = className.substring(1, className.length() - 1);
        }
        return className.replace('/', '.');
    }

    // 通过方法名和描述符查找 Method 对象
    private static Method findMethod(Class<?> clazz, String methodName, String methodDesc) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName) && methodDesc != null) {
                String desc = getMethodDescriptor(m);
                if (desc.equals(methodDesc)) {
                    return m;
                }
            }
        }
        return null;
    }

    // 获取方法的 JVM 描述符
    private static String getMethodDescriptor(Method method) {
        StringBuilder desc = new StringBuilder();
        desc.append("(");
        for (Class<?> paramType : method.getParameterTypes()) {
            desc.append(getTypeDescriptor(paramType));
        }
        desc.append(")");
        desc.append(getTypeDescriptor(method.getReturnType()));
        return desc.toString();
    }

    private static String getTypeDescriptor(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == void.class) return "V";
            else if (clazz == int.class) return "I";
            else if (clazz == boolean.class) return "Z";
            else if (clazz == byte.class) return "B";
            else if (clazz == char.class) return "C";
            else if (clazz == short.class) return "S";
            else if (clazz == long.class) return "J";
            else if (clazz == float.class) return "F";
            else if (clazz == double.class) return "D";
        } else if (clazz.isArray()) {
            return clazz.getName().replace('.', '/');
        } else {
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
        return "";
    }

    // native 方法声明
    private static native void registerMethodNative(Class<?> baseClass, Method method, boolean includeSubclasses);

    private static native void clearRegisteredMethodsNative();

    private static native void releaseNative();
}
