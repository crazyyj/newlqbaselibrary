package com.andlot.newlqlibrary.widgets;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.andlot.newlqlibrary.utils.IOUtils;
import com.andlot.newlqlibrary.utils.L;
import com.andlot.newlqlibrary.utils.T;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback,
		ShutterCallback {

	private static final String TAG = "CameraView";
	
	private static Camera mCamera;
	private static Camera.Parameters mParameters;
	private SurfaceHolder surfaceHolder;
	private boolean isClickable = true;
	private Activity mActivity;

	public static boolean isPrepared;
	private static int JPEG_QUALITY = 100;
	int cameraId;
	private IOUtils.OnSaveFileListener onSaveFileListener;

	public void setOnSaveFileListener(IOUtils.OnSaveFileListener l) {
		this.onSaveFileListener = l;
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialise(context);
	}

	public CameraView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CameraView(Context context) {
		this(context, null, 0);
	}

	private Handler h = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				isClickable = true;
				h.removeMessages(1);
				break;
			default:
				break;
			}
		}
	};
	
	private void initialise(Context context) {
		initHolder();
		if (context instanceof Activity) {
			mActivity = ((Activity) context);
		}
	}
	
	public void initHolder() {
		surfaceHolder = getHolder();
		surfaceHolder.setKeepScreenOn(true);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		surfaceHolder.setFixedSize(1280, 720);
		surfaceHolder.addCallback(this);
	}


	public void openCamera() {
		resetCamera();
		openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
		try {
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param id
	 *			Camera.CameraInfo.CAMERA_FACING_BACK = 0
	 * 			Camera.CameraInfo.CAMERA_FACING_FRONT = 1
	 * @return 
	 * 			对应的相机对象实例
	 */
	public Camera openCamera(int id){
		if (checkCameraHardware(mActivity)) {
			int cameraNum = Camera.getNumberOfCameras();
			Camera.CameraInfo mInfo = new Camera.CameraInfo();
			for (int i = 0; i< cameraNum; i++) {
				Camera.getCameraInfo(i, mInfo);
				if (mInfo.facing == id) {
					mCamera = Camera.open(i);
					mParameters = mCamera.getParameters();
					cameraId = i;
					break;
	            }
	        }
			if (openCameraListener != null) {
				if (mCamera == null) {
					openCameraListener.openFail();
				}else{
					openCameraListener.openSuccess(mCamera, cameraId);
				}
			}
		}else{
			T.show_short("未检测到相机设备, 请退出重试");
			if (openCameraListener != null)
				openCameraListener.openFail();
		}
		return mCamera;
	}
	
	interface OnOpenCameraListener{
		void openSuccess(Camera c, int cameraId);
		void openFail();
	}
	
	public OnOpenCameraListener openCameraListener;
	
	public void setOnOpenCameraListener(OnOpenCameraListener l){
		this.openCameraListener = l;
	}
	
	/**
	 * 检测是否存在相机
	 * @param context	
	 * @return	
	 */
	private static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	public void initCameraParameters() {
		if (mParameters != null) {
			mParameters.setPictureFormat(ImageFormat.JPEG);
			mParameters.setJpegQuality(JPEG_QUALITY);
			mParameters.setPreviewSize(mParameters.getPreviewSize().width, mParameters.getPreviewSize().height); // 设置预览大小
			mParameters.setPictureSize(mParameters.getPreviewSize().width, mParameters.getPreviewSize().height); // 设置保存的图片尺寸
			mParameters.setPreviewFrameRate(getpreviewframe()); 
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			mParameters.setRotation(getPreviewDegree(mActivity));
		}
		mCamera.setParameters(mParameters);
	}
	
	public int getpreviewframe(){
		int frame = -1;
		List<Integer> supportedPreviewFrameRates = mParameters.getSupportedPreviewFrameRates();
		if (supportedPreviewFrameRates.contains(5)) {
			frame = 5;
		}
		if (frame == -1) {
			frame = supportedPreviewFrameRates.get(supportedPreviewFrameRates.size() / 2);
		}
		return frame;
	}
	
	public Size getPreviewSize(){
		List<Size> previewSizes = mParameters.getSupportedPreviewSizes();
		for (int i = 0; i < previewSizes.size(); i++) {
			Size s = previewSizes.get(i);
			if (s.height == 720 && s.width == 1280) {
				  return s;
			}
		}
		return previewSizes.get(previewSizes.size() / 2);
	}  
	
	
	public void resetCamera() {
		isPrepared = false;
		isClickable = false;
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	
	private String caseId;
	private String saveDir;
	
	public void setFolderDir(String caseId, String path) {
		this.caseId = caseId;
//		saveDir = FileUtil.PROJECT_DIR + caseId + File.separator + path + File.separator;
	}

	public void takePic() {
		if (isPrepared && mCamera != null && isClickable) {
			mCamera.takePicture(this, null, new PictureOverCallback());
			isClickable = false;
			h.sendEmptyMessageDelayed(1, 150);
		}
	}

	// 按下快门后, 的回调
	@Override
	public void onShutter() {
		isPrepared = false;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			openCamera();
			initCameraParameters();
			mCamera.setDisplayOrientation(getPreviewDegree(mActivity));
			mCamera.setPreviewDisplay(holder);
//			mCamera.startPreview();
			isPrepared = true;
		} catch (IOException e) {
			e.printStackTrace();
			resetCamera();
		}
		L.e("surfaceCreated 了");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		L.e("surfaceChanged 了");
		try {
			isPrepared = false;
			if (holder.getSurface() == null){ // preview surface does not exist
				destroyDrawingCache();
				CameraView.this.setVisibility(GONE);
				resetCamera();
		    	return;
		    }
			mCamera.stopPreview();
			mCamera.setDisplayOrientation(getPreviewDegree(mActivity));
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			isPrepared = true;
			isClickable = true;
			requestLayout();
		} catch (IOException e) {e.printStackTrace();resetCamera();}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		destroyDrawingCache();
		resetCamera();
		L.e("surfaceDestroyed 了");
	}

	
	class PictureOverCallback implements PictureCallback {

		String imgPath;
		String imgName;
		boolean saveOver;

		@SuppressWarnings("deprecation")
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (data == null)
				return;
			try {
				camera.setPreviewDisplay(surfaceHolder);
				camera.startPreview();
				isPrepared = true;
//				FileUtil.savePhoto(data, saveDir, onSaveFileListener);
			} catch (IOException e) {
				try {
					mCamera.reconnect();
					T.show_short("连接相机失败, 尝试重新打开");
				} catch (IOException e1) {
					e1.printStackTrace();
					T.show_short("重新打开失败");
				}
				e.printStackTrace();
			}
//			FileUtil.saveBitmap(data, onSaveFileListener);
		}
	}
	

	// 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
	public static int getPreviewDegree(Activity activity) {
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}
		return degree;
	}

}
