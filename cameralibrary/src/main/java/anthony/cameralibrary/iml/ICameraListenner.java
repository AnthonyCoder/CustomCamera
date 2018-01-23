package anthony.cameralibrary.iml;

import anthony.cameralibrary.CameraManager;

/**
 * 主要功能:
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public interface ICameraListenner {
    void error(String msg);
    void switchCameraDirection(CameraManager.CameraDirection cameraDirection);
    void switchLightStatus(CameraManager.FlashLigthStatus flashLigthStatus);
    void takePhotoOver();
    void recordOver();
}
