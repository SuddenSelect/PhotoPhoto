package pl.edu.pw.mmajews2.photophoto.taking.tasks;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.media.MediaActionSound;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import java.io.Serializable;

import pl.edu.pw.mmajews2.photophoto.taking.OnTouchListener;
import pl.edu.pw.mmajews2.photophoto.taking.callbacks.CameraSessionCallback;
import pl.edu.pw.mmajews2.photophoto.taking.callbacks.CameraStateCallback;

/**
 * Created by Maciej Majewski on 2016-05-02.
 */
public class CameraTaskContext implements Serializable{
    private static final long serialVersionUID = 11110022L;

    private CameraManager cameraManager = null;
    private String frontCameraId = null;
    private String backCameraId = null;
    private int backCameraOrientation = 0;
    private int frontCameraOrientation = 0;
    private boolean hiddenPreview = false;
    private int exposureTime = 1000;
    private String filePrefix = null;
    private Handler taskHandler = null;
    private Handler uiHandler = null;
    private MediaActionSound mediaActionSound = null;

    private CameraSessionCallback cameraSession = null;
    private CameraStateCallback cameraState = null;

    private ViewSwitcher viewSwitcher = null;
    private TextureView textureView = null;
    private Surface surface = null;
    private SurfaceTexture surfaceTexture = null;
    private WindowManager windowManager = null;
    private OnTouchListener onTouchListener;

    private Size mainPictureResolution = null;
    private Size hiddenPictureResolution = null;
    private Bitmap mainPictureBitmap = null;
    private Bitmap hiddenPictureBitmap = null;
    private int bitsPerChannel = 4;

    private Animation savingAnimation = null;
    private ImageView savingIndicatorView = null;

    private boolean metadataEnabled = false;
    private String metadataText = null;
    private boolean metadataDate = false;
    private boolean metadataGPS = false;
    private boolean encryptionEnabled;
    private String encryptionPassword;

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    public String getFrontCameraId() {
        return frontCameraId;
    }

    public void setFrontCameraId(String frontCameraId) {
        this.frontCameraId = frontCameraId;
    }

    public String getBackCameraId() {
        return backCameraId;
    }

    public void setBackCameraId(String backCameraId) {
        this.backCameraId = backCameraId;
    }

    public Handler getTaskHandler() {
        return taskHandler;
    }

    public void setTaskHandler(Handler taskHandler) {
        this.taskHandler = taskHandler;
    }

    public CameraSessionCallback getCameraSession() {
        return cameraSession;
    }

    public void setCameraSession(CameraSessionCallback cameraSession) {
        this.cameraSession = cameraSession;
    }

    public CameraStateCallback getCameraState() {
        return cameraState;
    }

    public void setCameraState(CameraStateCallback cameraState) {
        this.cameraState = cameraState;
    }

    public TextureView getTextureView() {
        return textureView;
    }

    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
    }

    public Surface getSurface() {
        return surface;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }

    public Size getMainPictureResolution() {
        return mainPictureResolution;
    }

    public void setMainPictureResolution(Size mainPictureResolution) {
        this.mainPictureResolution = mainPictureResolution;
    }

    public Size getHiddenPictureResolution() {
        return hiddenPictureResolution;
    }

    public void setHiddenPictureResolution(Size hiddenPictureResolution) {
        this.hiddenPictureResolution = hiddenPictureResolution;
    }

    public void setMainPictureBitmap(Bitmap mainPictureBitmap) {
        this.mainPictureBitmap = mainPictureBitmap;
    }

    public Bitmap getMainPictureBitmap() {
        return mainPictureBitmap;
    }

    public void setHiddenPictureBitmap(Bitmap hiddenPictureBitmap) {
        this.hiddenPictureBitmap = hiddenPictureBitmap;
    }

    public Bitmap getHiddenPictureBitmap() {
        return hiddenPictureBitmap;
    }

    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public OnTouchListener getOnTouchListener() {
        return onTouchListener;
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public void setUiHandler(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    public Handler getUiHandler() {
        return uiHandler;
    }

    public void setBackCameraOrientation(int backCameraOrientation) {
        this.backCameraOrientation = backCameraOrientation;
    }

    public int getBackCameraOrientation() {
        return backCameraOrientation;
    }

    public void setFrontCameraOrientation(int frontCameraOrientation) {
        this.frontCameraOrientation = frontCameraOrientation;
    }

    public int getFrontCameraOrientation() {
        return frontCameraOrientation;
    }

    public void setMediaActionSound(MediaActionSound mediaActionSound) {
        this.mediaActionSound = mediaActionSound;
    }

    public MediaActionSound getMediaActionSound() {
        return mediaActionSound;
    }

    public void setViewSwitcher(ViewSwitcher viewSwitcher) {
        this.viewSwitcher = viewSwitcher;
    }

    public ViewSwitcher getViewSwitcher() {
        return viewSwitcher;
    }


    public void setSavingAnimation(Animation savingAnimation) {
        this.savingAnimation = savingAnimation;
    }

    public Animation getSavingAnimation() {
        return savingAnimation;
    }

    public void setSavingIndicatorView(ImageView savingIndicatorView) {
        this.savingIndicatorView = savingIndicatorView;
    }

    public ImageView getSavingIndicatorView() {
        return savingIndicatorView;
    }

    public void isHiddenPreviewEnabled(boolean hiddenPreview) {
        this.hiddenPreview = hiddenPreview;
    }

    public boolean isHiddenPreview() {
        return hiddenPreview;
    }

    public void setExposureTime(int exposureTime) {
        this.exposureTime = exposureTime;
    }

    public int getExposureTime() {
        return exposureTime;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public boolean isMetadataEnabled() {
        return metadataEnabled;
    }

    public void setMetadataEnabled(boolean metadataEnabled) {
        this.metadataEnabled = metadataEnabled;
    }

    public String getMetadataText() {
        return metadataText;
    }

    public void setMetadataText(String metadataText) {
        this.metadataText = metadataText;
    }

    public boolean isMetadataDateEnabled() {
        return metadataDate;
    }

    public void setMetadataDateEnabled(boolean metadataDate) {
        this.metadataDate = metadataDate;
    }

    public boolean isMetadataGPSEnabled() {
        return metadataGPS;
    }

    public void setMetadataGPSEnabled(boolean metadataGPS) {
        this.metadataGPS = metadataGPS;
    }

    public void setEncryptionEnabled(boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    public String getEncryptionPassword() {
        return encryptionPassword;
    }

    public int getBitsPerChannel() {
        return bitsPerChannel;
    }

    public void setBitsPerChannel(int bitsPerChannel) {
        this.bitsPerChannel = bitsPerChannel;
    }
}
