package com.triestpa.cloudcamera.Gallery;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

import java.util.ArrayList;
import java.util.List;

public class VideoGridAdapter extends RecyclerView.Adapter<VideoGridAdapter.ImageViewHolder> {
    final static String TAG = VideoGridAdapter.class.getName();
    private ArrayList<Video> mVideos;
    private GalleryGridFragment mFragment;
    private int imgDimens;
    private int imgSmallDimens;
    private Picasso picassoInstance;

    private Animation.AnimationListener resizeListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            notifyDataSetChanged();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public View mLayout;
        public ImageView mImage;


        public ImageViewHolder(View v) {
            super(v);
            mLayout = v;
            mImage = (ImageView) v.findViewById(R.id.gallery_image);
        }
    }

    public VideoGridAdapter(List<Video> videos, int imgDimens, GalleryGridFragment fragment) {
        mVideos = (ArrayList<Video>) videos;
        this.imgDimens = imgDimens;
        this.mFragment = fragment;
        this.imgSmallDimens = (int) Math.floor((double) imgDimens * .75);

        Picasso.Builder picassoBuilder = new Picasso.Builder(fragment.getContext());
        picassoBuilder.downloader(new OkHttpDownloader(mFragment.getContext()));
        picassoInstance = picassoBuilder.build();
    }

    public void setData(List<Video> videos) {
        mVideos.clear();
        mVideos.addAll(videos);
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
    public void onBindViewHolder(final ImageViewHolder holder, int position) {

        final Video thisVideo = mVideos.get(position);

        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.playVideo(thisVideo);
            }
        });

        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment.numSelected == 0) {
                    mFragment.playVideo(thisVideo);
                } else {
                    if (mFragment.toggleItemSelected(thisVideo)) {
                        SystemUtilities.zoomView(holder.mImage, imgDimens, imgSmallDimens, resizeListener);
                    } else {
                        SystemUtilities.zoomView(holder.mImage, imgSmallDimens, imgDimens, resizeListener);
                    }
                }
            }
        });

        holder.mImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mFragment.toggleItemSelected(thisVideo)) {
                    SystemUtilities.zoomView(holder.mImage, imgDimens, imgSmallDimens, resizeListener);
                } else {
                    SystemUtilities.zoomView(holder.mImage, imgSmallDimens, imgDimens, resizeListener);
                }
                return false;
            }
        });

        ViewGroup.LayoutParams imageParams = holder.mImage.getLayoutParams();
        if (mFragment.mGridSelectionMap.get(thisVideo.getObjectId())) {
            imageParams.height = imgSmallDimens;
            imageParams.width = imgSmallDimens;

        } else {
            imageParams.height = imgDimens;
            imageParams.width = imgDimens;
        }

        holder.mImage.setLayoutParams(imageParams);


        RequestCreator thisPictureRequest = picassoInstance.load(thisVideo.getThumbnail().getUrl()).resize(imgDimens, imgDimens).centerCrop();
        if (SystemUtilities.isOnlineResult) {
            thisPictureRequest.into(holder.mImage);
        }
        else {
            thisPictureRequest.networkPolicy(NetworkPolicy.OFFLINE).into(holder.mImage);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVideos.size();
    }

}