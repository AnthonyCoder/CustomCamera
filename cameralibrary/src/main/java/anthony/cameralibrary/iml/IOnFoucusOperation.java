package anthony.cameralibrary.iml;

import android.graphics.Point;
import android.hardware.Camera;

/**
 * 主要功能：
 * Created by wz on 2018/1/11.
 * 修改历史：
 */

public interface IOnFoucusOperation {
    boolean onPointFocus(Point point, Camera.AutoFocusCallback callback);
}
