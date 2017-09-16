package pl.edu.pw.mmajews2.photophoto.manipulation;

import java.io.Serializable;

/**
 * Created by Maciej Majewski on 2016-05-07.
 */
public class HiddenContent implements Serializable {
    private static final long serialVersionUID = 11110000L;

    private HiddenMetadata metadata = null;
    private byte[] picture = null;

    public HiddenMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(HiddenMetadata metadata) {
        this.metadata = metadata;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }
}
