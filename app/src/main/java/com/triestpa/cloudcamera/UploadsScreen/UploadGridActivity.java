package com.triestpa.cloudcamera.UploadsScreen;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.triestpa.cloudcamera.Model.Upload;
import com.triestpa.cloudcamera.R;

import java.util.ArrayList;

/**
 * Upload Grid Activity: Show a grid of ongoing and completed uploads for the current app session
 */
public class UploadGridActivity extends AppCompatActivity {
    final static String TAG = UploadGridActivity.class.getName();

    private UploadGridAdapter mAdapter;

    private boolean mRunning; // If the auto-refresh runnable is running
    private Handler mHandler = new Handler();
    private Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            // Check if still in focus
            if (!mRunning) return;

            // Refresh the adapter views
            mAdapter.notifyDataSetChanged();

            // Refresh views again in .25 seconds
            mHandler.postDelayed(this, 250);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_status);

        // Setup action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Setup upload grid
        ArrayList<Upload> uploads = UploadManager.getInstance().getUploads();
        RecyclerView uploadGrid = (RecyclerView) findViewById(R.id.upload_grid);

        if (uploads == null || uploads.isEmpty()) {
            uploadGrid.setVisibility(View.GONE);
        }
        else {
            uploadGrid.setVisibility(View.VISIBLE);
            uploadGrid.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
            uploadGrid.setLayoutManager(layoutManager);

            mAdapter = new UploadGridAdapter(uploads, this);
            uploadGrid.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAdapter != null) {
            // start grid auto-refresh
            mRunning = true;
            mHandler.post(mUpdater);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunning= false; // Stop auto-refresh on pause
    }
}
