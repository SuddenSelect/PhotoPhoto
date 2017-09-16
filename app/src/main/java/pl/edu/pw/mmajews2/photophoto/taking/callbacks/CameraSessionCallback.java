package pl.edu.pw.mmajews2.photophoto.taking.callbacks;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import java.util.List;

/**
 * Created by Maciej Majewski on 2016-04-24.
 */
public class CameraSessionCallback extends CameraCaptureSession.StateCallback {
    private static final String TAG = "PhotoPhoto-PrevSession";
    private List<Surface> targets;
    private CameraCaptureSession captureSession = null;
    private Runnable nextTask = null;
    private Handler taskHandler = null;

    private int jpegOrientation;

    public CameraSessionCallback(List<Surface> targets, int jpegOrientation) {
        this.targets = targets;
        this.jpegOrientation = jpegOrientation;
        Log.d(TAG, "JPEG_ORIENTATION="+jpegOrientation);
    }

    public void setNextTask(Handler taskHandler, Runnable nextTask){
        this.taskHandler = taskHandler;
        this.nextTask = nextTask;
    }

    @Override
    public void onConfigured(CameraCaptureSession session) {
        try {
            CaptureRequest.Builder builder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            for(Surface target : targets){
                builder.addTarget(target);
            }
            builder.set(CaptureRequest.JPEG_QUALITY, (byte) 100);
            builder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);

            session.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback(){}, null);

            captureSession = session;

            if(nextTask!=null){
                taskHandler.post(nextTask);
                nextTask = null;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Capture Session failed", e);
            session.getDevice().close();
        }
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {
        Log.e(TAG, "Capture Session configuration failed");
        captureSession = null;
        session.getDevice().close();
    }

    @Override
    public void onClosed(CameraCaptureSession session) {
        captureSession = null;
        session.getDevice().close();
        super.onClosed(session);
        if(nextTask!=null){
            taskHandler.post(nextTask);
            nextTask = null;
        }
    }

    public CameraCaptureSession getCaptureSession() {
        return captureSession;
    }

}
