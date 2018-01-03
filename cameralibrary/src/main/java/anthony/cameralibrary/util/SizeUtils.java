package anthony.cameralibrary.util;

import android.content.Context;
import android.hardware.Camera;

import java.util.List;

/**
 * 主要功能：
 * Created by wz on 2017/12/21.
 * 修改历史：
 */

public class SizeUtils {
    /**
     * 从支持分辨率库中获取最适合屏幕分辨率大小的分辨率
     * @param sizeList 传来的分辨率组集合
     * @return 返回的最适合屏幕的分辨率组
     */
    public static Camera.Size getAjustSizeFromScreen(List<Camera.Size> sizeList,Context c){
        if(sizeList==null||sizeList.size()==0){
            return null;
        }
        Camera.Size temp=null;
        if(sizeList.size()>1){
            for (int i=0;i<sizeList.size()-1;i++){
                for (int j=0;j<sizeList.size()-1-i;j++){
                    if(absScreen(sizeList.get(j),c)>absScreen(sizeList.get(j+1),c)||((absScreen(sizeList.get(j),c)==absScreen(sizeList.get(j+1),c))&&sizeList.get(j+1).height>sizeList.get(j).height)){
                        temp=sizeList.get(j);
                        sizeList.set(j,sizeList.get(j+1));
                        sizeList.set(j+1,temp);
                    }
                }
            }
        }

        return sizeList.get(0);
    }
    //分辨率是否支持适配列表
    public static boolean isSurpportDrivse(List<Camera.Size> surpportList,int w,int h){
        if(surpportList==null||surpportList.size()<1){
            return false;
        }
        for (int i=0; i<surpportList.size();i++){
            if(surpportList.get(i).width==w&&surpportList.get(i).height==h){
                return true;
            }
        }
        return false;
    }
    //计算width绝对值
    private static int absScreen(Camera.Size size,Context context){
        int screenWidth= ScreenUtils.getScreenWidth(context);
        return Math.abs(screenWidth-size.width);
    }

}
