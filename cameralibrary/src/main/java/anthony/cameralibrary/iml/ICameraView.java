package anthony.cameralibrary.iml;

import android.view.ViewGroup;

import anthony.cameralibrary.widget.CameraLayout;

/**
 * 主要功能：
 * Created by wz on 2018/1/17.
 * 修改历史：
 */

public interface ICameraView {
    ViewGroup cameraRootViewGrop();
    CameraLayout cameraLayout();
}
