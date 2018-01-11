package anthony.cameralibrary;

import android.app.Application;
import android.util.DisplayMetrics;

/**
 * 主要功能：
 * Created by wz on 2018/1/11.
 * 修改历史：
 */

public class CameraApplication extends Application {

    public static int mScreenWidth = 0;
    public static int mScreenHeight = 0;

    public static CameraApplication CONTEXT;
    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = this;
        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();
        CameraApplication.mScreenWidth = mDisplayMetrics.widthPixels;
        CameraApplication.mScreenHeight = mDisplayMetrics.heightPixels;
    }
}
