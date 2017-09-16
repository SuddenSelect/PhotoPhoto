package pl.edu.pw.mmajews2.photophoto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PhotoPhoto-MainActivity";

    private CameraManager manager = null;
    private Size[] mainCameraSizes = null;
    private Size[] hiddenCameraSizes = null;
    private String frontCameraId = null;
    private String backCameraId = null;
    private int backCameraOrientation = 0;
    private int frontCameraOrientation = 0;
    private Handler handler = null;
    private volatile boolean launching = false;

    private Bundle preferenceBundle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        manager =(CameraManager) getSystemService(Context.CAMERA_SERVICE);
        handler = new Handler(getMainLooper());

        setPreferenceBundle();
    }

    private void setPreferenceBundle(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceBundle = new Bundle();
        for(String key : preferences.getAll().keySet()){
            Object preference = preferences.getAll().get(key);
            if(preference instanceof Boolean){
                preferenceBundle.putBoolean(key, (Boolean) preference);
            } else {
                preferenceBundle.putString(key, String.valueOf(preference));
            }
            Log.d(TAG, "Preference ["+key+"]: "+String.valueOf(preference));
        }

        try {
            findCameras();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Init exception", e);
        }

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resume");
        setPreferenceBundle();
        super.onResume();
    }

    private void findCameras() throws CameraAccessException {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float nativeRatio = Math.max(
                displayMetrics.widthPixels/(float)displayMetrics.heightPixels,
                displayMetrics.heightPixels/(float)displayMetrics.widthPixels);
//        Log.d(TAG, "RATIO "+nativeRatio);

        for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics cc = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configs = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (cc.get(cc.LENS_FACING).equals(cc.LENS_FACING_BACK)) {
                Log.i(TAG, "Found back camera: " + cameraId);
                backCameraId = cameraId;
                backCameraOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);

                mainCameraSizes = viableCameraSizes(configs.getOutputSizes(SurfaceHolder.class), nativeRatio);

                preferenceBundle.putString("pl.edu.pw.mmajews2.photophoto.back_camera_id", backCameraId);
                preferenceBundle.putInt("pl.edu.pw.mmajews2.photophoto.back_camera_orientation", backCameraOrientation);
            }
            if (cc.get(cc.LENS_FACING).equals(cc.LENS_FACING_FRONT)) {
                Log.i(TAG, "Found front camera: " + cameraId);
                frontCameraId = cameraId;
                frontCameraOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);

                hiddenCameraSizes = viableCameraSizes(configs.getOutputSizes(SurfaceHolder.class), nativeRatio);

                preferenceBundle.putString("pl.edu.pw.mmajews2.photophoto.front_camera_id", frontCameraId);
                preferenceBundle.putInt("pl.edu.pw.mmajews2.photophoto.front_camera_orientation", frontCameraOrientation);
            }
        }
    }

    private Size[] viableCameraSizes(Size[] sizes, float nativeRatio){
        ArrayList<Size> viableSizes = new ArrayList<>(sizes.length);
        for (int i = 0; i < sizes.length; i++) {
            Size s = sizes[i];
//            Log.d(TAG, "RATIO "+s.getWidth()/(float)s.getHeight());
            if(Math.abs(s.getWidth()/(float)s.getHeight() - nativeRatio) < 0.3){
                viableSizes.add(s);
                Log.i(TAG, "Found viable res: " + s.toString());
            }
        }
        viableSizes.trimToSize();

        Size[] result = new Size[viableSizes.size()];
        result = viableSizes.toArray(result);
        return result;
    }

    public void launchSettingActivity(View view){
        if(launching){
            return;
        }else{
            launching=true;
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_menu_settings);
        ImageView button = (ImageView) findViewById(R.id.settingsButton);
        button.startAnimation(animation);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SettingsActivity.MAIN_RESOLUTIONS = mainCameraSizes;
                SettingsActivity.HIDDEN_RESOLUTIONS = hiddenCameraSizes;
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(intent);

                launching = false;
            }
        }, 600);

    }

    public void launchTakePictureActivity(View view){
        if(launching){
            return;
        }else{
            launching=true;
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_menu_take_picture);
        ImageView button = (ImageView) findViewById(R.id.takePictureButton);
        button.startAnimation(animation);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, TakePictureActivity.class);
                intent.putExtras(preferenceBundle);
                MainActivity.this.startActivity(intent);
                launching = false;
            }
        }, 600);

    }


    public void launchDecodePictureActivity(View view){
        if(launching){
            return;
        }else{
            launching=true;
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_menu_decode);
        ImageView button = (ImageView) findViewById(R.id.decodePictureButton);
        button.startAnimation(animation);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, DecodeActivity.class);
                intent.putExtras(preferenceBundle);
                MainActivity.this.startActivity(intent);
                launching = false;
            }
        }, 600);
    }
}
