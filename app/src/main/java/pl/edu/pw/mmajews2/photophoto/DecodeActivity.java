package pl.edu.pw.mmajews2.photophoto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import pl.edu.pw.mmajews2.photophoto.decoding.PermissionAcquiringActivity;
import pl.edu.pw.mmajews2.photophoto.decoding.RoundRobinList;
import pl.edu.pw.mmajews2.photophoto.manipulation.HiddenContent;
import pl.edu.pw.mmajews2.photophoto.manipulation.SteganographicBitmap;

public class DecodeActivity extends PermissionAcquiringActivity implements TabHost.OnTabChangeListener {
    private static final String TAG = "PhotoPhoto-Decode";

    private TabHost tabHost = null;
    private ImageView mainPicImageView = null;
    private ImageView hiddenPicImageView = null;
    private TextView textView = null;
    private ImageButton previousImageButton = null;
    private ImageButton nextImageButton = null;

    private RoundRobinList<File> imageList = new RoundRobinList();
    private Handler uiHandler = null;
    private Handler taskHandler = null;
    private HandlerThread taskHandlerThread = null;
    private class Content {
        private Bitmap mainBitmap = null;
        private Bitmap hiddenBitmap = null;
        private HiddenContent hiddenContent = null;

        public Bitmap getMainBitmap() {
            return mainBitmap;
        }

        public Content setMainBitmap(Bitmap mainBitmap) {
            this.mainBitmap = mainBitmap;
            return this;
        }

        public Bitmap getHiddenBitmap() {
            return hiddenBitmap;
        }

        public Content setHiddenBitmap(Bitmap hiddenBitmap) {
            this.hiddenBitmap = hiddenBitmap;
            return this;
        }

        public HiddenContent getHiddenContent() {
            return hiddenContent;
        }

        public Content setHiddenContent(HiddenContent hiddenContent) {
            this.hiddenContent = hiddenContent;
            return this;
        }
    }
    private Map<File, Content> contentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(
                Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_decode);

        tabHost = (TabHost) findViewById(R.id.tabHost);
        mainPicImageView = (ImageView) findViewById(R.id.mainPicImageView);
        hiddenPicImageView = (ImageView) findViewById(R.id.hiddenPicImageView);
        textView = (TextView) findViewById(R.id.textView);
        previousImageButton = (ImageButton) findViewById(R.id.previousImageButton);
        nextImageButton = (ImageButton) findViewById(R.id.nextImageButton);

