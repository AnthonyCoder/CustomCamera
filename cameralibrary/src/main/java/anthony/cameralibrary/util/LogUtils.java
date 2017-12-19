package anthony.cameralibrary.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * 主要功能:
 * Created by wz on 2017/12/19
 * 修订历史:
 */

public class LogUtils {
    public static boolean OPEN_LOG=true;
    public static String tag="相机";
    public static void d( String log){
        if(log!=null&&OPEN_LOG){
            Log.d(tag,log);
        }
    }
    public static void e( String log){
        if(log!=null&&OPEN_LOG){
            Log.e(tag,log);
        }
    }
}
