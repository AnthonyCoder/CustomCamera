package anthony.cameralibrary;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import anthony.cameralibrary.constant.EPreviewScaleType;
import anthony.cameralibrary.constant.SPConstants;
import anthony.cameralibrary.iml.ICameraHelper;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.iml.IOnFoucusOperation;
import anthony.cameralibrary.util.LogUtils;
import anthony.cameralibrary.util.SPConfigUtil;
import anthony.cameralibrary.util.SizeUtils;
import anthony.cameralibrary.widget.CameraLayout;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;

/**
 * 主要功能：
 * Created by wz on 2018/1/11.
 * 修改历史：
 */

public class CameraManager implements ICameraHelper,IOnFoucusOperation {
    private static CameraManager mInstance;
    private Context mContext;
    private final ICameraHelper mCameraHelper;
    private FlashLigthStatus mLightStatus;
    public static List<FlashLigthStatus> mFlashLightNotSupport = new ArrayList<FlashLigthStatus>();

    private CameraDirection mFlashDirection;
    private ICameraListenner iCameraListenner;
    private Camera mActivityCamera;
    private CameraLayout mCameraLayout;
    private EPreviewScaleType ePreviewScaleType;
    private Camera.Size preSize,vidSize,picSize;

    //屏蔽默认构造方法
    private CameraManager(Context context) {

        mContext = context;

        if (SDK_INT >= GINGERBREAD) {
            mCameraHelper = new CameraHelperGBImpl();
        } else {
            mCameraHelper = new CameraHelperBaseImpl();
        }

        mLightStatus = FlashLigthStatus.valueOf(SPConfigUtil.loadInt(SPConstants.SP_LIGHT_STATUE, FlashLigthStatus.LIGHT_AUTO.ordinal())); //默认 自动
        mFlashDirection = CameraDirection.valueOf(SPConfigUtil.loadInt(SPConstants.SP_CAMERA_DIRECTION, CameraDirection.CAMERA_BACK.ordinal())); //默认后置摄像头
    }

