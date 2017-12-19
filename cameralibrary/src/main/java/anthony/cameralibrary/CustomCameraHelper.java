package anthony.cameralibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import anthony.cameralibrary.constant.Constants;
import anthony.cameralibrary.constant.ECameraType;
import anthony.cameralibrary.iml.ICameraListenner;
import anthony.cameralibrary.util.LogUtils;

/**
 * 主要功能:辅助类
 * Created by wz on 2017/11/20
 * 修订历史:
 */
public class CustomCameraHelper {
    public static CustomCameraHelper instance = new CustomCameraHelper();
    private CameraSurfaceView cameraSurfaceView;
    private ICameraListenner iCameraListenner;
    private Context context;
    private Camera mCamera;
    private String outputMediaFileType;
    private Uri outputMediaFileUri;
    private MediaRecorder mMediaRecorder;//视频录制对象
    private CameraController.CameraParams coustomParams;
    private Camera.Parameters parameters;

    //绑定SurfaceView
    public void bind(CameraSurfaceView cameraSurfaceView) {
        LogUtils.d("............bind");
        cameraSurfaceView.getHolder().addCallback(cameraSurfaceView);
        this.cameraSurfaceView = cameraSurfaceView;
        this.coustomParams = cameraSurfaceView.getCameraParams();
        this.iCameraListenner = coustomParams.iCameraListenner;
        this.context = coustomParams.context;
    }

