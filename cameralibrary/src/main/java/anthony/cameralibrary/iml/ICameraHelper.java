package anthony.cameralibrary.iml;

import android.hardware.Camera;

/**
 * CameraHelper的统一接口
 * @author jerry
 * @date 2015-09-01
 */
public interface ICameraHelper {

    int getNumberOfCameras();

    Camera openCameraFacing(int facing) throws Exception;

    boolean hasCamera(int facing);

    void getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo);
}
