package anthony.cameralibrary;

import android.content.Context;
import android.widget.ImageView;

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


        public CameraParams(Context c,ICameraListenner i){
            this.context=c;
            this.iCameraListenner=i;
        }
    }

}
