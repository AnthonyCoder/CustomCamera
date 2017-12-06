package anthony.cameralibrary;

import android.content.Context;
import android.hardware.Camera;
import android.widget.ImageView;

import java.util.List;

import anthony.cameralibrary.constan.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.util.ScreenUtils;

/**
 * 主要功能:
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public class CameraController {
    public static class CameraParams{
        public ECameraType cameraType =ECameraType.CAMERA_TAKE_PHOTO;//拍摄类型（照片 视频）
        public int quality =100; //拍摄质量
//        public int
        public String path; //拍摄保存完整路径（整个路径 包括文件名 /storage/emulated/0/Pictures/default/image/IMG_20171121_154947.jpg）
        public String dirPath;//拍摄保存文件夹（文件夹名称 default）
        public String fileName;//拍摄保存的文件名（包括后缀 IMG_20171121_154947.jpg名）
        public Context context; //上下文
        public ICameraListenner iCameraListenner; //拍摄监听
        public ImageView previewImageView; //预览的imageview
        public Camera.Size picSize;//拍摄的照片分辨率（必须是相机可支持的分辨率camera.getParameters().getPictureSize()）
        public Camera.Size vidSize;//拍摄的视频拍摄分辨率（必须是相机可支持的分辨率camera.getParameters().getSupportedVideoSizes()）


        public CameraParams(Context c,ICameraListenner i){
            this.context=c;
            this.iCameraListenner=i;
            this.picSize=getAjustSizeFromScreen(CustomCameraHelper.getInstance().getCameraInstance().getParameters().getSupportedPictureSizes());
            this.vidSize=getAjustSizeFromScreen(CustomCameraHelper.getInstance().getCameraInstance().getParameters().getSupportedVideoSizes());
        }
        /**
         * 从支持分辨率库中获取最适合屏幕分辨率大小的分辨率
         * @param sizeList
         * @return
         */
        public Camera.Size getAjustSizeFromScreen(List<Camera.Size> sizeList){
            if(sizeList==null||sizeList.size()==0){
                return null;
            }
            Camera.Size temp=null;
            if(sizeList.size()>1){
                for (int i=0;i<sizeList.size()-1;i++){
                    for (int j=0;j<sizeList.size()-1-i;j++){
                        if(absScreen(sizeList.get(j))>absScreen(sizeList.get(j+1))||((absScreen(sizeList.get(j))==absScreen(sizeList.get(j+1)))&&sizeList.get(j+1).height>sizeList.get(j).height)){
                            temp=sizeList.get(j);
                            sizeList.set(j,sizeList.get(j+1));
                            sizeList.set(j+1,temp);
                        }
                    }
                }
            }

            return sizeList.get(0);
        }

        //计算width绝对值
        private int absScreen(Camera.Size size){
            int screenWidth= ScreenUtils.getScreenWidth(context);
            return Math.abs(screenWidth-size.width);
        }
    }

}
