package anthony.cameralibrary.setting;

import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import java.util.ArrayList;
import java.util.List;

import anthony.cameralibrary.R;
import anthony.cameralibrary.constant.SPConstants;
import anthony.cameralibrary.util.SPConfigUtil;


/**
 * 主要功能:
 * Created by wz on 2017/11/21
 * 修订历史:
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static Camera mCamera;
    private static Camera.Parameters mParameters;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getActivity().setTheme(R.style.PreferenceTheme);
        loadSupportedPreviewSize();
        loadSupportedPictureSize();
        loadSupportedVideoeSize();
        loadSupportedFlashMode();
        loadSupportedFocusMode();
        loadSupportedWhiteBalance();
        loadSupportedSceneMode();
        loadSupportedExposeCompensation();
        initSummary(getPreferenceScreen());
    }

    public static void passCamera(Camera camera) {
        mCamera = camera;
        mParameters = camera==null?null:camera.getParameters();
    }

    public static void setDefault(SharedPreferences sharedPrefs) {
        String valPreviewSize = SPConfigUtil.load(SPConstants.KEY_PREF_PREV_SIZE, null);
        if (valPreviewSize == null&&mParameters!=null) {
//            SharedPreferences.Editor editor = sharedPrefs.edit();
            SPConfigUtil.save(SPConstants.KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
            SPConfigUtil.save(SPConstants.KEY_PREF_PIC_SIZE, getDefaultPictureSize());
            SPConfigUtil.save(SPConstants.KEY_PREF_VIDEO_SIZE, getDefaultVideoSize());
            SPConfigUtil.save(SPConstants.KEY_PREF_FOCUS_MODE, getDefaultFocusMode());

//            editor.putString(SPConstants.KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
//            editor.putString(SPConstants.KEY_PREF_PIC_SIZE, getDefaultPictureSize());
//            editor.putString(SPConstants.KEY_PREF_VIDEO_SIZE, getDefaultVideoSize());
//            editor.putString(SPConstants.KEY_PREF_FOCUS_MODE, getDefaultFocusMode());
//            editor.apply();
        }
    }

    private static String getDefaultPreviewSize() {
        Camera.Size previewSize = mParameters.getPreviewSize();
        return previewSize.width + "x" + previewSize.height;
    }

    private static String getDefaultPictureSize() {
        Camera.Size pictureSize = mParameters.getPictureSize();
        return pictureSize.width + "x" + pictureSize.height;
    }

    private static String getDefaultVideoSize() {
        Camera.Size VideoSize = mParameters.getPreferredPreviewSizeForVideo();
        return VideoSize.width + "x" + VideoSize.height;
    }

    private static String getDefaultFocusMode() {
        List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
        if (supportedFocusModes.contains("continuous-picture")) {
            return "continuous-picture";
        }
        return "continuous-video";
    }

    public static void init(SharedPreferences sharedPref) {
        if(mParameters==null){
            return;
        }
        setPreviewSize(SPConfigUtil.load(SPConstants.KEY_PREF_PREV_SIZE));
        setPictureSize(SPConfigUtil.load(SPConstants.KEY_PREF_PIC_SIZE));
        setFlashMode(SPConfigUtil.load(SPConstants.KEY_PREF_FLASH_MODE));
        setFocusMode(SPConfigUtil.load(SPConstants.KEY_PREF_FOCUS_MODE));
        setWhiteBalance(SPConfigUtil.load(SPConstants.KEY_PREF_WHITE_BALANCE));
        setSceneMode(SPConfigUtil.load(SPConstants.KEY_PREF_SCENE_MODE));
        setExposComp(SPConfigUtil.load(SPConstants.KEY_PREF_EXPOS_COMP));
        setJpegQuality(SPConfigUtil.load(SPConstants.KEY_PREF_JPEG_QUALITY));
        setGpsData(SPConfigUtil.loadBoolean(SPConstants.KEY_PREF_GPS_DATA, false));
        mCamera.stopPreview();
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    private void loadSupportedPreviewSize() {
        cameraSizeListToListPreference(mParameters.getSupportedPreviewSizes(), SPConstants.KEY_PREF_PREV_SIZE);
    }

    private void loadSupportedPictureSize() {
        cameraSizeListToListPreference(mParameters.getSupportedPictureSizes(), SPConstants.KEY_PREF_PIC_SIZE);
    }

    private void loadSupportedVideoeSize() {
        cameraSizeListToListPreference(mParameters.getSupportedVideoSizes(), SPConstants.KEY_PREF_VIDEO_SIZE);
    }

    private void loadSupportedFlashMode() {
        stringListToListPreference(mParameters.getSupportedFlashModes(), SPConstants.KEY_PREF_FLASH_MODE);
    }

    private void loadSupportedFocusMode() {
        stringListToListPreference(mParameters.getSupportedFocusModes(), SPConstants.KEY_PREF_FOCUS_MODE);
    }

    private void loadSupportedWhiteBalance() {
        stringListToListPreference(mParameters.getSupportedWhiteBalance(), SPConstants.KEY_PREF_WHITE_BALANCE);
    }

    private void loadSupportedSceneMode() {
        stringListToListPreference(mParameters.getSupportedSceneModes(), SPConstants.KEY_PREF_SCENE_MODE);
    }

    private void loadSupportedExposeCompensation() {
        int minExposComp = mParameters.getMinExposureCompensation();
        int maxExposComp = mParameters.getMaxExposureCompensation();
        List<String> exposComp = new ArrayList<>();
        for (int value = minExposComp; value <= maxExposComp; value++) {
            exposComp.add(Integer.toString(value));
        }
        stringListToListPreference(exposComp, SPConstants.KEY_PREF_EXPOS_COMP);
    }

    private void cameraSizeListToListPreference(List<Camera.Size> list, String key) {
        List<String> stringList = new ArrayList<>();
        for (Camera.Size size : list) {
            String stringSize = size.width + "x" + size.height;
            stringList.add(stringSize);
        }
        stringListToListPreference(stringList, key);
    }

    private void stringListToListPreference(List<String> list, String key) {
        final CharSequence[] charSeq = list.toArray(new CharSequence[list.size()]);
        ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key);
        listPref.setEntries(charSeq);
        listPref.setEntryValues(charSeq);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
        switch (key) {
            case SPConstants.KEY_PREF_PREV_SIZE:
                setPreviewSize(SPConfigUtil.load(key, ""));
                break;
            case SPConstants.KEY_PREF_PIC_SIZE:
                setPictureSize(SPConfigUtil.load(key, ""));
                break;
            case SPConstants.KEY_PREF_FOCUS_MODE:
                setFocusMode(SPConfigUtil.load(key, ""));
                break;
            case SPConstants.KEY_PREF_FLASH_MODE:
                setFlashMode(SPConfigUtil.load(key, ""));
                break;
            case SPConstants.KEY_PREF_WHITE_BALANCE:
                setWhiteBalance(SPConfigUtil.load(key, ""));
                break;
            case SPConstants.KEY_PREF_SCENE_MODE:
                setSceneMode(SPConfigUtil.load(key, ""));
                break;
            case SPConstants.KEY_PREF_EXPOS_COMP:
                setExposComp(SPConfigUtil.load(key, ""));
                break;
            case SPConstants.KEY_PREF_JPEG_QUALITY:
                setJpegQuality(SPConfigUtil.load(key, ""));
                break;
            case SPConstants.KEY_PREF_GPS_DATA:
                setGpsData(SPConfigUtil.loadBoolean(key, false));
                break;
        }
        mCamera.stopPreview();
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    private static void setPreviewSize(String value) {
        String[] split = value.split("x");
        mParameters.setPreviewSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static void setPictureSize(String value) {
        String[] split = value.split("x");
        mParameters.setPictureSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static void setFocusMode(String value) {
        mParameters.setFocusMode(value);
    }

    private static void setFlashMode(String value) {
        mParameters.setFlashMode(value);
    }

    private static void setWhiteBalance(String value) {
        mParameters.setWhiteBalance(value);
    }

    private static void setSceneMode(String value) {
        mParameters.setSceneMode(value);
    }

    private static void setExposComp(String value) {
        mParameters.setExposureCompensation(Integer.parseInt(value));
    }

    private static void setJpegQuality(String value) {
        mParameters.setJpegQuality(Integer.parseInt(value));
    }

    private static void setGpsData(Boolean value) {
        if (value.equals(false)) {
            mParameters.removeGpsData();
        }
    }

    private static void initSummary(Preference pref) {
        if (pref instanceof PreferenceGroup) {
            PreferenceGroup prefGroup = (PreferenceGroup) pref;
            for (int i = 0; i < prefGroup.getPreferenceCount(); i++) {
                initSummary(prefGroup.getPreference(i));
            }
        } else {
            updatePrefSummary(pref);
        }
    }

    private static void updatePrefSummary(Preference pref) {
        if (pref instanceof ListPreference) {
            pref.setSummary(((ListPreference) pref).getEntry());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
