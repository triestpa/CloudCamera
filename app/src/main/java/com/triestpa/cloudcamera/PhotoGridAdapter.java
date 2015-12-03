package com.triestpa.cloudcamera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.ViewHolder> {
    final String TAG = PhotoGridAdapter.class.getName();
    private ArrayList<Picture> mPhotos;
    private Context mContext;
    private int imgDimens;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
            mImageView = (ImageView) v.findViewById(R.id.gallery_image);
        }
    }

    public PhotoGridAdapter(List<Picture> photos, Context context) {
        mPhotos = (ArrayList<Picture>) photos;
        mContext = context;

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        imgDimens = metrics.widthPixels / 2;
    }

    public void setData(ArrayList<Picture> pictures) {
        mPhotos = pictures;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PhotoGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_photo_grid, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.width = imgDimens;
        layoutParams.height = imgDimens;
        v.setLayoutParams(layoutParams);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picasso.with(holder.mImageView.getContext()).load(mPhotos.get(position).getThumbnail().getUrl()).resize(imgDimens, imgDimens).centerCrop().placeholder(R.drawable.loading).into(holder.mImageView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPhotos.size();
    }
}