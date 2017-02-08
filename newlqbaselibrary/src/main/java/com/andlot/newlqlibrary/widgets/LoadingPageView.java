package com.andlot.newlqlibrary.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by newlq on 2016/12/9.
 */

public class LoadingPageView extends RelativeLayout {


    public LoadingPageView(Context context) {
        this(context, null);
    }

    public LoadingPageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
