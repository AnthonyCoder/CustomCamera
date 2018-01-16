package anthony.cameralibrary;

import android.content.Context;
import android.hardware.Camera;
import android.widget.ImageView;

import anthony.cameralibrary.constant.ECameraScaleType;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.constant.EFouceMode;
import anthony.cameralibrary.constant.ESaveDirectionType;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.util.SizeUtils;

/**
 * 主要功能:
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public class CameraController {
    public static class CameraParams {
        public ECameraType cameraType = ECameraType.CAMERA_TAKE_PHOTO;//拍摄类型（照片 视频）
        public int quality = 100; //拍摄质量
        public String path; //拍摄保存完整路径（整个路径 包括文件名 /storage/emulated/0/Pictures/default/image/IMG_20171121_154947.jpg）
        public String dirName;//拍摄保存文件夹（文件夹名称 default）
        public String fileName;//拍摄保存的文件名（包括后缀 IMG_20171121_154947.jpg名）
        public Context context; //上下文
        public ICameraListenner iCameraListenner; //拍摄监听
        public ImageView previewImageView; //预览的imageview
        public Camera.Size picSize;//拍摄的照片分辨率（必须是相机可支持的分辨率camera.getParameters().getPictureSize()）
        public Camera.Size vidSize;//拍摄的视频拍摄分辨率（必须是相机可支持的分辨率camera.getParameters().getSupportedVideoSizes()）
//        public boolean loadSettingParams = false;//是否加载本地已设置的数据（通过SettingFragment储存的参数）  默认不加载
        public boolean enableZoom = false;//默认禁用缩放功能
        public int maxZoom = -1;//最大缩放值（值为-1时候表示，使用系统最大缩放）
        public boolean showFouceImg = false;//是否显示聚焦时候的图片（暂时只支持自带）
        public boolean openFouceVic = false;//是否打开聚焦时候的音效（暂时只支持自带）
        public EFouceMode eFouceMode = EFouceMode.AUTOPOINTFOUCEMODEL;//默认是自动+指定点对焦模式
        public ECameraScaleType eCameraScaleType = ECameraScaleType.CENTER_AUTO;//默认自动测量防止图形变形模式
        public ESaveDirectionType eSaveDirectionType = ESaveDirectionType.NORMAL;//默认正常模式
        public CameraParams(Context c, ICameraListenner i) {
            this.context = c;
            this.iCameraListenner = i;
//            this.picSize = SizeUtils.getAjustSizeFromScreen(CustomCameraHelper.getInstance().getCameraInstance().getParameters().getSupportedPictureSizes(), c);
//            this.vidSize = SizeUtils.getAjustSizeFromScreen(CustomCameraHelper.getInstance().getCameraInstance().getParameters().getSupportedVideoSizes(), c);
        }

    }

}
