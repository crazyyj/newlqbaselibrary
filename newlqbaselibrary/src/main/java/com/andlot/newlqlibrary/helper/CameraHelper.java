package com.andlot.newlqlibrary.helper;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.andlot.newlqlibrary.utils.IOUtils;

public class CameraHelper {

//	private static CameraHelper helper;

	public final static int CROP_IMAGE_REQUESTCODE = 10;
	public final static int OPEN_GALLER_REQUESTCODE = 20;
	public final static int OPEN_CAPTURE_REQUESTCODE = 30;

	private CameraHelper() {
	}

//	public static CameraHelper getIns() {
//		if (helper == null) {
//			synchronized (CameraHelper.class) {
//				helper = (helper == null) ? new CameraHelper() : helper;
//			}
//		}
//		return helper;
//	}

	/**
	 * 调用系统裁剪图片, 一般用于头像上传前
	 * <li>requestCode = 20</li>
	 * @param uri
	 */
	public static void startCROP_IMAGE(Activity act, Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IOUtils.FileType.FILE_TYPE_IMAGE);
		intent.putExtra("crop", "true");
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("outputX", 256);
		intent.putExtra("outputY", 256);
		intent.putExtra("aspectY", 1);
		intent.putExtra("aspectX", 1);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		act.startActivityForResult(intent, CROP_IMAGE_REQUESTCODE);
	}

	public static void openCapture(Activity act, String FileName){
		Intent intentForCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE); // --打开相机
		intentForCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(IOUtils.PROJECT_DIR, FileName)));
		act.startActivityForResult(intentForCapture, OPEN_CAPTURE_REQUESTCODE);
	}
	
	public static void openGaller(Activity act){
		Intent intentFromGallery = new Intent(Intent.ACTION_PICK);	//--相册Action
		intentFromGallery.setType(IOUtils.FileType.FILE_TYPE_IMAGE);						// 设置文件类型
		act.startActivityForResult(intentFromGallery, OPEN_GALLER_REQUESTCODE);
	}
	

}
