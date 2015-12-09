package com.triestpa.cloudcamera.Model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * ParseObject extension for Picture objects
 */
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

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setParseUser(ParseUser user)
    {
        put("user", user);
    }
}
