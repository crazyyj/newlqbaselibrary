package com.newchar.monitor.jvmti;

public class DebugStackMotionAgent {
    static {
        System.loadLibrary("jvmtiagent"); // 加载 native 层 so 库
    }

    public static void startAgent() {
        startAgentNative();
    }

    public static void stopAgent() {
        stopAgentNative();
    }

    private static native void startAgentNative();
    private static native void stopAgentNative();
}

