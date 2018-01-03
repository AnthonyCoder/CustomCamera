package anthony.camerademo.coustom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import anthony.camerademo.R;
import anthony.cameralibrary.CameraSurfaceView;
import anthony.cameralibrary.CustomCameraHelper;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.setting.SettingsFragment;
import anthony.cameralibrary.util.LogUtils;

/**
 * 主要功能:演示视频拍摄功能
 * Created by wz on 2017/11/21
 * 修订历史:
 */
public class VideoActivity extends Activity implements View.OnClickListener,ICameraListenner{
    private CameraSurfaceView mPreview;
    private Context mContext;
    private ImageView iv_preview;
    private FrameLayout preview;
    private Button start_record;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mContext = this;
        initView();
        initCamera();
    }

    private void initView() {
        preview = (FrameLayout) findViewById(R.id.surface_view);
        iv_preview = findViewById(R.id.iv_preview);
        start_record = findViewById(R.id.start_record);
        findViewById(R.id.iv_cancle).setOnClickListener(this);
        findViewById(R.id.start_record).setOnClickListener(this);
        findViewById(R.id.bt_setting).setOnClickListener(this);
        iv_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CustomCameraHelper.getInstance().getOutputMediaFileUri()!=null){
                    Intent showIntent=new Intent(VideoActivity.this,ShowActivity.class);
                    showIntent.setDataAndType(CustomCameraHelper.getInstance().getOutputMediaFileUri(),"vid");
                    startActivity(showIntent);
                }
            }
        });
    }

    private void initCamera() {
        mPreview = new CameraSurfaceView.Builder(mContext, this)
                .setCameraType(ECameraType.CAMERA_VIDEO)
                .setLoadSettingParams(true)
                .setPreviewImageView(iv_preview)
                .setOutPutDirName("video")
                .startCamera();
        if (mPreview.getParent() != null)
            ((ViewGroup) mPreview.getParent()).removeAllViews();
        preview.addView(mPreview);
        settingsFragment=new SettingsFragment();
        //初始化相机参数（包括相机的拍摄分辨率、闪光灯模式等...）
        SettingsFragment.passCamera(CustomCameraHelper.getInstance().getCameraInstance());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
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
                    if (CustomCameraHelper.getInstance().startCamera()) {
                        start_record.setText("停止");
                    }
                }
                break;
            case R.id.bt_setting:
                if(!settingsFragment.isVisible()){
                    getFragmentManager().beginTransaction().replace(R.id.surface_view, settingsFragment).addToBackStack(null).commit();
                }else{
                    getFragmentManager().popBackStack();
                }
                break;
            case R.id.iv_cancle://返回
                finish();
                break;
        }
    }

    @Override
    public void error(String msg) {
        LogUtils.d(msg);
        ToastUtils.showShortToast(mContext,msg);
    }
}
