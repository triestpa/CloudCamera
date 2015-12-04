package com.triestpa.cloudcamera.Model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

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

}
