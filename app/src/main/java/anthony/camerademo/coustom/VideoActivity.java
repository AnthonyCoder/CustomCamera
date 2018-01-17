package anthony.camerademo.coustom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import anthony.camerademo.R;
import anthony.cameralibrary.CameraManager;
import anthony.cameralibrary.iml.ICameraView;
import anthony.cameralibrary.widget.CameraLayout;
import anthony.cameralibrary.CustomCameraHelper;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.util.LogUtils;

/**
 * 主要功能:演示视频拍摄功能
 * Created by wz on 2017/11/21
 * 修订历史:
 */
public class VideoActivity extends Activity implements View.OnClickListener,ICameraListenner,ICameraView{
    private CameraLayout cameraLayout;
    private Context mContext;
    private ImageView iv_preview;
    private FrameLayout preview;
    private Button start_record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mContext = this;
        initView();
        CustomCameraHelper.getInstance().bindView(this);
    }

    private void initView() {
        preview = (FrameLayout) findViewById(R.id.surface_view);
        iv_preview = findViewById(R.id.iv_preview);
        start_record = findViewById(R.id.start_record);
        findViewById(R.id.iv_cancle).setOnClickListener(this);
        findViewById(R.id.start_record).setOnClickListener(this);
        findViewById(R.id.bt_setting).setOnClickListener(this);
        iv_preview.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        CustomCameraHelper.getInstance().onPause();
        super.onPause();
    }


    @Override
    public void onResume() {
        CustomCameraHelper.getInstance().onResume();
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_record://开始录制
                if ( CustomCameraHelper.getInstance().isRecording()) {
                    CustomCameraHelper.getInstance().stopRecording();
                    start_record.setText("录制");
                } else {
                    if (CustomCameraHelper.getInstance().doPicOrVid()) {
                        start_record.setText("停止");
                    }
                }
                break;
            case R.id.bt_setting:
                break;
            case R.id.iv_cancle://返回
                finish();
                break;
            case R.id.iv_preview:
                if(CustomCameraHelper.getInstance().getOutputMediaFileUri()!=null){
                    Intent showIntent=new Intent(VideoActivity.this,ShowActivity.class);
                    showIntent.setDataAndType(CustomCameraHelper.getInstance().getOutputMediaFileUri(),"vid");
                    startActivity(showIntent);
                }
                break;
        }
    }

    @Override
    public void error(String msg) {
        LogUtils.d(msg);
        ToastUtils.showShortToast(mContext,msg);
    }

    @Override
    public void switchCameraDirection(CameraManager.CameraDirection cameraDirection) {

    }

    @Override
    public void switchLightStatus(CameraManager.FlashLigthStatus flashLigthStatus) {

    }

    @Override
    public ViewGroup cameraRootViewGrop() {
        return preview;
    }

    @Override
    public CameraLayout cameraLayout() {

        cameraLayout = new CameraLayout.Builder(mContext, this)
                .setCameraType(ECameraType.CAMERA_VIDEO)
                .setShowFouceImg(true)
                .setOpenFouceVic(true)
                .setZoomEnable(false,100)
                .setPreviewImageView(iv_preview)
                .setOutPutDirName("video")
                .buildCamera();
        return cameraLayout;
    }
}
