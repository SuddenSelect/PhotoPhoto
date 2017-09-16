package pl.edu.pw.mmajews2.photophoto.manipulation;

import java.io.Serializable;

import pl.edu.pw.mmajews2.photophoto.taking.tasks.CameraTaskContext;

/**
 * Created by Maciej Majewski on 2016-05-07.
 */
public class HiddenMetadata implements Serializable {
    private static final long serialVersionUID = 11110011L;

    private String text;
    private Long date = null;
    private String gps = null;

    public HiddenMetadata(CameraTaskContext context){
        text = context.getMetadataText();
        if(context.isMetadataDateEnabled()) {
            date = System.currentTimeMillis();
        }
        if(context.isMetadataGPSEnabled()) {
            gps = "TODO some string coordinates";
        }
    }

    public String getText() {
        return text;
    }

    public long getDate() {
        return date;
    }

    public String getGps() {
        return gps;
    }

}
