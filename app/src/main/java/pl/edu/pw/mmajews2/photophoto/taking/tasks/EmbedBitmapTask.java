package pl.edu.pw.mmajews2.photophoto.taking.tasks;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.edu.pw.mmajews2.photophoto.manipulation.HiddenContent;
import pl.edu.pw.mmajews2.photophoto.manipulation.HiddenMetadata;
import pl.edu.pw.mmajews2.photophoto.manipulation.SteganographicBitmap;

/**
 * Created by Maciej Majewski on 2016-05-07.
 */
public class EmbedBitmapTask extends CameraTask {
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public EmbedBitmapTask(CameraTaskContext context) {
        super(context);
    }

    @Override
    public void run() {
//        executorService.submit(new Runnable() {
//
//            @Override
//            public void run() {
                embed();
//            }
//        });
        postNextTask();
    }

    private void embed(){
        Bitmap mainPicture = context.getMainPictureBitmap();
        Bitmap hiddenPicture = context.getHiddenPictureBitmap();
        int bitsPerChannel = context.getBitsPerChannel();
        HiddenContent hiddenContent = new HiddenContent();
        if(context.isMetadataEnabled()) {
            hiddenContent.setMetadata(new HiddenMetadata(context));
        }

        //Assumptions:
        // 1pix = 1byte hidden
        // PNG compression is about 2:1 (with a secure margin)

        int metadataBytes = 1 + getMetadataSize(hiddenContent);
        Log.d(TAG, "Metadata size: " + metadataBytes);


        int availableBytes = (int) Math.floor(
                mainPicture.getWidth() * mainPicture.getHeight()
                * 3 //channels RGB
                * (bitsPerChannel / 8.0) //ratio
        ) - metadataBytes;

        int hiddenPictureBytes = (int) Math.ceil(
                hiddenPicture.getWidth() * hiddenPicture.getHeight() //pixels
                        * 4.0 //RGB bytes
                        * 0.6 //PNG gain (estimated)
        );


        Bitmap adjustedHiddenPicture;
        if(hiddenPictureBytes > availableBytes) {
            adjustedHiddenPicture = getScaledBitmap(hiddenPicture, availableBytes);
            Log.d(TAG, "Hidden picture scaled to size: "+adjustedHiddenPicture.getWidth()+"x"+adjustedHiddenPicture.getHeight());
        }else{
            adjustedHiddenPicture = hiddenPicture;
            Log.d(TAG, "Hidden picture scaling not necessary");
        }

        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream(availableBytes);
        adjustedHiddenPicture.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
        hiddenContent.setPicture(bitmapStream.toByteArray());
        Log.d(TAG, "Hidden picture PNG size: "+bitmapStream.size());
        Log.i(TAG, "Usage: "+(bitmapStream.size()/(double)availableBytes));

        String password = null;
        if(context.isEncryptionEnabled()){
            password = context.getEncryptionPassword();
        }
        SteganographicBitmap steganographicBitmap = new SteganographicBitmap(mainPicture, password);
        steganographicBitmap.writeHiddenContent(hiddenContent, bitsPerChannel, availableBytes);
//
//        HiddenContent readContent = steganographicBitmap.readHiddenContent();
//        boolean isFine = Arrays.equals(hiddenContent.getPicture(), readContent.getPicture());
//        Log.i(TAG, "Success?: "+isFine);

        context.setHiddenPictureBitmap(null);

    }

    private Bitmap getScaledBitmap(Bitmap hiddenPicture, int availableBytes) {
        Bitmap scaledHiddenPicture;
        double dstHiddenPictureArea = Math.floor(availableBytes / 3.0 /0.6);
        double srcHiddenPictureArea = hiddenPicture.getWidth() * hiddenPicture.getHeight();
        double scale = Math.sqrt(dstHiddenPictureArea / srcHiddenPictureArea);
        Log.d(TAG, "Hidden picture scaling: "+scale);

        scaledHiddenPicture = Bitmap.createScaledBitmap(
                hiddenPicture,
                (int) Math.floor(hiddenPicture.getWidth() * scale),
                (int) Math.floor(hiddenPicture.getHeight() * scale),
                true);
        return scaledHiddenPicture;
    }

    private int getMetadataSize(HiddenContent hiddenContent){
        int metadataBytes = 0;
        if(context.isMetadataEnabled()) {
            try {
                ByteArrayOutputStream metadataStream = new ByteArrayOutputStream();
                ObjectOutputStream metadataObjectStream = new ObjectOutputStream(metadataStream);
                metadataObjectStream.writeObject(hiddenContent);
                metadataBytes += metadataStream.size();
            } catch (IOException e) {
                Log.e(TAG, "Metadata serialization failure", e);
            }
        }
        return metadataBytes;
    }
}
