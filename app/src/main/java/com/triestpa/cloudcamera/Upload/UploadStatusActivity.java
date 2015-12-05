package com.triestpa.cloudcamera.Upload;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.triestpa.cloudcamera.R;

import java.util.ArrayList;

public class UploadStatusActivity extends AppCompatActivity {
    final static String TAG = UploadStatusActivity.class.getName();

    private RecyclerView mUploadGrid;
    private UploadGridAdapter mAdapter;
    private boolean mRunning;

    Handler mHandler = new Handler();

    Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            // check if still in focus
            if (!mRunning) return;

            mAdapter.notifyDataSetChanged();

            // schedule next run
            mHandler.postDelayed(this, 500); // set time here to refresh views
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_status);

        ArrayList<Upload> uploads = UploadManager.getInstance().getUploads();

        mUploadGrid = (RecyclerView) findViewById(R.id.upload_grid);
        mUploadGrid.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        mUploadGrid.setLayoutManager(layoutManager);

        mAdapter = new UploadGridAdapter(uploads, this);
        mUploadGrid.setAdapter(mAdapter);

        mRunning = true;
        // start first run by hand
        mHandler.post(mUpdater);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRunning= false;
    }
}
