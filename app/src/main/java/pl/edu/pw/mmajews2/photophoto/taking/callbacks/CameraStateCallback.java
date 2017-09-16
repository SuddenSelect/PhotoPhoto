package pl.edu.pw.mmajews2.photophoto.taking.callbacks;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.util.Log;
import android.view.Surface;

import java.util.List;

/**
 * Created by Maciej Majewski on 2016-05-01.
 */
public class CameraStateCallback extends CameraDevice.StateCallback{
    private static final String TAG = "PhotoPhoto-CamState";

    private List<Surface> surfaceList;
    private CameraDevice openedCamera = null;
    private CameraCaptureSession.StateCallback sessionCallback;

    public CameraStateCallback(List<Surface> surfaceList, CameraCaptureSession.StateCallback sessionCallback) {
        this.surfaceList = surfaceList;
        this.sessionCallback = sessionCallback;
    }

    @Override
    public void onOpened(CameraDevice camera) {
        try {
            camera.createCaptureSession(surfaceList, sessionCallback, null);
            openedCamera = camera;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Session failure", e);
            camera.close();
        }
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
        Log.i(TAG, "Camera disconnected: "+camera.getId());
        camera.close();
    }

    @Override
    public void onError(CameraDevice camera, int error) {
        Log.i(TAG, "Camera errored: "+camera.getId()+", error: "+error);
        camera.close();
    }

    @Override
    public void onClosed(CameraDevice camera) {
        openedCamera = null;
    }

    public CameraDevice getOpenedCamera(){
        return openedCamera;
    }
}
