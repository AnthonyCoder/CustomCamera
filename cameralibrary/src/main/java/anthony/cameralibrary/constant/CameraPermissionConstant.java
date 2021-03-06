package anthony.cameralibrary.constant;

import android.Manifest;

/**
 * 主要功能:权限常量池
 * Created by wz on 2017/7/25
 * 修订历史:
 */
public class CameraPermissionConstant {
    public static final String CAMERA= Manifest.permission.CAMERA;//拍照
    public static final String RECORD_AUDIO= Manifest.permission.RECORD_AUDIO;//录制
    public static final String WRITE_EXTERNAL_STORAGE= Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String ACCESS_FINE_LOCATION= Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION= Manifest.permission.ACCESS_COARSE_LOCATION;
}