    public static CameraManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (CameraManager.class) {
                if (mInstance == null) {
                    mInstance = new CameraManager(context);
                }
            }
        }
        return mInstance;
    }
    public void initCameraLayout(CameraLayout cameraLayout){
        this.mCameraLayout = cameraLayout;
    }
    public void initScaleType(EPreviewScaleType scaleType){
        this.ePreviewScaleType = scaleType;
    }
    //绑定监听
    public void bindListenner(ICameraListenner iCameraListenner) {
        this.iCameraListenner = iCameraListenner;
    }

    public void unbindListenner() {
        this.iCameraListenner = null;
    }
    public void setActivityCamera(Camera mActivityCamera) {
        this.mActivityCamera = mActivityCamera;
    }


    @Override
    public int getNumberOfCameras() {
        return mCameraHelper.getNumberOfCameras();
    }

    @Override
    public Camera openCameraFacing(int facing) throws Exception {
        Camera camera = mCameraHelper.openCameraFacing(facing);
        mFlashLightNotSupport.clear();
        if (camera != null) {
            List<String> supportFlashModes = camera.getParameters().getSupportedFlashModes();
            if (facing == 0) {
                //某些supportFlashModes  null  不支持
                if (supportFlashModes != null) {
                    if (!supportFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                        mFlashLightNotSupport.add(FlashLigthStatus.LIGHT_AUTO);
                    }
                    if (!supportFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                        mFlashLightNotSupport.add(FlashLigthStatus.LIGHT_ON);
                    }
                }
            }
        }
        return camera;
    }

    @Override
    public boolean hasCamera(int facing) {
        return mCameraHelper.hasCamera(facing);
    }

    @Override
    public void getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo) {
        mCameraHelper.getCameraInfo(cameraId, cameraInfo);
    }

    public boolean hasFrontCamera() {
        return hasCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public boolean hasBackCamera() {
        return hasCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public boolean canSwitch() {
        return hasFrontCamera() && hasBackCamera();
    }

    @Override
    public boolean onPointFocus(Point point, Camera.AutoFocusCallback callback) {
        if (mActivityCamera == null) {
            return false;
        }

        Camera.Parameters parameters = null;
        try {
            parameters = mActivityCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        if(Build.VERSION.SDK_INT >= 14) {

            if (parameters.getMaxNumFocusAreas() <= 0) {
                return focus(callback);
            }

            LogUtils.d( "onCameraFocus:" + point.x + "," + point.y);

            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = point.x - 300;
            int top = point.y - 300;
            int right = point.x + 300;
            int bottom = point.y + 300;
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            parameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
                mActivityCamera.setParameters(parameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }


        return focus(callback);
    }
    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
            mActivityCamera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 闪光灯状态
     */
    public enum FlashLigthStatus {
        LIGHT_AUTO, LIGHT_ON, LIGTH_OFF;

        //不断循环的枚举
        public FlashLigthStatus next() {
            int index = ordinal();
            int len = FlashLigthStatus.values().length;
            FlashLigthStatus status = FlashLigthStatus.values()[(index + 1) % len];
            if (!mFlashLightNotSupport.contains(status.name())) {
                return status;
            } else {
                return next();
            }
        }

        public static FlashLigthStatus valueOf(int index) {
            return FlashLigthStatus.values()[index];
        }
    }

    /**
     * 前置还是后置摄像头
     */
    public enum CameraDirection {
        CAMERA_BACK, CAMERA_FRONT;

        //不断循环的枚举
        public CameraDirection next() {
            int index = ordinal();
            int len = CameraDirection.values().length;
            return CameraDirection.values()[(index + 1) % len];
        }

        public static CameraDirection valueOf(int index) {
            return CameraDirection.values()[index];
        }
    }

    public CameraDirection getCameraDirection() {
        return mFlashDirection;
    }

    //设置拍摄方向 前置/后置
    public void setCameraDirection(CameraDirection mFlashDirection) {
        this.mFlashDirection = mFlashDirection;

        if (iCameraListenner != null) {
            // 记录相机方向  会导致部分相机 前置摄像头
            SPConfigUtil.save(SPConstants.SP_CAMERA_DIRECTION, mFlashDirection.ordinal() + "");
            iCameraListenner.switchCameraDirection(mFlashDirection);
        }
    }

    public FlashLigthStatus getLightStatus() {
        return mLightStatus;
    }

    //设置闪光灯状态
    public void setLightStatus(FlashLigthStatus mLightStatus) {
        this.mLightStatus = mLightStatus;
        if (iCameraListenner != null) {
            // 记录相机方向  会导致部分相机 前置摄像头
            SPConfigUtil.save(SPConstants.SP_LIGHT_STATUE, mLightStatus.ordinal() + "");
            iCameraListenner.switchLightStatus(mLightStatus);
        }
    }
    /**
     * 闪光灯开关   开->关->自动
     */
    public void turnLight(CameraManager.FlashLigthStatus ligthStatus) {
        if (CameraManager.mFlashLightNotSupport.contains(ligthStatus)) {
            turnLight(ligthStatus.next());
            return;
        }

        if (mActivityCamera == null || mActivityCamera.getParameters() == null
                || mActivityCamera.getParameters().getSupportedFlashModes() == null || ligthStatus == null) {
            return;
        }
        Camera.Parameters parameters = mActivityCamera.getParameters();
        List<String> supportedModes = mActivityCamera.getParameters().getSupportedFlashModes();

        switch (ligthStatus) {
            case LIGHT_AUTO:
                if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                }
                break;
            case LIGTH_OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case LIGHT_ON:
                if (supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                break;
        }
        this.mLightStatus = ligthStatus;
        iCameraListenner.switchLightStatus(ligthStatus);
        mActivityCamera.setParameters(parameters);
    }

    //释放camera
    public void releaseCamera() {
        if(mActivityCamera==null){
            return;
        }
        if(mCameraLayout!=null){
            mCameraLayout.getHolder().removeCallback(mCameraLayout);

            mCameraLayout.getCameraSurfaceView().setVisibility(View.INVISIBLE);
        }
            try {
                mActivityCamera.setPreviewCallback(null);
                mActivityCamera.cancelAutoFocus();
                mActivityCamera.stopPreview();
                mActivityCamera.setPreviewDisplay(null);
//                mActivityCamera.stopPreview();
//                mActivityCamera.setPreviewCallback(null);
//                mActivityCamera.setPreviewDisplay(null);
//                mActivityCamera.setPreviewCallbackWithBuffer(null);
                mActivityCamera.release();
                mActivityCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

    }
    public void initDisplayOrientation(Camera.Parameters parameters) {
        if (mActivityCamera == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 14) {
            mActivityCamera.setDisplayOrientation(getDisplayOrientation());
        } else if (Build.VERSION.SDK_INT >= 8) {
            setDisplayOrientation(mActivityCamera, getDisplayOrientation());
        }

        parameters.setRotation(getDisplayOrientation());
        mActivityCamera.setParameters(parameters);


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
    /**
     * 将预览大小设置为屏幕大小
     * @param parameters
     * @return
     */
    public Camera.Size findPreviewSizeByScreen(Camera.Parameters parameters) {

        int viewWidth =mCameraLayout.getWidth();
        int viewHeight = mCameraLayout.getHeight();
        if (viewWidth != 0 && viewHeight != 0) {
            return mActivityCamera.new Size(Math.max(viewWidth, viewHeight),
                    Math.min(viewWidth, viewHeight));
        } else {
            return mActivityCamera.new Size(CameraApplication.mScreenWidth,
                    CameraApplication.mScreenHeight);
        }
    }

    /**
     * 获取预览分辨率
     * @return
     */
    public Camera.Size getPreViewSizeByScaleType(){
        if(preSize !=null){
            return preSize;
        }
        if(ePreviewScaleType == EPreviewScaleType.AJUST_PREVIEW){
            preSize =SizeUtils.getOptimalPreviewSize(mActivityCamera.getParameters().getSupportedPreviewSizes(), mCameraLayout.getWidth(),mCameraLayout.getHeight());
        }
        if(ePreviewScaleType == EPreviewScaleType.AJUST_SCREEN){
            preSize = SizeUtils.getAjustSizeFromScreen(mActivityCamera.getParameters().getSupportedPreviewSizes(), mContext);
        }
        LogUtils.d("预览分辨率  w:"+preSize.width+"   h:"+preSize.height);
        return preSize;
    }
    /**
     * 获取视频保存的分辨率
     * @return
     */
    public Camera.Size getSaveVidSize(){
        if(vidSize !=null){
            return vidSize;
        }
        vidSize = SizeUtils.getAjustSizeFromScreen(mActivityCamera.getParameters().getSupportedVideoSizes(), mContext);

        LogUtils.d("视频分辨率  w:"+vidSize.width+"   h:"+vidSize.height);
        return vidSize;
    }
    /**
     * 获取照片保存的分辨率
     * @return
     */
    public Camera.Size getSavePicSize(){
        if(picSize !=null){
            return picSize;
        }
        picSize = SizeUtils.getAjustSizeFromScreen(mActivityCamera.getParameters().getSupportedPictureSizes(), mContext);
        LogUtils.d("照片分辨率  w:"+picSize.width+"   h:"+picSize.height);
        return picSize;
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
        android.hardware.Camera.getCameraInfo(mFlashDirection.ordinal(), camInfo);

        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
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
        int result;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            int displayOrientation = (camInfo.orientation + degrees) % 360;
            result = (360 - displayOrientation) % 360;
        } else {
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        LogUtils.d(".......角度："+degrees +"....处理后："+result);
        return result;
    }

//    //自适应预览图片尺寸（防止预览画面变形）
//    public void adjustDisplayRatio(Camera.Parameters parameters) {
//        ViewGroup parent = ((ViewGroup) mCameraLayout.getParent());
//        Rect rect = new Rect();
//        parent.getLocalVisibleRect(rect);
//        final int width = rect.width();
//        final int height = rect.height();
//        Camera.Size previewSize = parameters.getPreviewSize();
//        int previewWidth;
//        int previewHeight;
//        if (getDisplayOrientation() == 90 || getDisplayOrientation() == 270) {
//            previewWidth = previewSize.height;
//            previewHeight = previewSize.width;
//        } else {
//            previewWidth = previewSize.width;
//            previewHeight = previewSize.height;
//        }
//        View.OnLayoutChangeListener layoutChangeListener;
//        if (width * previewHeight > height * previewWidth) {
//            final int scaledChildWidth = previewWidth * height / previewHeight;
//            layoutChangeListener = new View.OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    mCameraLayout.layout((width - scaledChildWidth) / 2, 0,
//                            (width + scaledChildWidth) / 2, height);
////                    mCameraLayout.removeOnLayoutChangeListener(this);
//                }
//            };
//
//        } else {
//            final int scaledChildHeight = previewHeight * width / previewWidth;
//            layoutChangeListener = new View.OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    mCameraLayout.layout(0, (height - scaledChildHeight) / 2,
//                            width, (height + scaledChildHeight) / 2);
////                    mCameraLayout.removeOnLayoutChangeListener(this);
//                }
//            };
//        }
//        mCameraLayout.addOnLayoutChangeListener(layoutChangeListener);
//    }
//    private void realeseCamera() {
//        if (mCamera == null) {
//            return;
//        }
//        mCameraLayout.getHolder().removeCallback(mCameraLayout);
//        if (mCamera != null) {
//            try {
//                mCamera.setPreviewCallback(null);
//                mCamera.cancelAutoFocus();
//                mCamera.stopPreview();
//                mCamera.setPreviewDisplay(null);
//            } catch (Exception e) {
//                iCameraListenner.error("释放相机资源失败");
//            }
//            mCamera.release();
//            mCamera = null;
//        }
//    }


}
