package com.triestpa.cloudcamera.Gallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GalleryGridFragment extends Fragment {
    private final static String TAG = GalleryGridFragment.class.getName();
    final static String ARGUMENT_TYPE = "TYPE";
    final static int TYPE_PHOTO_GRID = 0;
    final static int TYPE_VIDEO_GRID = 1;

    private int mType;

    private RecyclerView mMediaGrid;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected HashMap<String, Boolean> mGridSelectionMap;
    private ArrayList<ParseObject> mDisplayedMedia;
    protected int numSelected;
    private Snackbar mSelectionSnackbar;

    private View.OnClickListener snackbackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SystemUtilities.buildDialog(getActivity(), "Delete From Cloud?", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    deleteObjects(getAllSelected());
                }
            }).show();
        }
    };

    public GalleryGridFragment() {
    }

    public static GalleryGridFragment newInstance(int type) {
        GalleryGridFragment fragment = new GalleryGridFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(ARGUMENT_TYPE);
        mGridSelectionMap = new HashMap<>();
        mDisplayedMedia = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        int imageDimensions = metrics.widthPixels / 3;

        // Inflate the layout for this fragment
        View v;
        if (mType == TYPE_PHOTO_GRID) {
            v = inflater.inflate(R.layout.fragment_photo_grid, container, false);
            mAdapter = new PhotoGridAdapter(new ArrayList<Picture>(), imageDimensions, this);
        }
        else {
            v = inflater.inflate(R.layout.fragment_video_grid, container, false);
            mAdapter = new VideoGridAdapter(new ArrayList<Video>(), imageDimensions, this);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.grid_swipe_refresh_layout);
        mMediaGrid = (RecyclerView) v.findViewById(R.id.image_grid);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mMediaGrid.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mMediaGrid.setLayoutManager(layoutManager);

        mMediaGrid.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwipeRefreshLayout.setRefreshing(true);
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void refresh() {
        if (mType == TYPE_PHOTO_GRID) {
            refreshPhotos();
        }
        else {
            refreshVideos();
        }
    }

    public void showLargePhoto(View thumbnailView, Picture picture) {
        Intent intent = new Intent(getActivity(), PhotoViewActivity.class);

        Bitmap thumbnailBitmap = ((BitmapDrawable)((ImageView)thumbnailView).getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();

        intent.putExtra(PhotoViewActivity.EXTRA_PHOTO_ID, picture.getObjectId());
        intent.putExtra(PhotoViewActivity.EXTRA_FULLSIZE_URL, picture.getPhoto().getUrl());
        intent.putExtra(PhotoViewActivity.EXTRA_THUMBNAIL_URL, picture.getThumbnail().getUrl());
        intent.putExtra(PhotoViewActivity.EXTRA_THUMBNAIL_BYTES, bitmapdata);

        String transitionName = getString(R.string.transition_picture);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        thumbnailView,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning to
                );

        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }

    void playVideo(Video video) {
        Intent videoIntent = new Intent(getActivity(), VideoViewActivity.class);
        videoIntent.putExtra(VideoViewActivity.VIDEO_ID, video.getObjectId());
        videoIntent.putExtra(VideoViewActivity.VIDEO_URL, video.getVideo().getUrl());
        ActivityCompat.startActivity(getActivity(), videoIntent, null);
    }


    public boolean toggleItemSelected(ParseObject object) {
        boolean isToggled = false;
        if (mGridSelectionMap.get(object.getObjectId())) {
            mGridSelectionMap.put(object.getObjectId(), false);
            --numSelected;

            if (numSelected == 0) {
                mSelectionSnackbar.dismiss();
            }
            else {
                mSelectionSnackbar.setText(numSelected + " Selected");
            }
        }
        else {
            mGridSelectionMap.put(object.getObjectId(), true);
            ++numSelected;
            isToggled = true;

            if (numSelected == 1) {
                mSelectionSnackbar = Snackbar.make(mMediaGrid, "1 Selected", Snackbar.LENGTH_INDEFINITE);
                mSelectionSnackbar.setAction("Delete", snackbackClickListener);
                mSelectionSnackbar.setActionTextColor(getResources().getColor(R.color.md_red_500));
                mSelectionSnackbar.show();
            }
            else {
                mSelectionSnackbar.setText(numSelected + " Selected");
            }
        }
        return isToggled;
    }

    private ArrayList<ParseObject> getAllSelected() {
        ArrayList<ParseObject> selectedObjects = new ArrayList<>();

        for(ParseObject object : mDisplayedMedia) {
            if (mGridSelectionMap.get(object.getObjectId())) {
                selectedObjects.add(object);
            }
        }
        return selectedObjects;
    }

    private void deleteObjects(ArrayList<ParseObject> selectedObjects) {
        Toast.makeText(getActivity(), "Deleting Items", Toast.LENGTH_SHORT).show();
        ParseObject.deleteAllInBackground(selectedObjects, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Deletion Complete", Toast.LENGTH_SHORT).show();
                    if (mType == TYPE_PHOTO_GRID) {
                        refreshPhotos();
                    } else {
                        refreshVideos();
                    }
                } else {
                    String errorReport = "Error Deleting: " + e.getMessage();
                    SystemUtilities.reportError(TAG, errorReport);
                }
            }
        });
    }

    private void refreshPhotos() {
        ParseQuery<Picture> query = ParseQuery.getQuery(Picture.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(1000);
        query.findInBackground(new FindCallback<Picture>() {
            @Override
            public void done(List<Picture> pictures, ParseException e) {
                if (e == null) {
                    if (pictures == null || pictures.isEmpty()) {
                        mSwipeRefreshLayout.setVisibility(View.GONE);
                    } else {
                        mDisplayedMedia.clear();
                        mGridSelectionMap.clear();
                        for (Picture picture : pictures) {
                            mGridSelectionMap.put(picture.getObjectId(), false);
                            mDisplayedMedia.add(picture);
                        }
                        numSelected = 0;

                        ((PhotoGridAdapter) mAdapter).setData((ArrayList<Picture>) pictures);
                        mAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    mSwipeRefreshLayout.setVisibility(View.GONE);
                    SystemUtilities.reportError(TAG, "Error Loading Photos: " + e.getMessage());
                }
            }
        });
    }


    private void refreshVideos() {
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(1000);
        query.findInBackground(new FindCallback<Video>() {
            @Override
            public void done(List<Video> videos, ParseException e) {
                if (e == null) {
                    if (videos == null || videos.isEmpty()) {
                        mSwipeRefreshLayout.setVisibility(View.GONE);
                    } else {
                        mDisplayedMedia.clear();
                        mGridSelectionMap.clear();
                        for (Video video : videos) {
                            mGridSelectionMap.put(video.getObjectId(), false);
                            mDisplayedMedia.add(video);
                        }
                        numSelected = 0;

                        ((VideoGridAdapter) mAdapter).setData((ArrayList<Video>) videos);
                        mAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    mSwipeRefreshLayout.setVisibility(View.GONE);
                    SystemUtilities.reportError(TAG, "Error Loading Videos: " + e.getMessage());
                }
            }
        });
    }
}
