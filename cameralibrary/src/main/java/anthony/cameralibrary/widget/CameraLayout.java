package anthony.cameralibrary.widget;

import android.app.Activity;
import android.content.Context;
import android.media.SoundPool;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import anthony.cameralibrary.CameraController;
import anthony.cameralibrary.CameraManager;
import anthony.cameralibrary.CustomCameraHelper;
import anthony.cameralibrary.R;
import anthony.cameralibrary.constant.EPreviewScaleType;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.constant.EFouceMode;
import anthony.cameralibrary.constant.ESaveDirectionType;
import anthony.cameralibrary.iml.ICameraListenner;

/**
 * 主要功能：
 * Created by wz on 2018/1/11.
 * 修改历史：
 */

public class CameraLayout extends FrameLayout implements SurfaceHolder.Callback {

    private CameraController.CameraParams cameraParams;
    private FocusImageView focusImageView;
    private CameraSurfaceView cameraSurfaceView;
    private View rootView;

    public CameraLayout(@NonNull Context context) {
        super(context);
    }

    public CameraLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private void initParams(CameraController.CameraParams cameraParams) {
        this.cameraParams = cameraParams;
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.camera_layout, null);
        addView(rootView);
        focusImageView = rootView.findViewById(R.id.img_fouce);
        cameraSurfaceView = rootView.findViewById(R.id.surface_camera);
    }


    public CameraController.CameraParams getCameraParams() {
        return cameraParams;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        CustomCameraHelper.getInstance().create();

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {//大小发生变化时候
        CustomCameraHelper.getInstance().change();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        CustomCameraHelper.getInstance().onDestroy();

    }


    public static class Builder {
        private CameraController.CameraParams P;

        public Builder(Context context, ICameraListenner iCameraListenner) {
            this.P = new CameraController.CameraParams(context, iCameraListenner);
        }


        public Builder setCameraType(ECameraType cameraType) {//设置拍照类型（拍照 or 录像）
            P.cameraType = cameraType;
            return this;
        }

        public Builder setJpegQuality(int quality) {//设置照片质量
            P.quality = quality;
            return this;
        }

        public Builder setOutPutFilePath(String path) {//设置输出路径
            P.path = path;
            return this;
        }

        public Builder setOutPutDirName(String dirName) {//设置输出文件夹
            P.dirName = dirName;
            return this;
        }

        public Builder setFileName(String fileName) {//设置输出文件名
            P.fileName = fileName;
            return this;
        }

//        public Builder setLoadSettingParams(boolean isload) {//设置是否加载本地参数
//            P.loadSettingParams = isload;
//            return this;
//        }

        public Builder setPreviewImageView(ImageView ivPreview) {
            P.previewImageView = ivPreview;
            return this;
        }

        public Builder setPreviewImageView(int previewImgRes) {
            if (P.context != null) {
                P.previewImageView = ((Activity) P.context).findViewById(previewImgRes);
            }
            return this;
        }

        public Builder setZoomEnable(boolean enable) {//值为-1时候表示，使用系统最大缩放
            return setZoomEnable(enable, -1);
        }

        public Builder setZoomEnable(boolean enable, int maxZoom) {
            P.enableZoom = enable;
            P.maxZoom = maxZoom;
            return this;
        }

        public Builder setShowFouceImg(boolean isShow) {//是否显示聚焦图片
            P.showFouceImg = isShow;
            return this;
        }

        public Builder setOpenFouceVic(boolean isOpen) {//是否打开聚焦音效
            P.openFouceVic = isOpen;
            return this;
        }
        public Builder setFouceModel(EFouceMode eFouceMode) {//设置对焦模式
            P.eFouceMode = eFouceMode;
            return this;
        }
        public Builder setEPreviewScaleType(EPreviewScaleType ePreviewScaleType) {//设置比例模式
            P.ePreviewScaleType = ePreviewScaleType;
            return this;
        }

        public Builder setEPreviewScaleType(ESaveDirectionType eSaveDirectionType) {//设置成像模式
            P.eSaveDirectionType = eSaveDirectionType;
            return this;
        }

        public Builder setFlashLigthStatus(CameraManager.FlashLigthStatus flashLigthStatus) {//设置闪光灯模式
            P.flashLigthStatus = flashLigthStatus;
            return this;
        }

        public CameraLayout startCamera() {
            CameraLayout cameraLayout = new CameraLayout(P.context);
            cameraLayout.initParams(P);
            CustomCameraHelper.getInstance().bind(cameraLayout);
            return cameraLayout;
        }
    }

    public SurfaceHolder getHolder() {

        if (cameraSurfaceView == null) {
            return null;
        }
        return cameraSurfaceView.getHolder();
    }

    public FocusImageView getFocusImageView() {
        return focusImageView;
    }

    public CameraSurfaceView getCameraSurfaceView() {
        return cameraSurfaceView;
    }

    public SoundPool initSoundPool(){
        if(cameraSurfaceView == null){
            return null;
        }
        return cameraSurfaceView.initSoundPool();
    }
    public void playSound() {
        if (cameraSurfaceView == null) {
            return;
        }
        playSound(1.0f, 0.5f, 1, 0, 1.0f);
    }

    public void playSound(float leftVolume, float rightVolume, int priority, int loop, float rate) {
        if (cameraSurfaceView == null) {
            return;
        }
        cameraSurfaceView.playSound(leftVolume, rightVolume, priority, loop, rate);

    }

    public void releaseSoundPool() {
        if (cameraSurfaceView == null) {
            return;
        }
        cameraSurfaceView.releaseSoundPool();
    }
}
