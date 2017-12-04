package anthony.camerademo.coustom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import anthony.camerademo.R;
import anthony.cameralibrary.PermissionConstant;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * 主要功能:
 * Created by wz on 2017/11/21
 * 修订历史:
 */
@RuntimePermissions
public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.go_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityPermissionsDispatcher.openCameraToPicWithPermissionCheck(MainActivity.this);
            }
        });

        findViewById(R.id.go_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityPermissionsDispatcher.openCameraToVideoWithPermissionCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission({PermissionConstant.CAMERA, PermissionConstant.WRITE_EXTERNAL_STORAGE})
    void openCameraToPic() {
        startActivity(new Intent(MainActivity.this,PictureActivity.class));
    }
    @NeedsPermission({PermissionConstant.CAMERA, PermissionConstant.WRITE_EXTERNAL_STORAGE})
    void openCameraToVideo() {
        startActivity(new Intent(MainActivity.this,VideoActivity.class));
    }
    @OnPermissionDenied({PermissionConstant.CAMERA, PermissionConstant.WRITE_EXTERNAL_STORAGE})
    void permissionDenied() {
        new AlertDialog.Builder(MainActivity.this).setTitle("拒绝提示").setMessage("您拒绝了该权限，将不能访问相机进行拍照").setCancelable(true).show();
    }

    @OnNeverAskAgain({PermissionConstant.CAMERA, PermissionConstant.WRITE_EXTERNAL_STORAGE})
    void neverAskAgain() {
        new AlertDialog.Builder(MainActivity.this).setTitle("权限提示").setMessage("请你允许相机权限和读写文件的权限").setCancelable(true).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}
