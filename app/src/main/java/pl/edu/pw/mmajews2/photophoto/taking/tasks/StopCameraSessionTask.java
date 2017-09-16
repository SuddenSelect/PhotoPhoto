package pl.edu.pw.mmajews2.photophoto.taking.tasks;

import android.hardware.camera2.CameraAccessException;
import android.util.Log;

/**
 * Created by Maciej Majewski on 2016-05-02.
 */
public class StopCameraSessionTask extends CameraTask {
    private String cameraId;

    public StopCameraSessionTask(CameraTaskContext context, String cameraId) {
        super(context);
        this.cameraId = cameraId;
    }

    private boolean isMainCamera(){
        return cameraId.equals( context.getBackCameraId() );
    }

    @Override
    public void run() {
        try {
            if(isMainCamera()) {
                context.getTextureView().setOnTouchListener(null);
            }

            context.getCameraSession().setNextTask(context.getTaskHandler(), getNextTask());

            context.getCameraSession().getCaptureSession().stopRepeating();
            context.getCameraSession().getCaptureSession().close();
//            context.getCameraSession().getCaptureSession().getDevice().close(); //in onClose

            context.setCameraState(null);
            context.setCameraSession(null);

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access failure", e);
        }
    }
}
