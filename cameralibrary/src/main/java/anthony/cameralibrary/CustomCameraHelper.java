package anthony.cameralibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import anthony.cameralibrary.constant.EPreviewScaleType;
import anthony.cameralibrary.constant.EFouceMode;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.util.BitmapUtils;
import anthony.cameralibrary.util.LogUtils;
import anthony.cameralibrary.util.SizeUtils;
import anthony.cameralibrary.widget.CameraLayout;
import anthony.cameralibrary.widget.FocusImageView;

/**
 * 主要功能:辅助类
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public class CustomCameraHelper implements View.OnTouchListener {
    public static CustomCameraHelper instance = new CustomCameraHelper();
    private CameraLayout mCameraLayout;
    private ICameraListenner iCameraListenner;
    private Context context;
    private Camera mCamera;
    private String outputMediaFileType;
    private Uri outputMediaFileUri;
    private MediaRecorder mMediaRecorder;//视频录制对象
    private CameraController.CameraParams coustomParams;
    private Camera.Parameters parameters;

    private SensorControler mSensorControler;
    private CameraManager mCameraManager;
    private CameraManager.CameraDirection cameraDirection = CameraManager.CameraDirection.CAMERA_BACK; //0后置  1前置

    // 触摸屏幕时显示的聚焦图案
    private FocusImageView mFocusImageView;

    //初始模式
    private static final int MODE_INIT = 0;
    // 放大缩小照片模式
    private static final int MODE_ZOOM = 1;

    private int mode = MODE_INIT;// 初始状态
    private float startDis;
    private int currenZoom = 0;

    private EFouceMode mFouceMode;//对焦模式
    private EPreviewScaleType ePreviewScaleType;//比例模式
    private boolean autoFouce = false;
    private boolean pointFouce = false;

    //绑定SurfaceView
    public void bind(CameraLayout cameraLayout) {
        LogUtils.d("............bind");
        cameraLayout.getHolder().addCallback(cameraLayout);
        this.mCameraLayout = cameraLayout;
        this.mFocusImageView = mCameraLayout.getFocusImageView();
        this.coustomParams = cameraLayout.getCameraParams();
        this.iCameraListenner = coustomParams.iCameraListenner;
        this.context = coustomParams.context;
        this.mCameraManager = CameraManager.getInstance(context);
        this.cameraDirection = mCameraManager.getCameraDirection();
        this.mSensorControler = SensorControler.getInstance();
        this.mFouceMode = coustomParams.eFouceMode;
        this.ePreviewScaleType = coustomParams.ePreviewScaleType;
        mCameraManager.initCameraLayout(mCameraLayout);
        mCameraManager.initScaleType(ePreviewScaleType);
        mCameraManager.setLightStatus(coustomParams.flashLigthStatus);
        initFouceModel();
        if (pointFouce || coustomParams.enableZoom) {//如果打开了指定点对焦或者缩放功能则监听触摸事件
            mCameraLayout.setOnTouchListener(this);
        }

        if (autoFouce) {
            mSensorControler.setCameraFocusListener(new SensorControler.CameraFocusListener() {
                @Override
                public void onFocus() {
                    Point point = new Point(mCameraLayout.getWidth() / 2, mCameraLayout.getHeight() / 2);
                    onCameraFocus(point);
                }
            });
        }

    }

    private void initFouceModel() {
        if (mFouceMode == null) {
            return;
        }
        if (mFouceMode == EFouceMode.AUTOPOINTFOUCEMODEL || mFouceMode == EFouceMode.AUTOFOUCEMODEL) {
            autoFouce = true;
            pointFouce = false;
            if (mFouceMode == EFouceMode.AUTOPOINTFOUCEMODEL) {
                pointFouce = true;
            }
        } else if (mFouceMode == EFouceMode.POINTFOUCEMODEL) {
            pointFouce = true;
            autoFouce = false;
        }
    }

    public void create() {
        LogUtils.d("............create");
        if (null == mCamera) {
            setUpCamera();
        }
    }
    //切换相机头
    public void switchCamera() {
        cameraDirection = cameraDirection.next();
        realeseCamera();
//        mCameraManager.releaseCamera(mCameraLayout);

        setUpCamera();
    }
    //切换闪光灯
    public void switchFlashLight(){
        mCameraManager.turnLight(mCameraManager.getLightStatus().next());
    }

    /**
     * 设置当前的Camera 并进行参数设置
     */
    private void setUpCamera() {

        boolean isSwitchFromFront = cameraDirection == CameraManager.CameraDirection.CAMERA_BACK;
        int facing = cameraDirection.ordinal();
        mCameraManager.bindListenner(iCameraListenner);
        try {
            mCamera = mCameraManager.openCameraFacing(facing);
            mSensorControler.restFoucs();
        } catch (Exception e) {
            if (iCameraListenner != null) {
                iCameraListenner.error("启动相机失败");
            }
            e.printStackTrace();
        }
        if (mCamera != null) {
            mCameraManager.setActivityCamera(mCamera);
            mCameraManager.setCameraDirection(cameraDirection);
            if (coustomParams.picSize == null || !SizeUtils.isSurpportDrivse(parameters.getSupportedPictureSizes(), coustomParams.picSize)) {//如果未设置拍照分辨率，则默认系统的
                coustomParams.picSize = mCameraManager.getSavePicSize();
            }
            if (coustomParams.vidSize == null || !SizeUtils.isSurpportDrivse(parameters.getSupportedVideoSizes(), coustomParams.vidSize)) {//如果未设置视频分辨率，则默认系统的
                coustomParams.vidSize = mCameraManager.getSaveVidSize();
            }
            if (coustomParams.preSize == null || !SizeUtils.isSurpportDrivse(parameters.getSupportedPreviewSizes(), coustomParams.preSize)) {//如果未设置预览分辨率，则默认系统的
                coustomParams.preSize = mCameraManager.getPreViewSizeByScaleType();
            }
            mCameraManager.turnLight(mCameraManager.getLightStatus());
            try {
                mCamera.setPreviewDisplay(mCameraLayout.getHolder());
                parameters = mCamera.getParameters();
                currenZoom = parameters.getZoom();
                List<String> focusModes = parameters.getSupportedFocusModes();
                for (String mode : focusModes) {
                    if (mode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        break;
                    }
                }
                try {
                    parameters.setPreviewSize(coustomParams.preSize.width, coustomParams.preSize.height);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (cameraDirection == CameraManager.CameraDirection.CAMERA_FRONT) {
                    mSensorControler.lockFocus();
                } else {
                    mSensorControler.unlockFocus();
                }
                mCameraManager.initDisplayOrientation(parameters);
                mCamera.startPreview();
                if (iCameraListenner != null) {
                    iCameraListenner.switchCameraDirection(cameraDirection);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void change() {
        setUpCamera();
    }

    public void onDestroy() {
        LogUtils.d("............onDestroy");
//        mCameraManager.releaseCamera(mCameraLayout);
        realeseCamera();
    }

    private void realeseCamera() {
        if (mCamera == null) {
            return;
        }
        mCameraLayout.getHolder().removeCallback(mCameraLayout);

        mCameraManager.unbindListenner();
        try {
            mCamera.setPreviewCallback(null);
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(null);
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            iCameraListenner.error("释放相机资源失败");
        }

    }

    public void onResume() {
        LogUtils.d("............destroyed");
        mSensorControler.onStart();
        mCameraLayout.initSoundPool();
        if (mCameraLayout != null && mCameraLayout.getCameraSurfaceView().getVisibility() == View.INVISIBLE) {
            mCameraLayout.getHolder().addCallback(mCameraLayout);
            mCameraLayout.getCameraSurfaceView().setVisibility(View.VISIBLE);
        }

    }

    public void onPause() {
        mSensorControler.onStop();
        mCameraLayout.releaseSoundPool();
        LogUtils.d("............onPause");
        if (mCamera == null) {
            return;
        }
        realeseCamera();
//        mCameraManager.releaseCamera(mCameraLayout);
    }


    //开始拍照
    public boolean takePicture() {
        mCamera.stopPreview();
        parameters.setPictureSize(coustomParams.picSize.width, coustomParams.picSize.height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        try {
            mSensorControler.lockFocus();
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    File pictureFile = getOutputMediaFile(ECameraType.CAMERA_TAKE_PHOTO);
                    if (pictureFile == null) {
                        iCameraListenner.error("Error creating media file, check storage permissions");
                        return;
                    }
                    if (BitmapUtils.saveTakePicFile(data, pictureFile, cameraDirection, coustomParams.eSaveDirectionType)) {
                        if (coustomParams.previewImageView != null) {
                            coustomParams.previewImageView.setImageURI(null);
                            coustomParams.previewImageView.setImageURI(Uri.fromFile(pictureFile));
                        }
                    } else {
                        iCameraListenner.error("拍照失败");
                    }
//                    try {
//                        FileOutputStream fos = new FileOutputStream(pictureFile);
//                        fos.write(data);
//                        fos.close();
//                        if (coustomParams.previewImageView != null) {
//                            coustomParams.previewImageView.setImageURI(null);
//                            coustomParams.previewImageView.setImageURI(Uri.fromFile(pictureFile));
//                        }
//                    } catch (FileNotFoundException e) {
//                        iCameraListenner.error("File not found: " + e.getMessage());
//                    } catch (IOException e) {
//                        iCameraListenner.error("Error accessing file: " + e.getMessage());
//                    } catch (Exception e) {
//                        iCameraListenner.error("拍照失败,msg:" + e.getMessage());
//                    }

                }
            });
        } catch (Throwable t) {
            try {
                mCamera.startPreview();
            } catch (Throwable e) {
                iCameraListenner.error("Error takepic: " + e.getMessage());
                e.printStackTrace();
            }

            return false;
        }
        try {
            mCamera.startPreview();
        } catch (Throwable e) {
            iCameraListenner.error("Error takepic: " + e.getMessage());
            e.printStackTrace();
        }
        return true;

    }


    /**
     * 开始录制
     *
     * @return
     */
    private boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    //停止录制
    public void stopRecording() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
            } catch (RuntimeException stopException) {
                //handle cleanup here
            }

            if (coustomParams.previewImageView != null) {//预览视频第一帧的图片
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(outputMediaFileUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                coustomParams.previewImageView.setImageBitmap(thumbnail);
            }

        }
        releaseMediaRecorder();
    }

    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    //准备视频录制
    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();
        mCamera.stopPreview();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        } catch (Exception e) {
            iCameraListenner.error("请检查视频录制权限是否打开");
            return false;
        }
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        Log.e("相机", "......................w:" + coustomParams.vidSize.width + "。。。。。。。h:" + coustomParams.vidSize.height);
        mMediaRecorder.setVideoSize(coustomParams.vidSize.width, coustomParams.vidSize.height);

        mMediaRecorder.setOutputFile(getOutputMediaFile(ECameraType.CAMERA_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(mCameraLayout.getHolder().getSurface());

        mMediaRecorder.setOrientationHint(mCameraManager.getDisplayOrientation());
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            iCameraListenner.error("IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            iCameraListenner.error("IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    //释放录制对象实例
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    /**
     * 打开相机开始拍照或者录制
     *
     * @return 是否处于录制中的状态
     */
    public boolean startCamera() {
        if (coustomParams.cameraType != null) {
            if (coustomParams.cameraType == ECameraType.CAMERA_TAKE_PHOTO) {//拍照
                if (coustomParams.picSize != null) {
                    takePicture();
                    mSensorControler.unlockFocus();
                }
                return true;
            } else if (coustomParams.cameraType == ECameraType.CAMERA_VIDEO) {//录制视频
                if (coustomParams.vidSize != null) {
                    return startRecording();
                } else {
                    return false;
                }

            }
        }
        return false;
    }

    /**
     * 获取输出文件
     *
     * @param type 照片 或者视频
     * @return 输出文件
     */
    private File getOutputMediaFile(ECameraType type) {
        String dirPath;
        if (coustomParams.dirName == null) {
            dirPath = "default";
        } else {
            dirPath = coustomParams.dirName;
        }
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), dirPath + File.separator + (type == ECameraType.CAMERA_TAKE_PHOTO ? "image" : "video"));
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                iCameraListenner.error("failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == ECameraType.CAMERA_TAKE_PHOTO) {
            if (coustomParams.path != null) {
                mediaFile = new File(coustomParams.path);
            } else {
                String fileName = coustomParams.fileName == null ? "IMG_" + timeStamp + ".jpg" : coustomParams.fileName;
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + File.separator +
                        fileName);
            }

            outputMediaFileType = "image/*";
        } else if (type == ECameraType.CAMERA_VIDEO) {
            String fileName = coustomParams.fileName == null ? "VID_" + timeStamp + ".mp4" : coustomParams.fileName;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + File.separator +
                    fileName);
            outputMediaFileType = "video/*";
        } else {
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);
        LogUtils.d(".......路径：" + mediaFile.getAbsolutePath());
        return mediaFile;
    }

    /**
     * 获取CoustomCameraHelper实例
     *
     * @return 辅助类实例
     */
    public static CustomCameraHelper getInstance() {
        return instance;
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN:
                mode = MODE_INIT;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE_ZOOM;
                /** 计算两个手指间的距离 */
                startDis = SizeUtils.fingerSpacing(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_ZOOM) {
                    //只有同时触屏两个点的时候才执行
                    if (motionEvent.getPointerCount() < 2) return true;
                    float endDis = SizeUtils.fingerSpacing(motionEvent);// 结束距离
                    //每变化10f zoom变1
                    int scale = (int) ((endDis - startDis) / 10f);
                    if (scale >= 1 || scale <= -1) {
                        int zoom = currenZoom + scale;
                        //zoom不能超出范围
                        if (zoom > coustomParams.maxZoom) zoom = coustomParams.maxZoom;
                        if (zoom < 0) zoom = 0;
                        if (coustomParams.enableZoom) {
                            setZoom(zoom);
                        }
                        //将最后一次的距离设为当前距离
                        startDis = endDis;
                    }
                }
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                if (mode != MODE_ZOOM && pointFouce) {//不是缩放模式且打开了定点对焦时候对焦
                    //设置聚焦
                    Point point = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
                    onCameraFocus(point);
                } else {

                }
                break;
        }
        return true;
    }

    //获取最大缩放级别
    public int getMaxZoom() {
        if (mCamera == null) {
            return 0;
        }
        return mCamera.getParameters().getMaxZoom();
    }

    //缩放相机
    public void setZoom(int zoom) {
        if (mCamera == null) return;
        Camera.Parameters parameters;
        LogUtils.d(".....缩放当前 " + zoom + "  ..最大缩放" + coustomParams.maxZoom);
        //注意此处为录像模式下的setZoom方式。在Camera.unlock之后，调用getParameters方法会引起android框架底层的异常
        //stackoverflow上看到的解释是由于多线程同时访问Camera导致的冲突，所以在此使用录像前保存的mParameters。
        parameters = mCamera.getParameters();

        if (coustomParams != null && coustomParams.enableZoom) {
            coustomParams.maxZoom = (coustomParams.maxZoom > parameters.getMaxZoom() || coustomParams.maxZoom < 0) ? parameters.getMaxZoom() : coustomParams.maxZoom;
        }
        if (!parameters.isZoomSupported()) return;
        parameters.setZoom(zoom);
        mCamera.setParameters(parameters);
        currenZoom = zoom;
    }

    //相机对焦  默认不需要延时
    private void onCameraFocus(final Point point) {
        onCameraFocus(point, false);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    /**
     * 相机对焦
     *
     * @param point     对焦点
     * @param needDelay 是否需要延时
     */
    public void onCameraFocus(final Point point, boolean needDelay) {
        long delayDuration = needDelay ? 300 : 0;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSensorControler.isFocusLocked()) {
                    if (mCameraManager.onPointFocus(point, autoFocusCallback)) {
                        mSensorControler.lockFocus();
                        if (mFocusImageView != null && coustomParams.showFouceImg) {
                            mFocusImageView.startFocus(point);
                        }
                        if (coustomParams.openFouceVic) {
                            //播放对焦音效
                            mCameraLayout.playSound();
                        }

                    }
                }
            }
        }, delayDuration);
    }

    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            if (mFocusImageView != null && coustomParams.showFouceImg) {
                if (success) {
                    mFocusImageView.onFocusSuccess();
                } else {
                    //聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
                    mFocusImageView.onFocusFailed();
                }
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //一秒之后才能再次对焦
                    mSensorControler.unlockFocus();
                }
            }, 1000);
        }
    };

    public CameraController.CameraParams getCoustomParams() {
        return coustomParams;
    }
}
