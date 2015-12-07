package com.triestpa.cloudcamera.Gallery.VideoGallery;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.R;

import java.util.ArrayList;
import java.util.List;

public class VideoGridAdapter extends RecyclerView.Adapter<VideoGridAdapter.ImageViewHolder> {
    final static String TAG = VideoGridAdapter.class.getName();
    private ArrayList<Video> mVideos;
    private VideoGridFragment mFragment;
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

    public VideoGridAdapter(List<Video> videos, int imgDimens, VideoGridFragment fragment) {
        mVideos = (ArrayList<Video>) videos;
        this.imgDimens = imgDimens;
        this.mFragment = fragment;

        Picasso.Builder picassoBuilder = new Picasso.Builder(fragment.getContext());
        picassoInstance = picassoBuilder.build();
    }

    public void setData(ArrayList<Video> videos) {
        mVideos = videos;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public VideoGridAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_video_grid, parent, false);
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

        final Video thisVideo = mVideos.get(position);

        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.playVideo(thisVideo);
            }
        });

        picassoInstance.load(thisVideo.getThumbnail().getUrl()).resize(imgDimens, imgDimens).centerCrop().into(holder.mImage);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVideos.size();
    }
}