package pl.edu.pw.mmajews2.photophoto.decoding;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import pl.edu.pw.mmajews2.photophoto.taking.OnTouchCallback;

/**
 * Created by Maciej Majewski on 2016-05-01.
 */
abstract public class PermissionAcquiringActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, Runnable {
    private static final String TAG = "PhotoPhoto-Permissions";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 222 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Got permissions");
            onPermissionsGranted();
        }else {
            Log.e(TAG, "No permissions gotten");
            onPermissionsDenied();
        }
    }

    @Override
    public void run() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Asking for Camera permissions");

            String[] req = new String[1];
            req[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(this, req, 222);
        } else {
            Log.i(TAG, "Camera permissions granted");
            onPermissionsGranted();
        }
    }

    protected abstract void onPermissionsGranted();
    protected abstract void onPermissionsDenied();
}
