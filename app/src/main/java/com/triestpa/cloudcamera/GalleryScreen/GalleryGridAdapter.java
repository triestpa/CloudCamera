package com.triestpa.cloudcamera.GalleryScreen;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.parse.ParseObject;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

import java.util.ArrayList;

/**
 * Gallery Grid Adapter: RecyclerView Adapter to show a grid of videos or photos
 */
public class GalleryGridAdapter extends RecyclerView.Adapter<GalleryGridAdapter.MediaViewHolder> {
    final static String TAG = GalleryGridAdapter.class.getName();
    private ArrayList<ParseObject> mMedia; // Underlying dataset being displayed in grid
    private GalleryGridFragment mFragment;

    private int imgDimens; // 1/3 of screen width
    private int imgSmallDimens; // 3/12 of screen width

    private Picasso picassoInstance; // For image loading and caching

    // Listener to update dataset when resize animation finishes
    private Animation.AnimationListener resizeListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            notifyDataSetChanged();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    // Grid view holder for media image
    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        public View mLayout;
        public ImageView mImage;


        public MediaViewHolder(View v) {
            super(v);
            mLayout = v;
            mImage = (ImageView) v.findViewById(R.id.gallery_image);
        }
    }

    // Adapter constructor
    public GalleryGridAdapter(ArrayList<ParseObject> videos, int imgDimens, GalleryGridFragment fragment) {
        this.mMedia = videos;
        this.imgDimens = imgDimens;
        this.mFragment = fragment;
        this.imgSmallDimens = (int) Math.floor((double) imgDimens * .75);

        Picasso.Builder picassoBuilder = new Picasso.Builder(fragment.getContext());
        picassoInstance = picassoBuilder.build();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        View view;
        // Inflate view layout besed on fragment type
        if (mFragment.mType == GalleryGridFragment.TYPE_PHOTO_GRID) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_photo_grid, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_video_grid, parent, false);
        }

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = imgDimens;
        layoutParams.height = imgDimens;
        view.setLayoutParams(layoutParams);

        MediaViewHolder vh = new MediaViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MediaViewHolder holder, int position) {

        final ParseObject thisMedium = mMedia.get(position);

        // Set on click listener
        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If selection mode is off...
                if (mFragment.numSelected == 0) {
                    if (mFragment.mType == GalleryGridFragment.TYPE_PHOTO_GRID) {
                        // Show large photo if it is a photo grid
                        mFragment.showLargePhoto(holder.mImage, (Picture) thisMedium);
                    } else {
                        // Play video if it is a video grid
                        mFragment.playVideo((Video) thisMedium);
                    }
                } else {
                    // Toggle selection of the item
                    if (mFragment.toggleItemSelected(thisMedium)) {
                        SystemUtilities.zoomView(holder.mImage, imgDimens, imgSmallDimens, resizeListener);
                    } else {
                        SystemUtilities.zoomView(holder.mImage, imgSmallDimens, imgDimens, resizeListener);
                    }
                }
            }
        });

        // Select item on long click
        holder.mImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mFragment.toggleItemSelected(thisMedium)) {
                    SystemUtilities.zoomView(holder.mImage, imgDimens, imgSmallDimens, resizeListener);
                } else {
                    SystemUtilities.zoomView(holder.mImage, imgSmallDimens, imgDimens, resizeListener);
                }
                return false;
            }
        });

        // Resize image based on whether it is selected
        ViewGroup.LayoutParams imageParams = holder.mImage.getLayoutParams();
        if (mFragment.mGridSelectionMap.get(thisMedium.getObjectId())) {
            imageParams.height = imgSmallDimens;
            imageParams.width = imgSmallDimens;

        } else {
            imageParams.height = imgDimens;
            imageParams.width = imgDimens;
        }
        holder.mImage.setLayoutParams(imageParams);

        // Get thumbnail url
        String thumbnailUrl;
        if (mFragment.mType == GalleryGridFragment.TYPE_PHOTO_GRID) {
            thumbnailUrl = ((Picture) thisMedium).getThumbnail().getUrl();
        } else {
            thumbnailUrl = ((Video) thisMedium).getThumbnail().getUrl();
        }

        // Display thumbnail
        RequestCreator thisPictureRequest = picassoInstance.load(thumbnailUrl).resize(imgDimens, imgDimens).centerCrop();
        if (SystemUtilities.isOnlineResult) {
            thisPictureRequest.into(holder.mImage);
        } else {
            // Only use cache if offline
            thisPictureRequest.networkPolicy(NetworkPolicy.OFFLINE).into(holder.mImage);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mMedia.size();
    }

}