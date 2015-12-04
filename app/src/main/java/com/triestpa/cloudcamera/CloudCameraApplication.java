package com.triestpa.cloudcamera;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.Model.Video;

public class CloudCameraApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Picture.class);
        ParseObject.registerSubclass(Video.class);
        Parse.initialize(this, "q3IS3BqEI8TLXNxUv05vLlxHfhv7xFICbUdP6xH7", "bFbVdtOaEHH1tcFPLdUWggYzVe6LuCie2bst4SFR");
    }
}
