package com.andlot.newlqlibrary.utils;

import android.graphics.Color;
//import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.andlot.newlqlibrary.R;
import com.andlot.newlqlibrary.base.NewLqApplication;

/**
 * Tip 工具类， 更符合项目要求的系统Toast功能
 */
public class T {

	private T(){}
	private static Toast t;

	public static void show_short(String msg){
			if (t == null) {
				t = Toast.makeText(UIUtils.getContext(), msg, Toast.LENGTH_SHORT);
			}else{
				t.cancel();
				t = null;
				t = Toast.makeText(NewLqApplication.getAppContext(), msg, Toast.LENGTH_SHORT);
			}
		t.show();
	}

	public static void show_long(String msg){
			if (t == null) {
				t = Toast.makeText(UIUtils.getContext(), msg, Toast.LENGTH_LONG);
			}else{
				t.setDuration(Toast.LENGTH_LONG);
				t.setText(msg);
			}
		t.show();
	}
//
//	private static Snackbar snackBar;
//
//	public static void showSnackBar(View viewArgs, String toast) {
//		if (snackBar != null) {
//			snackBar.dismiss();
//			snackBar = null;
//		}
//		if (viewArgs == null) {
//			return;
//		}
//		snackBar = Snackbar.make(viewArgs, toast, Snackbar.LENGTH_SHORT);
//		snackBar.setActionTextColor(UIUtils.getResources().getColor(R.color.snaker_bg)).setDuration(Snackbar.LENGTH_SHORT);
//		View view = snackBar.getView();
//		TextView text = (TextView) view.findViewById(R.id.snackbar_text);
//		text.setText(toast);
//		text.setTextColor(Color.WHITE);
//		init(view);
//	}
//
//	public static void showSnackBar(View viewArgs, int toast) {
//		if (snackBar != null) {
//			snackBar.dismiss();
//			snackBar = null;
//		}
//		if (viewArgs == null) {
//			return;
//		}
//		snackBar = Snackbar.make(viewArgs, toast, Snackbar.LENGTH_SHORT);
//		snackBar.setActionTextColor(NewLqApplication.getAppContext().getResources().getColor(R.color.snaker_bg)).setDuration(Snackbar.LENGTH_SHORT);
//		View view = snackBar.getView();
//		TextView text = (TextView) view.findViewById(R.id.snackbar_text);
//		text.setText(toast);
//		text.setTextColor(Color.WHITE);
//		init( view);
//	}
//
//	private static void init(View view) {
//		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
//		view.setBackgroundColor(NewLqApplication.getAppContext().getResources().getColor(R.color.snaker_bg));
//		layoutParams.setMargins(30, 0, 30, UIUtils.dp2px(100));//4个参数按顺序分别是左上右下
//		layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
//		layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
//		view.setLayoutParams(layoutParams);
//		snackBar.show();
//	}
//
//	/**
//	 * 取消吐丝
//	 */
//	public static void dimssToast() {
//		if (snackBar != null) {
//			snackBar.dismiss();
//			snackBar = null;
//		}
//	}

}
