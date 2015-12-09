package com.triestpa.cloudcamera.CameraScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.parse.ParseUser;
import com.triestpa.cloudcamera.GalleryScreen.GalleryActivity;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.UploadsScreen.UploadGridActivity;
import com.triestpa.cloudcamera.LoginScreen.LoginActivity;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

/**
 * CameraActivity: Show a camera preview with different options and ability to capture media
 */
public class CameraActivity extends AppCompatActivity {
    protected final static String TAG = CameraActivity.class.getName();

    // Set constant values
    private final static int MODE_PICTURE = 0;
    private final static int MODE_VIDEO = 1;
    private int mMode = MODE_PICTURE;

    // UI references
    private FloatingActionButton mFlashButton, mCaptureButton, mSwapButton, mModeButton, mGalleryButton, mUploadViewButton;
    private RelativeLayout mVideoIndicator;
    private TextView mVideoTime;

    // Camera manager to handle camera configuration and customization
    private CameraManager mCameraManager;

    // Video recording UI indicator values
    private Boolean mRecording;
    private int mRecordingSeconds;
    private int mRecordingMinutes;
    private Handler mRecordingTimeHandler = new Handler();
    private Runnable mRecordingTimeUpdater = new Runnable() {
        @Override
        public void run() {
            // check if still recording
            if (!mRecording) return;

            if (++mRecordingSeconds >= 60) {
                mRecordingSeconds = 0;
                ++mRecordingMinutes;
            }

            String newTime;
            if (mRecordingSeconds >= 10) {
                newTime = mRecordingMinutes + ":" + mRecordingSeconds;
            } else {
                newTime = mRecordingMinutes + ":0" + mRecordingSeconds;
            }

            mVideoTime.setText(newTime);

            // update again in a second
            mRecordingTimeHandler.postDelayed(this, 1000);
        }
    };

    /**
     * ----- Activity Lifecycle Events -----
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch Login activity if no active user
        if (ParseUser.getCurrentUser() == null) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

        setContentView(R.layout.activity_camera);
        mCameraManager = new CameraManager();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reinitialize the camera and preview on resume
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraManager.cameraInit(mCameraManager.mCameraID, this, preview);

        // Rebind UI elements on resume
        mFlashButton = (FloatingActionButton) findViewById(R.id.button_flash);
        mCaptureButton = (FloatingActionButton) findViewById(R.id.button_capture);
        mSwapButton = (FloatingActionButton) findViewById(R.id.button_swap);
        mModeButton = (FloatingActionButton) findViewById(R.id.button_mode);
        mGalleryButton = (FloatingActionButton) findViewById(R.id.button_gallery);
        mUploadViewButton = (FloatingActionButton) findViewById(R.id.button_upload_view);

        mVideoIndicator = (RelativeLayout) findViewById(R.id.recording_indicator);
        mVideoTime = (TextView) findViewById(R.id.recording_time);

        // Set UI button listeners
        setCaptureButton();
        setCameraSwapButton();
        setFlashButton();
        setModeButton();
        setGalleryButton();
        setUploadViewButton();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // End the camera and view on pause
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraManager.stopPreview(preview);
        mCameraManager.releaseMediaRecorder(); // if you are using MediaRecorder, release it first
        mCameraManager.releaseCamera();

        // Release UI elements on pause
        mFlashButton = null;
        mCaptureButton = null;
        mSwapButton = null;
        mModeButton = null;
        mGalleryButton = null;
        mUploadViewButton = null;
        mVideoIndicator = null;
        mVideoTime = null;
    }

    /**
     * ----- Set UI Listeners -----
     */

    // Set button to initiate media capture from camera
    private void setCaptureButton() {
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMode == MODE_PICTURE) {
                    mCameraManager.takePicture();
                } else {
                    if (mCameraManager.toggleRecording()) {
                        // Lock orientation while recording
                        SystemUtilities.lockOrientation(CameraActivity.this);

                        // Setup recording UI indicator
                        mRecordingSeconds = 0;
                        mRecordingMinutes = 0;
                        mRecording = true;
                        mVideoIndicator.setVisibility(View.VISIBLE);
                        mRecordingTimeHandler.post(mRecordingTimeUpdater);

                        mCaptureButton.setImageResource(R.drawable.ic_stop_white_24dp);
                    } else {
                        // Reset UI once done recording
                        mRecording = false;
                        mVideoIndicator.setVisibility(View.INVISIBLE);
                        mCaptureButton.setImageResource(R.drawable.ic_videocam_white_24dp);
                        SystemUtilities.unlockOrientation(CameraActivity.this);
                    }
                }
            }

        });
    }

    // Toggle camera mode between video and photo
    private void setModeButton() {
        mModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMode == MODE_PICTURE) {
                    // Switch to video recording mode
                    mMode = MODE_VIDEO;
                    mModeButton.setImageResource(R.drawable.ic_switch_camera_white_24dp);
                    mModeButton.setLabelText(getString(R.string.camera_mode_photo));
                    mCaptureButton.setImageResource(R.drawable.ic_videocam_white_24dp);
                } else {
                    // Switch to picture mode
                    mMode = MODE_PICTURE;
                    mModeButton.setImageResource(R.drawable.ic_switch_video_white_24dp);
                    mModeButton.setLabelText(getString(R.string.camera_mode_video));
                    mCaptureButton.setImageResource(R.drawable.ic_photo_camera_white_24dp);
                }
            }
        });
    }

    //Set a button to flip the camera to front-facing/back-facing
    private void setCameraSwapButton() {
        // Add a listener to the Capture button
        mSwapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                if (mCameraManager.swapCamera(preview, CameraActivity.this)) {

                    // Set UI to show that rear camera is in use
                    mSwapButton.setLabelText(getString(R.string.camera_front));
                    mSwapButton.setImageResource(R.drawable.ic_camera_front_white_24dp);
                    mFlashButton.setVisibility(View.VISIBLE);
                } else {
                    // Set UI to show that front camera is in use
                    mSwapButton.setLabelText(getString(R.string.camera_rear));
                    mSwapButton.setImageResource(R.drawable.ic_camera_rear_white_24dp);

                    // Disable flash while using front camera
                    mFlashButton.setVisibility(View.GONE);
                    mFlashButton.setImageResource(R.drawable.ic_flash_on_white_24dp);
                    mFlashButton.setLabelText(getString(R.string.camera_flash_off));
                }
            }
        });
    }

    //Toggle the camera flash
    private void setFlashButton() {
        // Add a listener to the Capture button
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraManager.toggleFlash()) {
                    // Set UI to show that flash is on
                    mFlashButton.setImageResource(R.drawable.ic_flash_off_white_24dp);
                    mFlashButton.setLabelText(getString(R.string.camera_flash_off));
                } else {
                    // Set UI to show that flash is off
                    mFlashButton.setImageResource(R.drawable.ic_flash_on_white_24dp);
                    mFlashButton.setLabelText(getString(R.string.camera_flash_on));
                }
            }
        });
    }

    // Set button to open media gallery
    private void setGalleryButton() {
        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open gallery activity
                Intent galleryIntent = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(galleryIntent);
            }
        });
    }

    // Set button to open upload view
    private void setUploadViewButton() {
        mUploadViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open upload activity
                Intent uploadViewIntent = new Intent(CameraActivity.this, UploadGridActivity.class);
                startActivity(uploadViewIntent);
            }
        });
    }
}
