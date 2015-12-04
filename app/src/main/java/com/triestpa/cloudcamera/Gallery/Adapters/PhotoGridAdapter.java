package com.triestpa.cloudcamera.Gallery.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.triestpa.cloudcamera.Gallery.Fragments.PhotoGridFragment;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoGridAdapter extends RecyclerView.Adapter<PhotoGridAdapter.ImageViewHolder> {
    final static String TAG = PhotoGridAdapter.class.getName();
    private ArrayList<Picture> mPhotos;
    private PhotoGridFragment mFragment;
    private int imgDimens;
    private Picasso picassoInstance;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public View mLayout;
        public ImageView mImage;


        public ImageViewHolder(View v) {
            super(v);
            mLayout = v;
            mImage = (ImageView) v.findViewById(R.id.gallery_image);
        }
    }

    public PhotoGridAdapter(List<Picture> photos, int imgDimens, PhotoGridFragment fragment) {
        mPhotos = (ArrayList<Picture>) photos;
        this.imgDimens = imgDimens;
        this.mFragment = (PhotoGridFragment) fragment;

        Picasso.Builder picassoBuilder = new Picasso.Builder(fragment.getContext());
        picassoInstance = picassoBuilder.build();
    }

    public void setData(ArrayList<Picture> pictures) {
        mPhotos = pictures;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PhotoGridAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_photo_grid, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.width = imgDimens;
        layoutParams.height = imgDimens;
        v.setLayoutParams(layoutParams);

        ImageViewHolder vh = new ImageViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {

        final Picture thisPic = mPhotos.get(position);

        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.showLargePhoto(v, thisPic.getPhoto().getUrl(), thisPic.getThumbnail().getUrl());
            }
        });

        picassoInstance.load(thisPic.getThumbnail().getUrl()).resize(imgDimens, imgDimens).memoryPolicy(MemoryPolicy.NO_CACHE).centerCrop().placeholder(R.drawable.loading).into(holder.mImage);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPhotos.size();
    }
}