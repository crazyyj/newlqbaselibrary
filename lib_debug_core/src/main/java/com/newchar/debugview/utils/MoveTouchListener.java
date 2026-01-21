package com.newchar.debugview.utils;

import android.view.MotionEvent;
import android.view.View;

/**
 * @author newChar
 * date 2025/6/6
 * @since 简单跟随移动TouchListener
 * @since 迭代版本，（以及描述）
 */
public class MoveTouchListener implements View.OnTouchListener {

    private View mControl;

    public MoveTouchListener() {
    }

    public MoveTouchListener(View control) {
        mControl = control;
    }

    private float moveX;
    private float moveY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mControl == null) {
                    mControl = v;
                }
                moveX = event.getX();
                moveY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float moveXOffset = event.getX() - moveX;
                float moveYOffset = event.getY() - moveY;
                mControl.setTranslationX(moveXOffset + mControl.getTranslationX());
                mControl.setTranslationY(moveYOffset + mControl.getTranslationY());
                return true;
            case MotionEvent.ACTION_UP:
//                        // 同步变动，实际X，Y位置
//                float curXOffset = getTranslationX();
//                float curYOffset = getTranslationY();
                return true;
            default:
                break;
        }
        return false;
    }
}