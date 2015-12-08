package com.triestpa.cloudcamera.Gallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.triestpa.cloudcamera.CloudCameraApplication;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

public class VideoViewActivity extends AppCompatActivity {
    private final String TAG = VideoViewActivity.class.getName();
    public final static String VIDEO_ID = "VIDEO_ID";
    public final static String VIDEO_URL = "VIDEO_URL";

    private String mVideoId, mVideoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        Intent intent = getIntent();
        mVideoId = intent.getStringExtra(VIDEO_ID);
        mVideoUrl = intent.getStringExtra(VIDEO_URL);

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
                SystemUtilities.buildDialog(VideoViewActivity.this, "Delete Video From Cloud?", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (SystemUtilities.isOnline(VideoViewActivity.this)) {
                            deleteVideo();
                        }
                        else {
                            SystemUtilities.reportError(TAG, "Error Deleting Video: Device is Offline");
                        }
                    }
                }).show();
            }
        });

        ImageButton downloadButton = (ImageButton) findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtilities.buildDialog(VideoViewActivity.this, "Download Video From Cloud?", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (SystemUtilities.isOnline(VideoViewActivity.this)) {
                            SystemUtilities.downloadFile(mVideoUrl, mVideoId, SystemUtilities.MEDIA_TYPE_VIDEO);
                        }
                        else {
                            SystemUtilities.reportError(TAG, "Error Downloading Video: Device is Offline");
                        }
                    }
                }).show();
            }
        });


        VideoView videoView = (VideoView) findViewById(R.id.video_view);
        Uri vidUri = Uri.parse(mVideoUrl);
        videoView.setVideoURI(vidUri);
        MediaController videoControl = new MediaController(this);
        videoControl.setAnchorView(videoView);
        videoView.setMediaController(videoControl);
        videoView.start();
    }

    private void deleteVideo() {
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.getInBackground(mVideoId, new GetCallback<Video>() {
            public void done(Video video, ParseException e) {
                if (e == null) {
                    video.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(CloudCameraApplication.getAppContext(), "Video Deleted", Toast.LENGTH_SHORT).show();
                                VideoViewActivity.this.onBackPressed();
                            } else {
                                SystemUtilities.reportError(TAG, "Error Deleting File: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    Log.e(TAG, e.getMessage());
                    SystemUtilities.reportError(TAG, "Error Deleting File: " + e.getMessage());
                }
            }
        });
    }
}
