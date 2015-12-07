package com.triestpa.cloudcamera.Gallery.VideoGallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
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

        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        String videoUrl = intent.getStringExtra(VIDEO_URL);
        Uri vidUri = Uri.parse(videoUrl);

        VideoView videoView = (VideoView)findViewById(R.id.video_view);
        videoView.setVideoURI(vidUri);

        MediaController videoControl = new MediaController(this);
        videoControl.setAnchorView(videoView);
        videoView.setMediaController(videoControl);

        videoView.start();
    }
}
