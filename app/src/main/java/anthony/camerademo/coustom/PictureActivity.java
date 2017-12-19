package anthony.camerademo.coustom;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import anthony.camerademo.R;
import anthony.cameralibrary.CameraSurfaceView;
import anthony.cameralibrary.CustomCameraHelper;
import anthony.cameralibrary.iml.ICameraListenner;

/**
 * 主要功能:
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public class PictureActivity extends Activity implements View.OnClickListener {
    private CameraSurfaceView mPreview;
    private Context mContext;
    private ImageView iv_preview;

    FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置横屏
        setContentView(R.layout.activity_picture);
        mContext = this;
        initView();
        initCamera();
    }

    private void initView() {
        preview = (FrameLayout) findViewById(R.id.surface_view);
        iv_preview = findViewById(R.id.iv_preview);
        findViewById(R.id.iv_cancle).setOnClickListener(this);
        findViewById(R.id.iv_photograph).setOnClickListener(this);
        findViewById(R.id.iv_comfirm).setOnClickListener(this);
    }

    private void initCamera() {
        Log.e("相机", "............initCamera");
        mPreview = new CameraSurfaceView.Builder(mContext, new ICameraListenner() {
            @Override
            public void error(String msg) {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
            }
        }).setPreviewImageView(iv_preview).setOutPutDirPath("meike").setfileName("test.jpg").startCamera();
        if (mPreview.getParent() != null)
            ((ViewGroup) mPreview.getParent()).removeAllViews();
        preview.addView(mPreview);
    }

    /**
     * 锁屏时候这个方法也会被调用
     * 记住要手动设置surfaceView的可见性
     * 不然SurfaceView中surfaceholder.callback的所有方法都不会执行
     */
    @Override
    public void onPause() {
        Log.e("相机", ".........Activity...onPause");
        CustomCameraHelper.getInstance().destroyed();
        if (mPreview != null) {
            mPreview.setVisibility(View.INVISIBLE);
        }
        super.onPause();
    }


    @Override
    public void onResume() {
        Log.e("相机", ".........Activity...onResume");
        if (mPreview != null) {
            if (mPreview.getVisibility() == View.INVISIBLE) {
                initCamera();
                mPreview.setVisibility(View.VISIBLE);
            }
        }
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_cancle://返回
                finish();
                break;

            case R.id.iv_photograph://拍照
                CustomCameraHelper.getInstance().startCamera();
                break;

            case R.id.iv_comfirm://提交

                break;
        }
    }
}
