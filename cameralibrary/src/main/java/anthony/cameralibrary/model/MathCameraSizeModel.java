package anthony.cameralibrary.model;

import android.hardware.Camera;

/**
 * 主要功能：
 * Created by wz on 2018/1/23.
 * 修改历史：
 */

public class MathCameraSizeModel {
    private double ratioDiffer;//长宽比差
    private Camera.Size cameraSize;

    public MathCameraSizeModel(double ratioDiffer, Camera.Size cameraSize){
        this.ratioDiffer = ratioDiffer;
        this.cameraSize =cameraSize;
    }
    public double getRatioDiffer() {
        return ratioDiffer;
    }

    public void setRatioDiffer(double ratioDiffer) {
        this.ratioDiffer = ratioDiffer;
    }

    public Camera.Size getCameraSize() {
        return cameraSize;
    }

    public void setCameraSize(Camera.Size cameraSize) {
        this.cameraSize = cameraSize;
    }
}
