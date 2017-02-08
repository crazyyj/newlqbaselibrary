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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉标题
        View view = inflater.inflate(getDialogLayoutId(), container);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new NewLqAlterDialog(this.getActivity());
    }

    protected abstract int getDialogLayoutId();



    //T 内部专用提示框
    class NewLqAlterDialog extends AlertDialog{

        public NewLqAlterDialog(Context context) {
            this(context, R.style.NewLqStyle_Dialog);

        }

        public NewLqAlterDialog(Context context, int themeResId) {
            super(context, themeResId);
            setOwnerActivity(((Activity) context));
        }




    }
}
