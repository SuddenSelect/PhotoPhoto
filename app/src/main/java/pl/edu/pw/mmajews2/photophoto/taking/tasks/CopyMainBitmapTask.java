package pl.edu.pw.mmajews2.photophoto.taking.tasks;

import android.graphics.Bitmap;
import android.media.MediaActionSound;

/**
 * Created by Maciej Majewski on 2016-05-02.
 */
public class CopyMainBitmapTask extends CameraTask {
    public CopyMainBitmapTask(CameraTaskContext context) {
        super(context);
    }

    @Override
    public void run() {
        context.getMediaActionSound().play(MediaActionSound.SHUTTER_CLICK);

        Bitmap bitmap = context.getTextureView().getBitmap(
                context.getMainPictureResolution().getHeight(),
                context.getMainPictureResolution().getWidth());
//        context.getSurface().release();
//        context.setSurface(new Surface(context.getSurfaceTexture()));

        context.setMainPictureBitmap(bitmap);


        postNextTask();
    }
}
