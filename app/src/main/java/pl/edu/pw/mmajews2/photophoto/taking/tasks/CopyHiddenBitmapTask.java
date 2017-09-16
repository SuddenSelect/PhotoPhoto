package pl.edu.pw.mmajews2.photophoto.taking.tasks;

import android.graphics.Bitmap;
import android.media.MediaActionSound;
import android.util.Log;

/**
 * Created by Maciej Majewski on 2016-05-02.
 */
public class CopyHiddenBitmapTask extends CameraTask {
    private int sleepMillis;

    public CopyHiddenBitmapTask(CameraTaskContext context, int sleepMillis) {
        super(context);
        this.sleepMillis = sleepMillis;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(sleepMillis, 0);
        } catch (InterruptedException e) {
            Log.e(TAG, "Front camera interrupted", e);
        }

        context.getMediaActionSound().play(MediaActionSound.SHUTTER_CLICK);

        Bitmap bitmap = context.getTextureView().getBitmap(
                context.getHiddenPictureResolution().getHeight(),
                context.getHiddenPictureResolution().getWidth());
//        context.getSurface().release();
//        context.setSurface(new Surface(context.getSurfaceTexture()));

        context.setHiddenPictureBitmap(bitmap);

        postNextTask();
    }
}
