package com.triestpa.cloudcamera.Model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * ParseObject extension for Picture objects
 */
@ParseClassName("Video")
public class Video extends ParseObject {
    public ParseFile getVideo() {
        return getParseFile("video");
    }

    public void setVideo(ParseFile video) {
        put("video", video);
    }

    public ParseFile getThumbnail() {
        return getParseFile("videoThumbnail");
    }

    public void setThumbnail(ParseFile thumbnail) {
        put("videoThumbnail", thumbnail);
    }


    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setParseUser(ParseUser user)
    {
        put("user", user);
    }
}