    public void create() {
        LogUtils.d("............create");
        getCameraInstance();
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewDisplay(cameraSurfaceView.getHolder());
            mCamera.startPreview();
        } catch (IOException e) {
            iCameraListenner.error("Error setting camera preview: " + e.getMessage());
        }
    }

    public Camera getCameraInstance() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                parameters = mCamera.getParameters();
                List<String> focusModes = parameters.getSupportedFocusModes();
                for (String mode : focusModes) {
                    if (mode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        break;
                    }
                }
            } catch (Exception e) {
                iCameraListenner.error("请检查应用是否开启相机权限或是否被其他应用占用相机");
            }
        }
        return mCamera;
    }

    public void change() {
        LogUtils.d("............change");
        if (mCamera == null) {
            return;
        }
        int rotation = getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);
        adjustDisplayRatio(rotation);

    }

    public void destroyed() {
        LogUtils.d("............destroyed");
        if (mCamera == null) {
            return;
        }
        cameraSurfaceView.getHolder().removeCallback(cameraSurfaceView);
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            try {
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mCamera.release();
            mCamera = null;
        }

    }


    /**
     * 用于根据手机方向获得相机预览画面旋转的角度
     * 校正拍照的角度
     *
     * @return 返回适应的角度
     */
    public int getDisplayOrientation() {
        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    //自适应预览图片尺寸（防止预览画面变形）
    private void adjustDisplayRatio(int rotation) {
        ViewGroup parent = ((ViewGroup) cameraSurfaceView.getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = parameters.getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildWidth = previewWidth * height / previewHeight;
            cameraSurfaceView.layout((width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            cameraSurfaceView.layout(0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2);
        }
    }

    //开始拍照
    public void takePicture() {
        mCamera.stopPreview();
        if (getSize() == null) {
            return;
        }
        LogUtils.d("......................w:" + getSize()[0] + "。。。。。。。h:" + getSize()[1]);
        parameters.setPictureSize(getSize()[0], getSize()[1]);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(ECameraType.CAMERA_TAKE_PHOTO);
                if (pictureFile == null) {
                    iCameraListenner.error("Error creating media file, check storage permissions");
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    if (coustomParams.previewImageView != null) {
                        coustomParams.previewImageView.setImageURI(outputMediaFileUri);
                    }
                    camera.startPreview();
                } catch (FileNotFoundException e) {
                    iCameraListenner.error("File not found: " + e.getMessage());
                } catch (IOException e) {
                    iCameraListenner.error("Error accessing file: " + e.getMessage());
                }
            }
        });
    }

    //获取分辨率
    private int[] getSize() {
        int[] sizeList = new int[2];
        if (coustomParams.loadSettingParams) {//本地加载
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(coustomParams.context);
            if (coustomParams.cameraType == ECameraType.CAMERA_TAKE_PHOTO) {//拍照
                String prefPicSize = prefs.getString(Constants.KEY_PREF_PIC_SIZE, "");
                if (prefPicSize == null || prefPicSize.trim().isEmpty()) {//本地没有 就使用默认或者用户定义的分辨率
                    Camera.Size picize = coustomParams.picSize;
                    if (picize == null) {
                        iCameraListenner.error("相机没有可支持的拍照分辨率参数");
                        return null;
                    }
                    prefPicSize = picize.width + "x" + picize.height;
                }
                String[] split = prefPicSize.split("x");
                sizeList[0] = Integer.parseInt(split[0].trim());
                sizeList[1] = Integer.parseInt(split[1].trim());
                return sizeList;
            } else if (coustomParams.cameraType == ECameraType.CAMERA_VIDEO) {//拍视频
                String prefVideoSize = prefs.getString(Constants.KEY_PREF_VIDEO_SIZE, "");
                if (prefVideoSize == null || prefVideoSize.trim().isEmpty()) {
                    Camera.Size videoSize = coustomParams.vidSize;
                    if (videoSize == null) {
                        iCameraListenner.error("相机没有可支持的视频分辨率参数");
                        return null;
                    }
                    prefVideoSize = videoSize.width + "x" + videoSize.height;
                }
                String[] split = prefVideoSize.split("x");
                sizeList[0] = Integer.parseInt(split[0].trim());
                sizeList[1] = Integer.parseInt(split[1].trim());
                return sizeList;
            }
            return null;

        } else {//加载用户配置参数
            if (coustomParams.cameraType == ECameraType.CAMERA_TAKE_PHOTO) {//拍照
                sizeList[0] = coustomParams.picSize.width;
                sizeList[1] = coustomParams.picSize.height;
                return sizeList;
            } else if (coustomParams.cameraType == ECameraType.CAMERA_VIDEO) {//视频
                sizeList[0] = coustomParams.vidSize.width;
                sizeList[1] = coustomParams.vidSize.height;
                return sizeList;
            }
            return null;
        }
    }

    /**
     * 开始录制
     *
     * @return
     */
    private boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    //停止录制
    public void stopRecording() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
            } catch (RuntimeException stopException) {
                //handle cleanup here
            }

            if (coustomParams.previewImageView != null) {//预览视频第一帧的图片
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(outputMediaFileUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                coustomParams.previewImageView.setImageBitmap(thumbnail);
            }

        }
        releaseMediaRecorder();
    }

    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    //准备视频录制
    private boolean prepareVideoRecorder() {
        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        } catch (Exception e) {
            iCameraListenner.error("请检查视频录制权限是否打开");
        }
        if (getSize() == null) {
            return false;
        }
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        Log.e("相机", "......................w:" + getSize()[0] + "。。。。。。。h:" + getSize()[1]);
        mMediaRecorder.setVideoSize(getSize()[0], getSize()[1]);

        mMediaRecorder.setOutputFile(getOutputMediaFile(ECameraType.CAMERA_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(cameraSurfaceView.getHolder().getSurface());

        int rotation = getDisplayOrientation();
        mMediaRecorder.setOrientationHint(rotation);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            iCameraListenner.error("IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            iCameraListenner.error("IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    //释放录制对象实例
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    /**
     * 打开相机开始拍照或者录制
     *
     * @return 是否处于录制中的状态
     */
    public boolean startCamera() {
        if (coustomParams.cameraType != null) {
            if (coustomParams.cameraType == ECameraType.CAMERA_TAKE_PHOTO) {//拍照
                takePicture();
                return true;
            } else if (coustomParams.cameraType == ECameraType.CAMERA_VIDEO) {//录制视频
                return startRecording();
            }
        }
        return false;
    }

    /**
     * 获取输出文件
     *
     * @param type 照片 或者视频
     * @return 输出文件
     */
    private File getOutputMediaFile(ECameraType type) {
        String dirPath;
        if (coustomParams.dirName == null) {
            dirPath = "default";
        } else {
            dirPath = coustomParams.dirName;
        }
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), dirPath + File.separator + (type == ECameraType.CAMERA_TAKE_PHOTO ? "image" : "video"));
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                iCameraListenner.error("failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == ECameraType.CAMERA_TAKE_PHOTO) {
            if (coustomParams.path != null) {
                mediaFile = new File(coustomParams.path);
            } else {
                String fileName = coustomParams.fileName == null ? "IMG_" + timeStamp + ".jpg" : coustomParams.fileName;
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + File.separator +
                        fileName);
            }

            outputMediaFileType = "image/*";
        } else if (type == ECameraType.CAMERA_VIDEO) {
            String fileName = coustomParams.fileName == null ? "VID_" + timeStamp + ".mp4" : coustomParams.fileName;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + File.separator +
                    fileName);
            outputMediaFileType = "video/*";
        } else {
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);
        LogUtils.d(".......路径：" + mediaFile.getAbsolutePath());
        return mediaFile;
    }

    /**
     * 获取CoustomCameraHelper实例
     *
     * @return 辅助类实例
     */
    public static CustomCameraHelper getInstance() {
        return instance;
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }
}
