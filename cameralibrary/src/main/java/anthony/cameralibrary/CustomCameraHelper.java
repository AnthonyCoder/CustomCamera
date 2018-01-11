package anthony.cameralibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import anthony.cameralibrary.constant.SPConstants;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.util.LogUtils;
import anthony.cameralibrary.util.SPConfigUtil;
import anthony.cameralibrary.util.SizeUtils;
import anthony.cameralibrary.widget.CameraLayout;
import anthony.cameralibrary.widget.CameraSurfaceView;
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

    //绑定SurfaceView
    public void bind(CameraLayout cameraLayout) {
        LogUtils.d("............bind");
        cameraLayout.getHolder().addCallback(cameraLayout);
        this.mCameraLayout = cameraLayout;
        this.coustomParams = cameraLayout.getCameraParams();
        this.iCameraListenner = coustomParams.iCameraListenner;
        this.context = coustomParams.context;
        this.mCameraManager = CameraManager.getInstance(context);
        this.cameraDirection = mCameraManager.getCameraDirection();
        this.mSensorControler = SensorControler.getInstance();
        mSensorControler.setCameraFocusListener(new SensorControler.CameraFocusListener() {
            @Override
            public void onFocus() {
                Point point = new Point(mCameraLayout.getWidth() / 2, mCameraLayout.getHeight() / 2);

                onCameraFocus(point);
            }
        });
    }

    public void create() {
        LogUtils.d("............create");
        getCameraInstance();
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewDisplay(mCameraLayout.getHolder());
            mCamera.startPreview();
            mCameraManager.setActivityCamera(mCamera);
        } catch (IOException e) {
            iCameraListenner.error("Error setting camera preview: " + e.getMessage());
        }
    }



    public Camera getCameraInstance() {
        if (mCamera == null) {
//            try {
                mCamera = Camera.open();
                parameters = mCamera.getParameters();
                if (coustomParams != null && coustomParams.enableZoom) {
                    coustomParams.maxZoom = (coustomParams.maxZoom > parameters.getMaxZoom() || coustomParams.maxZoom <= 0) ? parameters.getMaxZoom() : coustomParams.maxZoom;
                }
                currenZoom = parameters.getZoom();
                List<String> focusModes = parameters.getSupportedFocusModes();
                for (String mode : focusModes) {
                    if (mode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        break;
                    }
                }
//            } catch (Exception e) {
//                LogUtils.e("");
//                if(iCameraListenner!=null){
//                    iCameraListenner.error("请检查应用是否开启相机权限或是否被其他应用占用相机");
//                }
//            }
        }
        return mCamera;
    }

    public void change() {
        LogUtils.d("............change");
        if (mCamera == null) {
            return;
        }
        int rotation = getDisplayOrientation();
        if (Build.VERSION.SDK_INT >= 14) {
            mCamera.setDisplayOrientation(rotation);
        } else if (Build.VERSION.SDK_INT >= 8) {
            setDisplayOrientation(mCamera, rotation);
        }

        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);
        adjustDisplayRatio(rotation);

    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }

    public void onDestroy() {
        LogUtils.d("............onDestroy");

        realeseCamera();
    }

    private void realeseCamera() {
        if (mCamera == null) {
            return;
        }
        mCameraLayout.getHolder().removeCallback(mCameraLayout);
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.cancelAutoFocus();
                mCamera.stopPreview();
                mCamera.setPreviewDisplay(null);
            } catch (Exception e) {
                iCameraListenner.error("释放相机资源失败");
            }
            mCamera.release();
            mCamera = null;
        }
    }

    public void onResume() {
        LogUtils.d("............destroyed");
        mSensorControler.onStart();
        mCameraLayout.initSoundPool();
        if (mCameraLayout != null && mCameraLayout.getVisibility() == View.INVISIBLE) {
            mCameraLayout.getHolder().addCallback(mCameraLayout);
            mCameraLayout.setVisibility(View.VISIBLE);
        }

    }

    public void onPause() {
        mSensorControler.onStop();
        mCameraLayout.releaseSoundPool();
        LogUtils.d("............onPause");
        if (mCamera == null) {
            return;
        }
        mCameraLayout.setVisibility(View.INVISIBLE);
        realeseCamera();

    }

    /**
     * 用于根据手机方向获得相机预览画面旋转的角度
     * 校正拍照的角度
     *
     * @return 返回适应的角度
     */
    public int getDisplayOrientation() {
        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    //自适应预览图片尺寸（防止预览画面变形）
    private void adjustDisplayRatio(int rotation) {
        ViewGroup parent = ((ViewGroup) mCameraLayout.getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = parameters.getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildWidth = previewWidth * height / previewHeight;
            mCameraLayout.layout((width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            mCameraLayout.layout(0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2);
        }
    }

    //开始拍照
    public boolean takePicture() {
        mCamera.stopPreview();
        if (getSize() == null) {
            return false;
        }
        LogUtils.d("......................w:" + getSize().width + "。。。。。。。h:" + getSize().height);
        parameters.setPictureSize(getSize().width, getSize().height);
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
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                        if (coustomParams.previewImageView != null) {
                            coustomParams.previewImageView.setImageURI(null);
                            coustomParams.previewImageView.setImageURI(outputMediaFileUri);
                        }
                    } catch (FileNotFoundException e) {
                        iCameraListenner.error("File not found: " + e.getMessage());
                    } catch (IOException e) {
                        iCameraListenner.error("Error accessing file: " + e.getMessage());
                    }
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

    //获取分辨率
    @Nullable
    private Camera.Size getSize() {
        int[] sizeList = new int[2];
        if (coustomParams.loadSettingParams) {//本地加载
            if (coustomParams.cameraType == ECameraType.CAMERA_TAKE_PHOTO) {//拍照
                String prefPicSize = SPConfigUtil.load(SPConstants.KEY_PREF_PIC_SIZE, "");
                if (prefPicSize == null || prefPicSize.trim().isEmpty()) {//本地没有 就使用默认或者用户定义的分辨率
                    if (coustomParams.picSize == null) {
                        iCameraListenner.error("相机没有可支持的拍照分辨率参数");
                        return null;
                    }
                    //检查用户参数是否合法 不合法则默认适应屏幕分辨率
                    if (!SizeUtils.isSurpportDrivse(parameters.getSupportedVideoSizes(), coustomParams.picSize.width, coustomParams.picSize.height)) {
                        coustomParams.picSize = SizeUtils.getAjustSizeFromScreen(parameters.getSupportedPictureSizes(), context);
                    }
                }
                return coustomParams.picSize;
            } else if (coustomParams.cameraType == ECameraType.CAMERA_VIDEO) {//拍视频
                String prefVideoSize = SPConfigUtil.load(SPConstants.KEY_PREF_VIDEO_SIZE, "");
                if (prefVideoSize == null || prefVideoSize.trim().isEmpty()) {
                    if (coustomParams.vidSize == null) {
                        iCameraListenner.error("相机没有可支持的视频分辨率参数");
                        return null;
                    }
                    if (!SizeUtils.isSurpportDrivse(parameters.getSupportedVideoSizes(), coustomParams.vidSize.width, coustomParams.vidSize.height)) {
                        coustomParams.vidSize = SizeUtils.getAjustSizeFromScreen(parameters.getSupportedVideoSizes(), context);
                    }

                } else {
                    try {
                        String[] s = prefVideoSize.split("x");
                        coustomParams.vidSize.width = Integer.parseInt(s[0].trim());
                        coustomParams.vidSize.height = Integer.parseInt(s[1].trim());
                    } catch (Exception e) {
                        coustomParams.vidSize = SizeUtils.getAjustSizeFromScreen(parameters.getSupportedVideoSizes(), context);
                    }
                    if (!SizeUtils.isSurpportDrivse(parameters.getSupportedVideoSizes(), coustomParams.vidSize.width, coustomParams.vidSize.height)) {
                        coustomParams.vidSize = SizeUtils.getAjustSizeFromScreen(parameters.getSupportedVideoSizes(), context);
                    }
                }

                return coustomParams.vidSize;
            }
            return null;

        } else {//加载用户配置参数
            if (coustomParams.cameraType == ECameraType.CAMERA_TAKE_PHOTO) {//拍照
                return coustomParams.picSize;
            } else if (coustomParams.cameraType == ECameraType.CAMERA_VIDEO) {//视频
                return coustomParams.vidSize;
            }
            return null;
        }
    }

    /**
     * 开始录制
     *
     * @return
     */
    private boolean startRecording() {
        if (prepareVideoRecorder()) {
//            try{
            mMediaRecorder.start();
//            }catch (Exception e){
//                iCameraListenner.error("启动相机失败"+e.getMessage());
//                return false;
//            }
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
        mCamera = getCameraInstance();
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
        if (getSize() == null) {
            return false;
        }
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        Log.e("相机", "......................w:" + getSize().width + "。。。。。。。h:" + getSize().height);
        mMediaRecorder.setVideoSize(getSize().width, getSize().height);

        mMediaRecorder.setOutputFile(getOutputMediaFile(ECameraType.CAMERA_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(mCameraLayout.getHolder().getSurface());

        int rotation = getDisplayOrientation();
        mMediaRecorder.setOrientationHint(rotation);
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
                takePicture();
                return true;
            } else if (coustomParams.cameraType == ECameraType.CAMERA_VIDEO) {//录制视频
                return startRecording();
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
                        currenZoom = zoom;
                        //将最后一次的距离设为当前距离
                        startDis = endDis;
                    }
                }
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                if (mode != MODE_ZOOM) {
                    //设置聚焦
                    Point point = new Point((int) motionEvent.getX(), (int) motionEvent.getY());
                    onCameraFocus(point);
                } else {

                }
                break;
        }
        return true;
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
     * @param point
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
                        if (mFocusImageView != null) {
                            mFocusImageView.startFocus(point);
                        }
                        //播放对焦音效
                        mCameraLayout.playSound();
                    }
                }
            }
        }, delayDuration);
    }

    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            if(mFocusImageView!=null){
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
