package anthony.cameralibrary.util;

import android.content.Context;
import android.hardware.Camera;
import android.view.MotionEvent;

import java.util.LinkedList;
import java.util.List;

import anthony.cameralibrary.model.MathCameraSizeModel;

/**
 * 主要功能：
 * Created by wz on 2017/12/21.
 * 修改历史：
 */

public class SizeUtils {
    /**
     * dip转为PX
     */
    public static int dp2px(Context context, float dipValue) {
        float fontScale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * fontScale + 0.5f);
    }

    /**
     * 从支持分辨率库中获取最适合屏幕分辨率大小的分辨率
     *
     * @param sizeList 传来的分辨率组集合
     * @return 返回的最适合屏幕的分辨率组
     */
    public static Camera.Size getAjustSizeFromScreen(List<Camera.Size> sizeList, Context c) {
        if (sizeList == null || sizeList.size() == 0) {
            return null;
        }
        Camera.Size temp = null;
        if (sizeList.size() > 1) {
            for (int i = 0; i < sizeList.size() - 1; i++) {
                for (int j = 0; j < sizeList.size() - 1 - i; j++) {
                    if (absScreen(sizeList.get(j), c) > absScreen(sizeList.get(j + 1), c) || ((absScreen(sizeList.get(j), c) == absScreen(sizeList.get(j + 1), c)) && sizeList.get(j + 1).height > sizeList.get(j).height)) {
                        temp = sizeList.get(j);
                        sizeList.set(j, sizeList.get(j + 1));
                        sizeList.set(j + 1, temp);
                    }
                }
            }
        }

        return sizeList.get(0);
    }
    /**
     * 获取最佳的预览View的分辨率
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        double targetRatio = (double) w / h;//目标比例
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        LinkedList<MathCameraSizeModel> mathCameraSizeModelList = new LinkedList<>();
        for (Camera.Size size : sizes) {
            LogUtils.d("当前w:" + size.width + "  h:" + size.height);
//
//            if(w>h && size.width < size.height) continue;
//            if(w<h && size.width > size.height) continue;
            double ratio = (double) size.width / size.height;//某一像素组的比例
            mathCameraSizeModelList.add(new MathCameraSizeModel(Math.abs(ratio - targetRatio), size));
        }
        MathCameraSizeModel temp;
        for (int i = 0; i < mathCameraSizeModelList.size() - 1; i++) {
            for (int j = 0; j < mathCameraSizeModelList.size() - 1 - i; j++) {
                if (mathCameraSizeModelList.get(j).getRatioDiffer() > mathCameraSizeModelList.get(j + 1).getRatioDiffer()) {
                    temp = mathCameraSizeModelList.get(j);
                    mathCameraSizeModelList.set(j, mathCameraSizeModelList.get(j + 1));
                    mathCameraSizeModelList.set(j + 1, temp);
                }
            }
        }
        for (MathCameraSizeModel mathCameraSizeModel : mathCameraSizeModelList) {
            LogUtils.d("比例差" + mathCameraSizeModel.getRatioDiffer() + "      当前w:" + mathCameraSizeModel.getCameraSize().width + "  h:" + mathCameraSizeModel.getCameraSize().height);

        }
        for (MathCameraSizeModel mathCameraSizeModel : mathCameraSizeModelList) {
            if(mathCameraSizeModel.getCameraSize().height>h){
                LogUtils.d("所选比例差" + mathCameraSizeModel.getRatioDiffer() + "  layout w:" + w + "  h:" + h + "      当前w:" + mathCameraSizeModel.getCameraSize().width + "  h:" + mathCameraSizeModel.getCameraSize().height);
                optimalSize = mathCameraSizeModel.getCameraSize();
                return optimalSize;
            }
        }

        if(mathCameraSizeModelList==null||mathCameraSizeModelList.size()==0) return null;
        return mathCameraSizeModelList.get(0).getCameraSize();
    }


    //分辨率是否支持适配列表
    public static boolean isSurpportDrivse(List<Camera.Size> surpportList, Camera.Size size) {
        if (size == null) {
            return false;
        }
        return isSurpportDrivse(surpportList, size.width, size.height);
    }

    public static boolean isSurpportDrivse(List<Camera.Size> surpportList, int w, int h) {
        if (surpportList == null || surpportList.size() < 1) {
            return false;
        }
        for (int i = 0; i < surpportList.size(); i++) {
            if (surpportList.get(i).width == w && surpportList.get(i).height == h) {
                return true;
            }
        }
        return false;
    }

    //计算width绝对值
    private static int absScreen(Camera.Size size, Context context) {
        int screenWidth = ScreenUtils.getScreenWidth(context);
        return Math.abs(screenWidth - size.width);
    }

    //两手指间距
    public static float fingerSpacing(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

}
