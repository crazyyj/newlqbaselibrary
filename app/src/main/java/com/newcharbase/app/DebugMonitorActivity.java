package com.newcharbase.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.newcharbase.jvmti.DebugStackMotion;
import com.newcharbase.jvmti.DebugStackMotionAgent;
import com.newcharbase.jvmti.DebugStackMotionCallback;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

public class DebugMonitorActivity extends AppCompatActivity {
    private final List<String> eventLogs = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Handler handler;

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
            public void onMethodEnter(Class<?> clazz, Method method) {
                addLog("Enter: " + clazz.getName() + "#" + method.getName());
            }
            @Override
            public void onMethodExit(Class<?> clazz, Method method) {
                addLog("Exit: " + clazz.getName() + "#" + method.getName());
            }
            @Override
            public void onVariableChanged(Class<?> clazz, Method method, String varName, Object newValue) {
                addLog("VarChanged: " + clazz.getName() + "#" + method.getName() + " - " + varName + " = " + newValue);
            }
        });
        // 示例注册监控方法
        // DebugStackMotion.registerClassAndMethod(YourClass.class, "yourMethod");
    }

    private void addLog(String log) {
        handler.post(() -> {
            eventLogs.add(0, log);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugStackMotion.release();
    }
}
