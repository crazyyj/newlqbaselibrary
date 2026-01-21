package com.newchar.deviceview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.newchar.deviceview.devices.Strict;
import com.newchar.debug.common.utils.ViewUtils;
import com.newchar.debugview.api.PluginContext;
import com.newchar.debugview.api.ScreenDisplayPlugin;

/**
 * @author newChar
 * date 2025/6/18
 * @since 设备调试入口插件
 * @since 迭代版本，（以及描述）
 */
public class DevicesInfoPlugin extends ScreenDisplayPlugin {

    public static final String TAG_PLUGIN = "DEVICE_DEBUG";

    private ViewGroup mContainerView;

    @Override
    public String id() {
        return TAG_PLUGIN;
    }

    /**
     * 插件加载时创建调试入口界面。
     *
     * @param ctx                插件上下文
     * @param pluginContainerView 插件容器
     */
    @Override
    public void onLoad(PluginContext ctx, ViewGroup pluginContainerView) {
        mContainerView = buildContainer(pluginContainerView.getContext());
        pluginContainerView.addView(mContainerView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 显示插件视图。
     */
    @Override
    public void onShow() {
        ViewUtils.setVisibility(mContainerView, View.VISIBLE);
    }

    /**
     * 隐藏插件视图。
     */
    @Override
    public void onHide() {
        ViewUtils.setVisibility(mContainerView, View.GONE);
    }

    /**
     * 插件卸载时释放引用。
     */
    @Override
    public void onUnload() {
        mContainerView = null;
    }

    /**
     * 构建包含调试入口按钮的容器。
     *
     * @param context 上下文
     * @return 容器视图
     */
    private ViewGroup buildContainer(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(buildActionView(context, "开启严格模式", v -> Strict.startAll()));
        layout.addView(buildActionView(context, "关闭严格模式", v -> Strict.stopAll()));
        layout.addView(buildActionView(context, "开发者选项", v ->
                openPage(v, new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))));
        layout.addView(buildActionView(context, "应用设置", v ->
                openPage(v, buildAppSettingsIntent(v.getContext()))));
        layout.addView(buildActionView(context, "系统设置", v ->
                openPage(v, new Intent(Settings.ACTION_SETTINGS))));
        return layout;
    }

    /**
     * 创建可点击的入口文本。
     *
     * @param context  上下文
     * @param title    标题
     * @param listener 点击回调
     * @return 文本视图
     */
    private View buildActionView(Context context, String title, View.OnClickListener listener) {
        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setPadding(24, 20, 24, 20);
        textView.setOnClickListener(listener);
        return textView;
    }

    /**
     * 构建当前应用的设置页 Intent。
     *
     * @param context 上下文
     * @return Intent
     */
    private Intent buildAppSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    /**
     * 打开调试入口页面。
     *
     * @param view   触发视图
     * @param intent 目标 Intent
     */
    private void openPage(View view, Intent intent) {
        Context context = view.getContext();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "无法打开页面", Toast.LENGTH_SHORT).show();
        }
    }
}
