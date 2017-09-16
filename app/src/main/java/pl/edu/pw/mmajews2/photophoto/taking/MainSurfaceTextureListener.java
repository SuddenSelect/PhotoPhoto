package pl.edu.pw.mmajews2.photophoto.taking;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import pl.edu.pw.mmajews2.photophoto.taking.tasks.CameraTask;
import pl.edu.pw.mmajews2.photophoto.taking.tasks.CameraTaskContext;

/**
 * Created by Maciej Majewski on 2016-05-02.
 */
public class MainSurfaceTextureListener implements TextureView.SurfaceTextureListener {
    private CameraTaskContext context;
    private CameraTask task;

    public MainSurfaceTextureListener(CameraTaskContext context, CameraTask task) {
        this.context = context;
        this.task = task;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        context.setSurfaceTexture(surface);
        context.setSurface(new Surface(surface));
        context.getTaskHandler().post(task);
//        Log.d("PP-DBG", "New surface texture! "+surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        Log.d("PP-DBG", "TRANSFORM!");
//        configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

//    private void configureTransform(int viewWidth, int viewHeight) {
//        int rotation = context.getWindowManager().getDefaultDisplay().getRotation();
//        Matrix matrix = new Matrix();
//        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
//        RectF bufferRect = new RectF(0, 0, context.getTextureView().getHeight(), context.getTextureView().getWidth());
//        float centerX = viewRect.centerX();
//        float centerY = viewRect.centerY();
//        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
//            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
//            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
//            float scale = Math.max(
//                    (float) viewHeight / context.getTextureView().getHeight(),
//                    (float) viewWidth / context.getTextureView().getWidth());
//            matrix.postScale(scale, scale, centerX, centerY);
//            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
//        } else if (Surface.ROTATION_180 == rotation) {
//            matrix.postRotate(180, centerX, centerY);
//        }
//        context.getTextureView().setTransform(matrix);
//    }
}
