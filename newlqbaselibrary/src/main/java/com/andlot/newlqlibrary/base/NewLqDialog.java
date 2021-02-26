package com.andlot.newlqlibrary.base;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by newlq on 2016/10/13.
 */

public class NewLqDialog extends DialogFragment {

    /**
     *
     */
    protected static final int CREATE_TYPE_DIALOG = 0;
    protected static final int CREATE_TYPE_VIEW = 1;
    protected int CREATE_TYPE_DEFAULT;

    @IntDef({CREATE_TYPE_DIALOG, CREATE_TYPE_VIEW})
    @Retention(RetentionPolicy.CLASS)
    public @interface CreateDialogType {
    }


    public NewLqDialog() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (CREATE_TYPE_DEFAULT == CREATE_TYPE_VIEW) {
            int layoutId = getDialogLayoutId();
            if (layoutId > 0) {
                getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉标题
                view = inflater.inflate(layoutId, container);
                initWidgets(view);
            }
        }
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = getNewlqDialog(savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (CREATE_TYPE_DEFAULT == CREATE_TYPE_VIEW || dialog == null) {
            dialog = super.onCreateDialog(savedInstanceState);
        }
        return dialog;
    }

    protected int getDialogLayoutId() {
        return 0;
    }

    protected int initWidgets(View view) {
        return 0;
    }

    protected NewLqAlterDialog getNewlqDialog(Bundle savedInstanceState) {
        return null;
    }


    //Tip: 内部专用提示框
    public class NewLqAlterDialog extends AlertDialog {

        /*public NewLqAlterDialog(Context context) {
//            this(context, );
        }
*/
        public NewLqAlterDialog(Context context, int themeResId) {
            super(context, themeResId);
            setOwnerActivity(((Activity) context));
        }

    }
}
