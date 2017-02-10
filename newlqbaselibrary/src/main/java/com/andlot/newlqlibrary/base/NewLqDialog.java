package com.andlot.newlqlibrary.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.andlot.newlqlibrary.R;

/**
 * Created by newlq on 2016/10/13.
 *
 */

public abstract class NewLqDialog extends DialogFragment {

    /**
     *
     */
    protected static final int CREATE_TYPE_DIALOG = 0;

    protected static final int CREATE_TYPE_VIEW = 1;

    protected int CREATE_TYPE_DEFAULT ;

    public NewLqDialog(int type) {
        CREATE_TYPE_DEFAULT = type;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉标题
        View view = inflater.inflate(getDialogLayoutId(), container);
        initWidgets(view);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        NewLqAlterDialog dialog = getNewlqDialog();
        return dialog;
    }

    protected abstract int getDialogLayoutId();
    protected abstract int initWidgets(View view);

    protected abstract NewLqAlterDialog getNewlqDialog();




    //T 内部专用提示框
    public class NewLqAlterDialog extends AlertDialog{

        public NewLqAlterDialog(Context context) {
            this(context, R.style.NewLqStyle_Dialog);
        }

        public NewLqAlterDialog(Context context, int themeResId) {
            super(context, themeResId);
            setOwnerActivity(((Activity) context));
        }

    }
}
