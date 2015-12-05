package com.triestpa.cloudcamera;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.triestpa.cloudcamera.Utilities.Upload;

import java.util.ArrayList;
import java.util.List;

public class UploadGridAdapter extends RecyclerView.Adapter<UploadGridAdapter.UploadViewHolder> {
    private ArrayList<Upload> mUploads;

    public static class UploadViewHolder extends RecyclerView.ViewHolder {
        public View mLayout;
        public TextView mStatusText;


        public UploadViewHolder(View v) {
            super(v);
            mLayout = v;
            mStatusText = (TextView) v.findViewById(R.id.upload_status);
        }
    }

    public UploadGridAdapter(List<Upload> uploads) {
        mUploads = (ArrayList<Upload>) uploads;
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
    public void onBindViewHolder(UploadViewHolder holder, int position) {

        final Upload thisUpload = mUploads.get(position);
        holder.mStatusText.setText(thisUpload.getProgress() + "%");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mUploads.size();
    }
}
