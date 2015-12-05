package com.triestpa.cloudcamera.Gallery.VideoGallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import com.triestpa.cloudcamera.R;

public class VideoViewActivity extends AppCompatActivity {
    private final String TAG = VideoViewActivity.class.getName();
    public final static String VIDEO_URL = "VIDEO_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        VideoView videoView = (VideoView)findViewById(R.id.video_view);

        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra(VIDEO_URL);

        Uri vidUri = Uri.parse(videoUrl);
        videoView.setVideoURI(vidUri);

        MediaController videoControl = new MediaController(this);
        videoControl.setAnchorView(videoView);
        videoView.setMediaController(videoControl);

        videoView.start();
    }
}
