package com.triestpa.cloudcamera.GalleryScreen;

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

/**
 * Video View Activity: Stream a video to the device screen
 */
public class VideoViewActivity extends AppCompatActivity {

    // Constants
    private final String TAG = VideoViewActivity.class.getName();
    public final static String VIDEO_ID = "VIDEO_ID";
    public final static String VIDEO_URL = "VIDEO_URL";

    // Video data
    private String mVideoId, mVideoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        // Get video data from intent
        Intent intent = getIntent();
        mVideoId = intent.getStringExtra(VIDEO_ID);
        mVideoUrl = intent.getStringExtra(VIDEO_URL);

        // Set back arrow button to emulate physical back button
        ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Setup delete video button
        ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtilities.buildDialog(VideoViewActivity.this, "Delete Video From Cloud?", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (SystemUtilities.isOnline(VideoViewActivity.this)) {
                            deleteVideo();// Delete file if device is online
                        }
                        else {
                            SystemUtilities.reportError(TAG, "Error Deleting Video: Device is Offline");
                        }
                    }
                }).show();
            }
        });

        // Setup download photo button
        ImageButton downloadButton = (ImageButton) findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirm dialog
                SystemUtilities.buildDialog(VideoViewActivity.this, "Download Video From Cloud?", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (SystemUtilities.isOnline(VideoViewActivity.this)) {
                            // Download file if device is online
                            SystemUtilities.downloadFile(mVideoUrl, mVideoId, SystemUtilities.MEDIA_TYPE_VIDEO);
                        }
                        else {
                            SystemUtilities.reportError(TAG, "Error Downloading Video: Device is Offline");
                        }
                    }
                }).show();
            }
        });

        // Setup video view
        VideoView videoView = (VideoView) findViewById(R.id.video_view);
        Uri vidUri = Uri.parse(mVideoUrl);
        videoView.setVideoURI(vidUri);

        // Add system UI to video view
        MediaController videoControl = new MediaController(this);
        videoControl.setAnchorView(videoView);
        videoView.setMediaController(videoControl);

        videoView.start();
    }

    // Delete the video from Parse
    private void deleteVideo() {
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);

        // No need to use bandwidth to find photo, it must be cached
        query.fromLocalDatastore();

        // Find video by id
        query.getInBackground(mVideoId, new GetCallback<Video>() {
            public void done(final Video video, ParseException e) {
                if (e == null) {
                    // Delete the video
                    video.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                video.unpinInBackground();
                                Toast.makeText(CloudCameraApplication.getAppContext(), "Video Deleted", Toast.LENGTH_SHORT).show();
                                VideoViewActivity.this.onBackPressed(); // Return to gallery
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
