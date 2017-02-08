package com.andlot.newlqlibrary.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by newlq on 2016/12/9.
 */

public class TitleBarView extends RelativeLayout {

    private TextView tv_title;
    private TextView left_text_1;
    private TextView left_text_2;

    private ImageView iv_leftIcon;

    public TitleBarView(Context context) {
        this(context, null);
    }

    public TitleBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initTitleBar();
    }

    private void initTitleBar() {

    }
}
