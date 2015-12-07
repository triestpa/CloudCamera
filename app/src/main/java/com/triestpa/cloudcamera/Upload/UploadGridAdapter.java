package com.triestpa.cloudcamera.Upload;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.triestpa.cloudcamera.CloudCameraApplication;
import com.triestpa.cloudcamera.R;

import java.util.ArrayList;
import java.util.List;

public class UploadGridAdapter extends RecyclerView.Adapter<UploadGridAdapter.UploadViewHolder> {
    private ArrayList<Upload> mUploads;
    private Context mContext;

    public static class UploadViewHolder extends RecyclerView.ViewHolder {
        public View mLayout;
        public TextView mStatusText;
        public ImageView mMediaImage;

        public UploadViewHolder(View v) {
            super(v);
            mLayout = v;
            mStatusText = (TextView) v.findViewById(R.id.upload_status);
            mMediaImage = (ImageView) v.findViewById(R.id.media_image);
        }
    }

    public UploadGridAdapter(List<Upload> uploads, Context context) {
        mUploads = (ArrayList<Upload>) uploads;
        mContext = context;
    }

    public void setData(ArrayList<Upload> uploads) {
        mUploads = uploads;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UploadGridAdapter.UploadViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_upload_grid, parent, false);
        // set the view's size, margins, paddings and layout parameters

        UploadViewHolder vh = new UploadViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final UploadViewHolder holder, int position) {

        final Upload thisUpload = mUploads.get(position);

        if (thisUpload.getClass() == PhotoUpload.class) {
            holder.mLayout.setBackgroundColor(mContext.getResources().getColor(R.color.md_blue_500));
            holder.mMediaImage.setImageResource(R.drawable.ic_photo_camera_white_24dp);
        }
        else {
            holder.mLayout.setBackgroundColor(mContext.getResources().getColor(R.color.md_green_500));
            holder.mMediaImage.setImageResource(R.drawable.ic_videocam_white_24dp);
        }

        if (thisUpload.isAborted()) {
            holder.mLayout.setBackgroundColor(mContext.getResources().getColor(R.color.md_red_500));
            holder.mStatusText.setText(CloudCameraApplication.getAppContext().getString(R.string.upload_fail));
            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    thisUpload.retryUpload();
                    thisUpload.setAborted(false);
                    holder.mStatusText.setText("Retrying...");
                    holder.mLayout.setOnClickListener(null);
                }
            });
        }
        else if (thisUpload.isCompleted()) {
            holder.mStatusText.setText(CloudCameraApplication.getAppContext().getString(R.string.upload_success));
        }
        else {
            holder.mStatusText.setText(thisUpload.getProgress() + "%");
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mUploads.size();
    }
}
