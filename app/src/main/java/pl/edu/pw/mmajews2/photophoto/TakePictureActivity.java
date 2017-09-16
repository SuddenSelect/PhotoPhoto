package pl.edu.pw.mmajews2.photophoto;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraManager;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.util.Calendar;

import pl.edu.pw.mmajews2.photophoto.taking.OnTouchListener;
import pl.edu.pw.mmajews2.photophoto.taking.MainSurfaceTextureListener;
import pl.edu.pw.mmajews2.photophoto.taking.PermissionAcquiringActivity;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.AnimatedSavingTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.AnimatedTransitionTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.CameraTaskChainBuilder;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.CameraTaskContext;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.CopyHiddenBitmapTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.CopyMainBitmapTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.EmbedBitmapTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.SaveMainBitmapTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.StartCameraSessionTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.StopAnimatedSavingTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.StopCameraSessionTask;

public class TakePictureActivity extends PermissionAcquiringActivity {
    private static final String TAG = "PhotoPhoto-TakePicture";

    private ViewSwitcher viewSwitcher = null;
    private TextureView textureView = null;
    private ImageView savingImageView = null;

    private SharedPreferences preferences = null;
    private CameraTaskContext cameraTaskContext = new CameraTaskContext();

    private HandlerThread handlerThread = new HandlerThread("TakePictureActivityHandleThread");
    private Handler taskHandler;
    private Handler uiHandler;

    private MediaActionSound mediaActionSound = new MediaActionSound();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(
//                Window.FEATURE_SWIPE_TO_DISMISS |
                Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_take_picture);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        textureView = (TextureView) findViewById(R.id.textureView);
        savingImageView = (ImageView) findViewById(R.id.savingImageView);

