package anthony.cameralibrary;

import android.annotation.TargetApi;
import android.hardware.Camera;

import anthony.cameralibrary.iml.ICameraHelper;

/**
 * API 9 以及以上使用
 *
 * @author jerry
 * @date 2015-09-01
 */
@TargetApi(9)
public class CameraHelperGBImpl implements ICameraHelper {
    @Override
    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    @Override
    public Camera openCameraFacing(int facing) {
        return Camera.open(getCameraId(facing));
    }

    @Override
    public boolean hasCamera(int facing) {
        return getCameraId(facing) != -1;
    }

    @Override
    public void getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo) {
        Camera.getCameraInfo(cameraId, cameraInfo);
    }

    /**
     * 获取cameraId
     */
    private int getCameraId(final int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int id = 0; id < numberOfCameras; id++) {
            Camera.getCameraInfo(id, info);
            if (info.facing == facing) {
                return id;
            }
        }
        return -1;
    }
}
