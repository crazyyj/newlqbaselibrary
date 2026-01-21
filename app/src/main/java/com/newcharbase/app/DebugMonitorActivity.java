package com.newcharbase.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.newchar.monitor.jvmti.DebugStackMotionAgent;
import com.newchar.monitor.jvmti.DebugStackMotionCallback;
import com.newchar.monitor.jvmti.DebugStackMotion;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

public class DebugMonitorActivity extends AppCompatActivity {
    private final List<String> eventLogs = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Handler handler;

    /**
     * 初始化调试监控界面。
     *
     * @param savedInstanceState 状态
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
        setContentView(listView);
        handler = new Handler(Looper.getMainLooper());
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventLogs);
        listView.setAdapter(adapter);

        DebugStackMotionAgent.startAgent();
        DebugStackMotion.setCallback(new DebugStackMotionCallback() {
            @Override
            public void onMethodVisit(Method method, boolean isEnter, Throwable error) {
                String state = isEnter ? "Enter" : "Exit";
                addLog(state + ": " + method.getDeclaringClass().getName() + "#" + method.getName());
            }

            @Override
            public void onVariableVisit(java.lang.reflect.Field field, boolean isSet, Throwable error) {
                String state = isSet ? "Set" : "Get";
                addLog("Var" + state + ": " + field.getDeclaringClass().getName() + "#" + field.getName());
            }
        });
        // 示例注册监控方法
        // DebugStackMotion.registerClassAndMethod(YourClass.class, "yourMethod");
    }

    /**
     * 添加日志并刷新列表。
     *
     * @param log 日志
     */
    private void addLog(String log) {
        handler.post(() -> {
            eventLogs.add(0, log);
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * 释放资源。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugStackMotion.release();
    }
}