        viewSwitcher.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );


        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        handlerThread.setDaemon(true);
        handlerThread.start();
        taskHandler = new Handler(handlerThread.getLooper());

        uiHandler = new Handler(this.getMainLooper());

        setupCameraTaskContext();

        uiHandler.post(this);

        mediaActionSound.load(MediaActionSound.SHUTTER_CLICK);
    }

    private void setupCameraTaskContext(){
        cameraTaskContext.setViewSwitcher(viewSwitcher);
        cameraTaskContext.setTextureView(textureView);
        cameraTaskContext.setWindowManager(getWindowManager());
        cameraTaskContext.setOnTouchListener(new OnTouchListener(this));

        cameraTaskContext.setCameraManager((CameraManager) getSystemService(Context.CAMERA_SERVICE));
        cameraTaskContext.setBackCameraId(getIntent().getStringExtra("pl.edu.pw.mmajews2.photophoto.back_camera_id"));
        cameraTaskContext.setFrontCameraId(getIntent().getStringExtra("pl.edu.pw.mmajews2.photophoto.front_camera_id"));
        cameraTaskContext.setBackCameraOrientation(getIntent().getIntExtra("pl.edu.pw.mmajews2.photophoto.back_camera_orientation", 0));
        cameraTaskContext.setFrontCameraOrientation(getIntent().getIntExtra("pl.edu.pw.mmajews2.photophoto.front_camera_orientation", 0));
        cameraTaskContext.isHiddenPreviewEnabled(getIntent().getBooleanExtra("pl.edu.pw.mmajews2.photophoto.picture.hidden.preview", false));
        String exposure = getIntent().getStringExtra("pl.edu.pw.mmajews2.photophoto.picture.exposure");
        if(exposure == null) exposure = "1000";
        cameraTaskContext.setExposureTime(Integer.valueOf(exposure));
        cameraTaskContext.setFilePrefix(getIntent().getStringExtra("pl.edu.pw.mmajews2.photophoto.picture.file_prefix"));
        String channelBits = getIntent().getStringExtra("pl.edu.pw.mmajews2.photophoto.picture.channel_bits");
        if(channelBits == null) channelBits = "1000";
        cameraTaskContext.setBitsPerChannel(Integer.valueOf(channelBits));

        cameraTaskContext.setMetadataEnabled(getIntent().getBooleanExtra("pl.edu.pw.mmajews2.photophoto.metadata.marker.switch", false));
        cameraTaskContext.setMetadataText(getIntent().getStringExtra("pl.edu.pw.mmajews2.photophoto.metadata.marker.text"));
        cameraTaskContext.setMetadataDateEnabled(getIntent().getBooleanExtra("pl.edu.pw.mmajews2.photophoto.metadata.marker.date", false));
        cameraTaskContext.setMetadataGPSEnabled(getIntent().getBooleanExtra("pl.edu.pw.mmajews2.photophoto.metadata.marker.gps", false));

        cameraTaskContext.setEncryptionEnabled(getIntent().getBooleanExtra("pl.edu.pw.mmajews2.photophoto.encryption.enabled_switch", false));
        cameraTaskContext.setEncryptionPassword(getIntent().getStringExtra("pl.edu.pw.mmajews2.photophoto.encryption.password"));

        cameraTaskContext.setTaskHandler(taskHandler);
        cameraTaskContext.setUiHandler(uiHandler);
        cameraTaskContext.setMediaActionSound(mediaActionSound);

        Size mainPictureResolution = Size.parseSize( preferences.getString("pl.edu.pw.mmajews2.photophoto.picture.main.resolution", "800x600") );
        Size hiddenPictureResolution = Size.parseSize( preferences.getString("pl.edu.pw.mmajews2.photophoto.picture.hidden.resolution", "800x600") );
        cameraTaskContext.setMainPictureResolution(mainPictureResolution);
        cameraTaskContext.setHiddenPictureResolution(hiddenPictureResolution);

        cameraTaskContext.setSavingAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_photo_saving));
        cameraTaskContext.setSavingIndicatorView(savingImageView);
    }

    @Override
    protected void onDestroy() {
        Log.d("PP-DBG", "Destroying");
        if(cameraTaskContext.getCameraSession() != null && cameraTaskContext.getCameraSession().getCaptureSession() != null){
            taskHandler.post(new StopCameraSessionTask(cameraTaskContext,
                    cameraTaskContext.getCameraSession().getCaptureSession().getDevice().getId()));
        }
//        try {
//            handlerThread.join(3000);
//        } catch (InterruptedException e) {
//            Log.e(TAG, "HandlerThread interrupted", e);
//        }
        super.onDestroy();
    }


    @Override
    protected void onPermissionsGranted() {
        StartCameraSessionTask sessionTask = new StartCameraSessionTask(cameraTaskContext, cameraTaskContext.getBackCameraId());
        MainSurfaceTextureListener mainSurfaceTextureListener = new MainSurfaceTextureListener(cameraTaskContext, sessionTask);
        textureView.setSurfaceTextureListener(mainSurfaceTextureListener);
    }

    @Override
    protected void onPermissionsDenied() {
        Log.d(TAG, "No permissions - Finishing");
        finish();
    }

    @Override
    public void onTouched() {
        onMainPictureTaking();
    }

    @Override
    protected void onMainPictureTaking() {
        textureView.setOnTouchListener(null);

        boolean showHiddenPreview = cameraTaskContext.isHiddenPreview();
        int exposureTime = cameraTaskContext.getExposureTime();
        String filePrefix = cameraTaskContext.getFilePrefix()+ Calendar.getInstance().getTimeInMillis();

        CameraTaskChainBuilder taskChainBuilder = new CameraTaskChainBuilder();
        taskChainBuilder.chainTask(new StopCameraSessionTask(cameraTaskContext, cameraTaskContext.getBackCameraId()));
        taskChainBuilder.chainTask(new AnimatedTransitionTask(cameraTaskContext));
        taskChainBuilder.chainTask(new CopyMainBitmapTask(cameraTaskContext));

        taskChainBuilder.chainTask(new StartCameraSessionTask(cameraTaskContext, cameraTaskContext.getFrontCameraId()));
        if(showHiddenPreview){
            taskChainBuilder.chainTask(new AnimatedTransitionTask(cameraTaskContext));
        }

        taskChainBuilder.chainTask(new CopyHiddenBitmapTask(cameraTaskContext, exposureTime));
        taskChainBuilder.chainTask(new StopCameraSessionTask(cameraTaskContext, cameraTaskContext.getFrontCameraId()));
        if(showHiddenPreview){
            taskChainBuilder.chainTask(new AnimatedTransitionTask(cameraTaskContext));
        }

        taskChainBuilder.chainTask(new StartCameraSessionTask(cameraTaskContext, cameraTaskContext.getBackCameraId()));
        taskChainBuilder.chainTask(new AnimatedSavingTask(cameraTaskContext));
        taskChainBuilder.chainTask(new EmbedBitmapTask(cameraTaskContext));
        taskChainBuilder.chainTask(new SaveMainBitmapTask(cameraTaskContext, filePrefix + ".png"));
//        taskChainBuilder.chainTask(new SaveHiddenBitmapTask(cameraTaskContext, filePrefix + "-hidden.png"));
        taskChainBuilder.chainTask(new StopAnimatedSavingTask(cameraTaskContext));
        taskChainBuilder.chainTask(new AnimatedTransitionTask(cameraTaskContext));

        taskHandler.post(taskChainBuilder.buildChain());
    }

}
