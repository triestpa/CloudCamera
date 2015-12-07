package com.triestpa.cloudcamera.Gallery.VideoGallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

public class VideoViewActivity extends AppCompatActivity {
    private final String TAG = VideoViewActivity.class.getName();
    public final static String VIDEO_ID = "VIDEO_ID";
    public final static String VIDEO_URL = "VIDEO_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        Intent intent = getIntent();
        final String videoID = intent.getStringExtra(VIDEO_ID);
        final String videoUrl = intent.getStringExtra(VIDEO_URL);
        Uri vidUri = Uri.parse(videoUrl);


        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteVideo(videoID);
            }
        });

        VideoView videoView = (VideoView)findViewById(R.id.video_view);
        videoView.setVideoURI(vidUri);

        MediaController videoControl = new MediaController(this);
        videoControl.setAnchorView(videoView);
        videoView.setMediaController(videoControl);

        videoView.start();
    }


    public void deleteVideo(String videoID) {
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.getInBackground(videoID, new GetCallback<Video>() {
            public void done(Video video, ParseException e) {
                if (e == null) {
                    video.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                SystemUtilities.showToastMessage("Video Deleted");
                                VideoViewActivity.this.onBackPressed();
                            }
                            else {
                                Log.e(TAG, e.getMessage());
                                SystemUtilities.showToastMessage("Error Deleting File: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    Log.e(TAG, e.getMessage());
                    SystemUtilities.showToastMessage("Error Deleting File: " + e.getMessage());
                }
            }
        });
    }
}
