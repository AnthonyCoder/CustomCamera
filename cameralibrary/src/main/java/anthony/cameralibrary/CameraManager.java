package anthony.cameralibrary;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import anthony.cameralibrary.constant.SPConstants;
import anthony.cameralibrary.iml.ICameraHelper;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.iml.IOnFoucusOperation;
import anthony.cameralibrary.util.LogUtils;
import anthony.cameralibrary.util.SPConfigUtil;

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

    //释放camera
    public void releaseCamera() {
        if (mActivityCamera != null) {
            try {
                mActivityCamera.stopPreview();
                mActivityCamera.setPreviewCallback(null);
                mActivityCamera.setPreviewCallbackWithBuffer(null);
                mActivityCamera.release();
                mActivityCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