        tabHost.setup();
        tabHost.addTab(
                tabHost.newTabSpec("Main")
                .setContent(R.id.mainPicImageView)
                .setIndicator("Main"));
        tabHost.addTab(
                tabHost.newTabSpec("Hidden")
                .setContent(R.id.hiddenPicImageView)
                .setIndicator("Hidden"));
        tabHost.addTab(
                tabHost.newTabSpec("Meta")
                .setContent(R.id.textView)
                .setIndicator("Meta"));
        tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);


        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            if(tv.getText().equals("Main")) {
                tv.setTextColor(getColor(android.R.color.holo_green_light));
            }else{
                tv.setTextColor(getColor(android.R.color.holo_orange_dark));
            }
        }

        tabHost.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );


        tabHost.setOnTabChangedListener(this);

        taskHandlerThread = new HandlerThread("DecodeActivityTaskthread");
        taskHandlerThread.setDaemon(true);
        taskHandlerThread.start();
        taskHandler = new Handler(taskHandlerThread.getLooper());

        uiHandler = new Handler(this.getMainLooper());
        uiHandler.post(this);
    }

    private void findImages(){
        File galleryFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File[] galleryPictures = galleryFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(".png");
            }
        });

        if(galleryPictures == null || galleryPictures.length == 0){
            finish();
        }
        for(File file : galleryPictures){
            imageList.add(file);
        }
    }

    private void setMetadata(){
        HiddenContent hiddenContent = contentMap.get(imageList.getCurrent()).getHiddenContent();
        String metadata = "No metadata available";
        if(hiddenContent!=null && hiddenContent.getMetadata() != null) {
            metadata =
                    "Marker: " + hiddenContent.getMetadata().getText() + "\n" +
                    "Date: " + new Date(hiddenContent.getMetadata().getDate()).toString() + "\n" +
                    "GPS: " + hiddenContent.getMetadata().getGps();
        }
        textView.setText(metadata);
    }

    private void switchView(File file){
        if(!contentMap.containsKey(file)) {
            contentMap.put(file, new Content().setMainBitmap(BitmapFactory.decodeFile(file.getAbsolutePath())));
        }
        mainPicImageView.setImageBitmap(contentMap.get(file).getMainBitmap());

        if(contentMap.get(file).getHiddenBitmap() == null) {
            Drawable drawable = getDrawable(R.drawable.ic_lock_open_black_24dp);
            drawable.setTint(getColor(android.R.color.holo_orange_dark));
            hiddenPicImageView.setImageDrawable(drawable);
            textView.setText("");

            tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(false);
        }else{
            hiddenPicImageView.setImageBitmap(contentMap.get(file).getHiddenBitmap());
            setMetadata();
            tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(true);
        }
    }

    @Override
    protected void onPermissionsGranted() {
        findImages();
        if(imageList.size()>0) {
            switchView(imageList.getCurrent());
        }

    }

    @Override
    protected void onPermissionsDenied() {
        finish();
    }

    private void setDecodeFailure(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = getDrawable(R.drawable.ic_broken_image_black_24dp);
                drawable.setTint(getColor(android.R.color.holo_red_light));
                hiddenPicImageView.setImageDrawable(drawable);

                hiddenPicImageView.clearAnimation();
                tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(true);
                tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(true);

                previousImageButton.setEnabled(true);
                nextImageButton.setEnabled(true);
            }
        });
    }

    private void setHiddenContent(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                hiddenPicImageView.setImageBitmap(contentMap.get(imageList.getCurrent()).getHiddenBitmap());
                setMetadata();

                hiddenPicImageView.clearAnimation();
                tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(true);
                tabHost.getTabWidget().getChildTabViewAt(2).setEnabled(true);

                previousImageButton.setEnabled(true);
                nextImageButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onTabChanged(String tabId) {
        Log.d(TAG, "TAB_CHANGED: "+tabId);
        final Content content = contentMap.get(imageList.getCurrent());

        if(tabId.equals("Hidden") && content.getHiddenBitmap() == null){
            decode(content);
        }else{
            hiddenPicImageView.clearAnimation();
        }
    }

    private void decode(final Content content){
        hiddenPicImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_decode_progress));
        tabHost.getTabWidget().getChildTabViewAt(0).setEnabled(false);

        previousImageButton.setEnabled(false);
        nextImageButton.setEnabled(false);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                SteganographicBitmap steganographicBitmap = new SteganographicBitmap(
                        content.getMainBitmap(),
                        getIntent().getStringExtra("pl.edu.pw.mmajews2.photophoto.encryption.password"));
                content.setHiddenContent(steganographicBitmap.readHiddenContent());
                if(content.getHiddenContent() == null){
                    setDecodeFailure();
                }else {

                    try {
                        content.setHiddenBitmap(BitmapFactory.decodeByteArray(content.getHiddenContent().getPicture(), 0, content.getHiddenContent().getPicture().length));
                        setHiddenContent();
                    } catch (Exception e) {
                        Log.e(TAG, "Hidden bitmap decoding failure", e);
                        setDecodeFailure();
                    }
                }

            }
        };

        taskHandler.post(task);
    }

    public void onNextPicture(View view){
        final Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.anim_decode_right_out);
        final Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.anim_decode_right_in);
        tabHost.setCurrentTab(0);
        mainPicImageView.startAnimation(animationOut);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchView(imageList.getNext());
                mainPicImageView.startAnimation(animationIn);
            }
        }, 300);
    }

    public void onPreviousPicture(View view){
        final Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.anim_decode_left_out);
        final Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.anim_decode_left_in);
        tabHost.setCurrentTab(0);
        mainPicImageView.startAnimation(animationOut);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switchView(imageList.getPrevious());
                mainPicImageView.startAnimation(animationIn);
            }
        }, 300);
    }


}
