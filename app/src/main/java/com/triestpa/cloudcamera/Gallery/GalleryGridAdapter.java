package com.triestpa.cloudcamera.Gallery;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.parse.ParseObject;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

import java.util.ArrayList;

/**
 * VideoGridAdapter: RecyclerView Adapter to show a grid of videos
 */
public class GalleryGridAdapter extends RecyclerView.Adapter<GalleryGridAdapter.ImageViewHolder> {
    final static String TAG = GalleryGridAdapter.class.getName();
    private ArrayList<ParseObject> mMedia;
    private GalleryGridFragment mFragment;
    private int imgDimens;
    private int imgSmallDimens;
    private Picasso picassoInstance;

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

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public View mLayout;
        public ImageView mImage;


        public ImageViewHolder(View v) {
            super(v);
            mLayout = v;
            mImage = (ImageView) v.findViewById(R.id.gallery_image);
        }
    }

    public GalleryGridAdapter(ArrayList<ParseObject> videos, int imgDimens, GalleryGridFragment fragment) {
        this.mMedia = videos;
        this.imgDimens = imgDimens;
        this.mFragment = fragment;
        this.imgSmallDimens = (int) Math.floor((double) imgDimens * .75);

        Picasso.Builder picassoBuilder = new Picasso.Builder(fragment.getContext());
        picassoBuilder.downloader(new OkHttpDownloader(mFragment.getContext()));
        picassoInstance = picassoBuilder.build();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GalleryGridAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        View view;
        if (mFragment.mType == GalleryGridFragment.TYPE_PHOTO_GRID) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_photo_grid, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_video_grid, parent, false);
        }

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = imgDimens;
        layoutParams.height = imgDimens;
        view.setLayoutParams(layoutParams);

        ImageViewHolder vh = new ImageViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ImageViewHolder holder, int position) {

        final ParseObject thisMedium = mMedia.get(position);

        holder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment.numSelected == 0) {
                    if (mFragment.mType == GalleryGridFragment.TYPE_PHOTO_GRID) {
                        mFragment.showLargePhoto(holder.mImage, (Picture) thisMedium);
                    } else {
                        mFragment.playVideo((Video) thisMedium);
                    }
                } else {
                    if (mFragment.toggleItemSelected(thisMedium)) {
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
                if (mFragment.toggleItemSelected(thisMedium)) {
                    SystemUtilities.zoomView(holder.mImage, imgDimens, imgSmallDimens, resizeListener);
                } else {
                    SystemUtilities.zoomView(holder.mImage, imgSmallDimens, imgDimens, resizeListener);
                }
                return false;
            }
        });

        ViewGroup.LayoutParams imageParams = holder.mImage.getLayoutParams();
        if (mFragment.mGridSelectionMap.get(thisMedium.getObjectId())) {
            imageParams.height = imgSmallDimens;
            imageParams.width = imgSmallDimens;

        } else {
            imageParams.height = imgDimens;
            imageParams.width = imgDimens;
        }

        holder.mImage.setLayoutParams(imageParams);

        String thumbnailUrl;
        if (mFragment.mType == GalleryGridFragment.TYPE_PHOTO_GRID) {
            thumbnailUrl = ((Picture)thisMedium).getThumbnail().getUrl();
        } else {
            thumbnailUrl = ((Video)thisMedium).getThumbnail().getUrl();
        }

        RequestCreator thisPictureRequest = picassoInstance.load(thumbnailUrl).resize(imgDimens, imgDimens).centerCrop();

        if (SystemUtilities.isOnlineResult) {
            thisPictureRequest.into(holder.mImage);
        } else {
            thisPictureRequest.networkPolicy(NetworkPolicy.OFFLINE).into(holder.mImage);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mMedia.size();
    }

}