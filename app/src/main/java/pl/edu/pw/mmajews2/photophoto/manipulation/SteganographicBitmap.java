package pl.edu.pw.mmajews2.photophoto.manipulation;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Maciej Majewski on 2016-05-07.
 */
public class SteganographicBitmap {
    private static final String TAG = "SteganographicBitmap";
    private Bitmap mainBitmap;
    private String securityPassword;

    public SteganographicBitmap(Bitmap mainBitmap, String securityPassword) {
        this.mainBitmap = mainBitmap;
        this.securityPassword = securityPassword;
    }

    private byte[] serializeHiddenContent(HiddenContent hiddenContent, int align){
        ByteArrayOutputStream hiddenContentStream = new ByteArrayOutputStream(align);
        try {
            Random random = new Random();
            ObjectOutputStream hiddenContentObjectStream = new ObjectOutputStream(hiddenContentStream);
            hiddenContentObjectStream.writeObject(hiddenContent);
            hiddenContentObjectStream.close();
//            hiddenContentObjectStream.flush();
//            byte[] filler = new byte[align - hiddenContentStream.size()];
//            hiddenContentStream.write(filler);
            while(hiddenContentStream.size() <= align){
                hiddenContentStream.write(random.nextInt(126));
            }
            hiddenContentStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Hidden Content serialization failure", e);
        }
        return hiddenContentStream.toByteArray();
    }

    public void writeHiddenContent(HiddenContent hiddenContent, int bitsPerChannel, int align){
        try {
            int pixel = (mainBitmap.getPixel(0, 0) & 0xFFFFFF00) | bitsPerChannel;
            mainBitmap.setPixel(0,0,pixel);

            if(securityPassword!=null){
                try {
                    hiddenContent.setPicture(encrypt(hiddenContent.getPicture()));
                }catch ( NoSuchAlgorithmException
                        |NoSuchPaddingException
                        |InvalidAlgorithmParameterException
                        |InvalidKeySpecException
                        |InvalidKeyException
                        |BadPaddingException
                        |IllegalBlockSizeException e){
                    Log.e(TAG, "Hidden Content security failure", e);
                }
            }
            BitInputStream bitInputStream = new BitInputStream(serializeHiddenContent(hiddenContent, align));
            byte mask = bitInputStream.getMask(bitsPerChannel);

            for (int x = 1; x < mainBitmap.getWidth(); x++) {
                for (int y = 0; y < mainBitmap.getHeight(); y++) {
                    int color = mainBitmap.getPixel(x,y);
                    int red   = (Color.red(color)   & mask) | bitInputStream.getBits(bitsPerChannel);
                    int green = (Color.green(color) & mask) | bitInputStream.getBits(bitsPerChannel);
                    int blue  = (Color.blue(color)  & mask) | bitInputStream.getBits(bitsPerChannel);

                    mainBitmap.setPixel(x,y, Color.rgb(red, green, blue));
                }
            }

        }catch (Throwable e){
            Log.e(TAG, "Hidden Content deserialization failure", e);
            throw e;
        }
    }

    public HiddenContent readHiddenContent(){
        HiddenContent hiddenContent = null;
        try {

            int bitsPerChannel = (mainBitmap.getPixel(0, 0) & 0x000000FF);
            int hiddenContentSize = (mainBitmap.getWidth() * mainBitmap.getHeight() * 3/*channels*/ * bitsPerChannel) / 8;

            BitOutputStream bitOutputStream = new BitOutputStream(hiddenContentSize);
            byte mask = bitOutputStream.getMask(bitsPerChannel);

            for (int x = 1; x < mainBitmap.getWidth(); x++) {
                for (int y = 0; y < mainBitmap.getHeight(); y++) {
                    int color = mainBitmap.getPixel(x, y);
                    int red =   (Color.red(color)   & mask);
                    int green = (Color.green(color) & mask);
                    int blue =  (Color.blue(color)  & mask);

                    bitOutputStream.putBits(bitsPerChannel, red);
                    bitOutputStream.putBits(bitsPerChannel, green);
                    bitOutputStream.putBits(bitsPerChannel, blue);
                }
            }

            ByteArrayInputStream inputStream = new ByteArrayInputStream(bitOutputStream.getContent());
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                hiddenContent = (HiddenContent) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "Hidden Content deserialization failure", e);
            }

            if(securityPassword!=null){
                try {
                    hiddenContent.setPicture(decrypt(hiddenContent.getPicture()));
                }catch ( NoSuchAlgorithmException
                        |NoSuchPaddingException
                        |InvalidAlgorithmParameterException
                        |InvalidKeySpecException
                        |InvalidKeyException
                        |BadPaddingException
                        |IllegalBlockSizeException e){
                    Log.e(TAG, "Hidden Content security failure", e);
                }
            }

        }catch (Throwable e){
            Log.e(TAG, "Hidden Content deserialization failure", e);
            throw e;
        }

        return hiddenContent;
    }

    private byte[] notRandomSalt(){
        return new byte[]{(byte) 0xDE, (byte) 0xAD, 0x12, (byte) 0xBF};
    }
    private SecretKey getSecretKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        int iterationCount = 1000;
        int keyLength = 256;

        KeySpec keySpec = new PBEKeySpec(securityPassword.toCharArray(), notRandomSalt(), iterationCount, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] encrypt(byte[] input) throws NoSuchAlgorithmException,
                                                NoSuchPaddingException,
                                                InvalidAlgorithmParameterException,
                                                InvalidKeySpecException,
                                                InvalidKeyException,
                                                BadPaddingException,
                                                IllegalBlockSizeException {


        Cipher cipher = Cipher.getInstance("DESEDE");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        return cipher.doFinal(input);
    }

    private byte[] decrypt(byte[] input) throws NoSuchAlgorithmException,
                                                NoSuchPaddingException,
                                                InvalidAlgorithmParameterException,
                                                InvalidKeySpecException,
                                                InvalidKeyException,
                                                BadPaddingException,
                                                IllegalBlockSizeException {
        SecretKey key = getSecretKey();

        Cipher cipher = Cipher.getInstance("DESEDE");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(input);
    }
}
