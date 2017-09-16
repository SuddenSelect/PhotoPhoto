package pl.edu.pw.mmajews2.photophoto.taking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Maciej Majewski on 2016-05-01.
 */
abstract public class PermissionAcquiringActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, Runnable, OnTouchCallback {
    private static final String TAG = "PhotoPhoto-Permissions";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Got permissions");
            onPermissionsGranted();
        }else {
            Log.e(TAG, "No permissions gotten");
            onPermissionsDenied();
        }
    }

    @Override
    public void run() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
         || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Asking for Camera permissions");

            String[] req = new String[2];
            req[0] = Manifest.permission.CAMERA;
            req[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(this, req, 111);
        } else {
            Log.i(TAG, "Camera permissions granted");
            onPermissionsGranted();
        }
    }

    protected abstract void onPermissionsGranted();
    protected abstract void onPermissionsDenied();
    protected abstract void onMainPictureTaking();

}
