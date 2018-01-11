package anthony.cameralibrary.widget;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import anthony.cameralibrary.CameraController;
import anthony.cameralibrary.CustomCameraHelper;
import anthony.cameralibrary.R;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;

/**
 * 主要功能:
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public class CameraSurfaceView extends SurfaceView {

    private SoundPool mSoundPool;
    private int mFocusSoundId;
    private boolean mFocusSoundPrepared;


    public CameraSurfaceView(Context context) {
        super(context);
        initData();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    private void initData(){
        initSoundPool();
    }
    public SoundPool initSoundPool(){
        if(mSoundPool == null) {
            mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            mFocusSoundId = mSoundPool.load(getContext(), R.raw.camera_focus,1);
            mFocusSoundPrepared = false;
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    mFocusSoundPrepared = true;
                }
            });
        }
        return mSoundPool;
    }
    public void playSound(){
        playSound(1.0f, 0.5f, 1, 0, 1.0f);
    }
    public void playSound( float leftVolume, float rightVolume, int priority, int loop, float rate){
        if(mFocusSoundPrepared){
            mSoundPool.play(mFocusSoundId, leftVolume, rightVolume, priority, loop, rate);
        }
    }
    public void releaseSoundPool(){
        if(mSoundPool!=null){
            mSoundPool.release();
            mSoundPool = null;
        }
    }
}
