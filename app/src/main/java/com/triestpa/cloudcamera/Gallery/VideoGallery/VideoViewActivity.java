package com.triestpa.cloudcamera.Gallery.VideoGallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import com.triestpa.cloudcamera.R;

public class VideoViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        VideoView videoView = (VideoView)findViewById(R.id.video_view);
        String vidAddress = "http://files.parsetfss.com/d0bdb5f9-4a42-4f6d-b4c6-a1a4ffbc8928/tfss-bdb99958-6593-4da0-b573-571a78bd3a22-video.mp4";
        Uri vidUri = Uri.parse(vidAddress);
        videoView.setVideoURI(vidUri);
        videoView.start();
    }
}
