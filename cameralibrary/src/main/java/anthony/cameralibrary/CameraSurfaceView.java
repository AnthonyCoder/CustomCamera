package anthony.cameralibrary;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;

/**
 * 主要功能:
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private CameraController.CameraParams cameraParams;
    public CameraSurfaceView(Context context) {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private void initParams(CameraController.CameraParams cameraParams){
        this.cameraParams=cameraParams;
    }
    public CameraController.CameraParams getCameraParams(){
        return cameraParams;
    }
    @Override
    public void surfaceCreated(SurfaceHolder srfaceHolder) {
        CustomCameraHelper.getInstance().create();

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        CustomCameraHelper.getInstance().change();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        CustomCameraHelper.getInstance().destroyed();

    }
    public static class Builder{
        private CameraController.CameraParams P;
        public Builder(Context context,ICameraListenner iCameraListenner){
            this.P=new CameraController.CameraParams(context,iCameraListenner);
        }



        public Builder setCameraType(ECameraType cameraType){//设置拍照类型（拍照 or 录像）
            P.cameraType=cameraType;
            return this;
        }
        public Builder setJpegQuality(int quality){//设置照片质量
            P.quality=quality;
            return this;
        }
        public Builder setOutPutFilePath(String path){//设置输出路径
            P.path=path;
            return this;
        }

        public Builder setOutPutDirName(String dirName){//设置输出文件夹
            P.dirName=dirName;
            return this;
        }

        public Builder setFileName(String fileName){//设置输出文件名
            P.fileName=fileName;
            return this;
        }

        public Builder setLoadSettingParams(boolean isload){//设置是否加载本地参数
            P.loadSettingParams=isload;
            return this;
        }

        public Builder setPreviewImageView(ImageView ivPreview){
            P.previewImageView=ivPreview;
            return this;
        }

        public Builder setPreviewImageView(int previewImgRes){
            if(P.context!=null){
                P.previewImageView=((Activity)P.context).findViewById(previewImgRes);
            }
            return this;
        }


        public CameraSurfaceView startCamera(){
            CameraSurfaceView cameraSurfaceView=new CameraSurfaceView(P.context);
            cameraSurfaceView.initParams(P);
            CustomCameraHelper.getInstance().bind(cameraSurfaceView);
            return cameraSurfaceView;
        }
    }
}
