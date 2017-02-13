package com.andlot.newlqlibrary.helper;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Environment;

import com.andlot.newlqlibrary.utils.IOUtils;
import com.andlot.newlqlibrary.utils.T;
import com.andlot.newlqlibrary.utils.UIUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author newlq
 * 将Bitmap存到本地的工具类
 */
public class SaveBitmapTask extends AsyncTask<String, Integer, Boolean> {

    //图片的质量
    private static final int Image_Quality = 100;

    //需要保存的保存的图片
    private Bitmap mBitmap;
    //文件夹路径
    private String savePath;
    private File imageFile;

    public SaveBitmapTask(Bitmap bm) {
        mBitmap = bm;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                UIUtils.getContext().getPackageName() + File.separator;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean bmSaveSuccess = false;
        OutputStream stream = null;
        //TODO 判断存储位置可用性

        if (mBitmap != null) {
            try {
                File filePath = new File(savePath + buildTargetDir(params));
                if (filePath.exists() || filePath.mkdirs()){
                    imageFile = new File(filePath, params[params.length - 1]);
                    stream = new FileOutputStream(imageFile);
                    bmSaveSuccess = mBitmap.compress(CompressFormat.JPEG, Image_Quality, stream) && imageFile.exists();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(stream);
            }
        }
        return bmSaveSuccess;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        T.show_short(result ? "图像保存成功" : "图像保存失败");
        if (result && imageFile != null && imageFile.exists()) {
            if (mSaveFinishListener != null) {
                mSaveFinishListener.saveFinishListener(imageFile.getAbsolutePath());
            }
        }
        onRecycle();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mProgressUpdateListener != null)
            mProgressUpdateListener.progressUpdateListener(values);
    }

    private void onRecycle(){
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
            System.gc();
        }
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        onRecycle();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        onRecycle();
    }

    /**
     * 构建文件夹路径字符串
     * @param dirs 文件夹名称数组
     * @return
     */
    private String buildTargetDir(String[] dirs){
        String temp_dir = "";
        for (int i = 0; i < dirs.length - 1; i++){
            temp_dir += dirs[i] + File.separator;
        }
        return temp_dir;
    }

    private SaveFinishListener mSaveFinishListener;
    private ProgressUpdateListener mProgressUpdateListener;

    public void setSaveFinishListener(SaveFinishListener saveFinishListener) {
        this.mSaveFinishListener = saveFinishListener;
    }

    public void setProgressUpdateListener(ProgressUpdateListener progressUpdateListener) {
        this.mProgressUpdateListener = progressUpdateListener;
    }

    public interface ProgressUpdateListener{
        void progressUpdateListener(Integer... values);
    }

    public interface SaveFinishListener{
        void saveFinishListener(String path);
    }
}
