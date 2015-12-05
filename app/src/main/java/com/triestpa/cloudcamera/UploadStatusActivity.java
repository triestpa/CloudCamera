package com.triestpa.cloudcamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.triestpa.cloudcamera.Utilities.Upload;
import com.triestpa.cloudcamera.Utilities.UploadManager;

import java.util.ArrayList;

public class UploadStatusActivity extends AppCompatActivity {
    final static String TAG = UploadStatusActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_status);

        ArrayList<Upload> uploads = UploadManager.getInstance().getUploads();

        RecyclerView uploadGrid = (RecyclerView) findViewById(R.id.upload_grid);
        uploadGrid.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        uploadGrid.setLayoutManager(layoutManager);

        UploadGridAdapter uploadAdapter = new UploadGridAdapter(uploads);
        uploadGrid.setAdapter(uploadAdapter);
    }
}
