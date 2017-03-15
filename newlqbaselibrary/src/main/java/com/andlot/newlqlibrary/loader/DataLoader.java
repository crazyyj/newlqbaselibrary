package com.andlot.newlqlibrary.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.CursorLoader;

/**
 * Created by newlq on 2017/3/15.
 */

public class DataLoader<D> extends AsyncTaskLoader<D> {

    private D mD;

    public DataLoader(Context context, D d) {
        super(context);
        mD = d;
    }


    @Override
    public D loadInBackground() {
        return null;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (takeContentChanged() || mD == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        mD = null;
    }

    @Override
    public void deliverResult(D data) {
        if (isReset()) {
            if (mD != null) {
                mD = null;
            }
            return ;
        }
        mD = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

}
