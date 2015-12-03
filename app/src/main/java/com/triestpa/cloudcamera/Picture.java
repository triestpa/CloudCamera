package com.triestpa.cloudcamera;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Picture")
public class Picture extends ParseObject {
    public ParseFile getPhoto() {
        return getParseFile("fullSizePhoto");
    }
    public void setPhoto(ParseFile photo) {
        put("fullSizePhoto", photo);
    }

    public ParseFile getThumbnail() {
        return getParseFile("photoThumbnail");
    }
}
