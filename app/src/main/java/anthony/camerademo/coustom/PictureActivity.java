package anthony.camerademo.coustom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import anthony.camerademo.R;
import anthony.cameralibrary.CameraManager;
import anthony.cameralibrary.constant.EPreviewScaleType;
import anthony.cameralibrary.constant.EFouceMode;
import anthony.cameralibrary.iml.ICameraView;
import anthony.cameralibrary.widget.CameraLayout;
import anthony.cameralibrary.CustomCameraHelper;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;

/**
 * 主要功能:演示拍照功能
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public class PictureActivity extends Activity implements View.OnClickListener, ICameraListenner ,ICameraView{
    private CameraLayout cameraLayout;
    private Context mContext;
    private ImageView iv_preview;
    private CheckBox cbSwithDir;
    private TextView tv_camera_dir;
    private ImageButton ib_recentpic;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//设置横屏
        setContentView(R.layout.activity_picture);
        mContext = this;
        initView();
//        initCamera();
        CustomCameraHelper.getInstance().bindView(this);
    }

    private void initView() {
        frameLayout = (FrameLayout) findViewById(R.id.surface_view);
        iv_preview = findViewById(R.id.iv_preview);
        tv_camera_dir = findViewById(R.id.tv_camera_dir);
        findViewById(R.id.ib_exit).setOnClickListener(this);
        findViewById(R.id.ib_takephoto).setOnClickListener(this);
        iv_preview.setOnClickListener(this);
        cbSwithDir = findViewById(R.id.ck_switch_dir);
        ib_recentpic = findViewById(R.id.ib_recentpic);
        ib_recentpic.setOnClickListener(this);
        cbSwithDir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                CustomCameraHelper.getInstance().switchCamera();
                tv_camera_dir.setText("方向：" + (b ? "前" : "后") + "置摄像头");
            }
        });
    }


    /**
     * 锁屏时候这个方法也会被调用
     * 记住要手动设置surfaceView的可见性
     * 不然SurfaceView中surfaceholder.callback的所有方法都不会执行
     */
    @Override
    public void onPause() {
        super.onPause();
        CustomCameraHelper.getInstance().onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
        CustomCameraHelper.getInstance().onResume();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_exit://返回
                finish();
                break;

            case R.id.ib_takephoto://拍照
                CustomCameraHelper.getInstance().doPicOrVid();
                break;

            case R.id.iv_preview://预览
                if (CustomCameraHelper.getInstance().getOutputMediaFileUri() != null) {
                    Intent showIntent = new Intent(PictureActivity.this, ShowActivity.class);
                    showIntent.setDataAndType(CustomCameraHelper.getInstance().getOutputMediaFileUri(), "pic");
                    startActivity(showIntent);
                }
                break;
            case R.id.ib_recentpic://闪光灯切换
                CustomCameraHelper.getInstance().switchFlashLight();
                break;
        }
    }

    @Override
    public void error(String msg) {
        ToastUtils.showShortToast(mContext, msg);
    }

    @Override
    public void switchCameraDirection(CameraManager.CameraDirection cameraDirection) {
        boolean isfont = cameraDirection == CameraManager.CameraDirection.CAMERA_FRONT;
        tv_camera_dir.setText("方向：" + (isfont ? "前" : "后") + "置摄像头");
        cbSwithDir.setChecked(isfont);
    }

    @Override
    public void switchLightStatus(CameraManager.FlashLigthStatus flashLigthStatus) {
        switch (flashLigthStatus) {
            case LIGHT_ON:
                ib_recentpic.setBackground(getResources().getDrawable(R.drawable.selector_btn_flashlight_on));
                break;
            case LIGTH_OFF:
                ib_recentpic.setBackground(getResources().getDrawable(R.drawable.selector_btn_flashlight_off));
                break;
            case LIGHT_AUTO:
                ib_recentpic.setBackground(getResources().getDrawable(R.drawable.selector_btn_flashlight_auto));
                break;
        }
    }

    @Override
    public void takePhotoOver() {

    }

    @Override
    public void recordOver() {

    }

    @Override
    public ViewGroup cameraRootViewGrop() {
        return frameLayout;
    }

    @Override
    public CameraLayout cameraLayout() {
        cameraLayout = new CameraLayout.Builder(mContext, this)
                .setCameraType(ECameraType.CAMERA_TAKE_PHOTO)
                .setShowFouceImg(true)
                .setOpenFouceVic(false)
                .setEPreviewScaleType(EPreviewScaleType.AJUST_PREVIEW)
                .setZoomEnable(false, 100)
                .setFouceModel(EFouceMode.AUTOPOINTFOUCEMODEL)
                .setPreviewImageView(iv_preview).setOutPutDirName("images")
                .setFileName("test.jpg")
                .buildCamera();
        return cameraLayout;
    }
}
