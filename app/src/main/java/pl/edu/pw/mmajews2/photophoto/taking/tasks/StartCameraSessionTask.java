package pl.edu.pw.mmajews2.photophoto.taking.tasks;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.camera2.CameraAccessException;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.LinkedList;
import java.util.List;

import pl.edu.pw.mmajews2.photophoto.taking.callbacks.CameraSessionCallback;
import pl.edu.pw.mmajews2.photophoto.taking.callbacks.CameraStateCallback;

/**
 * Created by Maciej Majewski on 2016-05-02.
 */
public class StartCameraSessionTask extends CameraTask {
    private String cameraId;
    
    public StartCameraSessionTask(CameraTaskContext context, String cameraId) {
        super(context);
        this.cameraId = cameraId;
    }

    private boolean isMainCamera(){
        return cameraId.equals( context.getBackCameraId() );
    }

    @Override
    public void run() {
        Size resolution;
        int cameraOrientation;
        if(isMainCamera()){
            resolution = context.getMainPictureResolution();
            cameraOrientation = context.getBackCameraOrientation();
        }else{
            resolution = context.getHiddenPictureResolution();
            cameraOrientation = context.getFrontCameraOrientation();
        }
        context.getSurfaceTexture().setDefaultBufferSize( resolution.getWidth(), resolution.getHeight() );

        context.getUiHandler().post(applyCorrectionMatrix(resolution));


        List<Surface> surfaceList = new LinkedList<>();
        surfaceList.add(context.getSurface());

        context.setCameraSession(new CameraSessionCallback(surfaceList, calculateJpegOrientation(cameraOrientation)));
        context.setCameraState(new CameraStateCallback(surfaceList, context.getCameraSession()));

        context.getCameraSession().setNextTask(context.getTaskHandler(), getNextTask());

        try {
            context.getCameraManager().openCamera(cameraId, context.getCameraState(), context.getTaskHandler());
        } catch (CameraAccessException|SecurityException e) {
            Log.e(TAG, "Camera access failure", e);
        }

        if(isMainCamera()) {
            context.getTextureView().setOnTouchListener(context.getOnTouchListener());
        }
    }

    private int calculateJpegOrientation(int cameraOrientation) {
//        return cameraOrientation;
        int deviceOrientation = context.getWindowManager().getDefaultDisplay().getRotation();

        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN){
            return 0;
        }

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        if (!isMainCamera()) {
            deviceOrientation = -deviceOrientation;
        }

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (cameraOrientation + deviceOrientation + 360) % 360;
    }

    private Runnable applyCorrectionMatrix(final Size resolution){
        return new Runnable() {
            @Override
            public void run() {
                float screenRatio = context.getTextureView().getWidth()/context.getTextureView().getHeight();
                float resRatio = resolution.getWidth()/resolution.getHeight();

                if(Math.abs(screenRatio - resRatio) > 0.1){
                    RectF textureRectF = new RectF(0, 0, resolution.getWidth(), resolution.getHeight());
                    RectF previewRectF = new RectF(0, 0, context.getTextureView().getWidth(), context.getTextureView().getHeight());
                    textureRectF.offset(previewRectF.centerX() - textureRectF.centerX(), previewRectF.centerY() - textureRectF.centerY());

                    Log.d(TAG, "Resolution : "+textureRectF);
                    Log.d(TAG, "TextureView: "+previewRectF);

                    float previewArea = previewRectF.width() * previewRectF.height();
                    float textureArea = textureRectF.width() * textureRectF.height();
                    float scale = previewArea / textureArea;
                    if(scale < 1.7){
                        scale = 2;
                    }

                    Matrix matrix = new Matrix();
                    matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.CENTER);
                    matrix.postScale(scale, scale, textureRectF.centerX(), textureRectF.centerY());

                    context.getTextureView().setTransform(matrix);
                }

            }
        };
    }
}
