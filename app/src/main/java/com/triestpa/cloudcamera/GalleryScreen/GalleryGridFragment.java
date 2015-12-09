package com.triestpa.cloudcamera.GalleryScreen;

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

/**
 * Gallery Grid Fragment: Manages the photo or video gallery grid
 * displayed in the Gallery Activity viewpager.
 */
public class GalleryGridFragment extends Fragment {
    private final static String TAG = GalleryGridFragment.class.getName();

    // Set constants
    final static String ARGUMENT_TYPE = "TYPE";
    final static int TYPE_PHOTO_GRID = 0;
    final static int TYPE_VIDEO_GRID = 1;
    final static String PIN_LABEL_PHOTO = "PHOTOS";
    final static String PIN_LABEL_VIDEO = "VIDEOS";

    // Stores whether fragment is for videos or photos
    protected int mType;

    // UI references
    private RecyclerView mMediaGrid;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Snackbar mSelectionSnackbar;

    // Data-structures to manage grid item selection
    protected HashMap<String, Boolean> mGridSelectionMap;
    private ArrayList<ParseObject> mDisplayedMedia;

    protected int numSelected;

    // Delete selected items from cloud on snackbar button click
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

    /**
     * Fragment initialization and lifecycle methods
     */
    public GalleryGridFragment() {
    }

    public static GalleryGridFragment newInstance(int type) {
        // Store the fragment type
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
        mDisplayedMedia = new ArrayList<ParseObject>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set image dimension to 1/3 of screen size
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        int imageDimensions = metrics.widthPixels / 3;

        // Inflate the layout for this fragment based on type
        View v;
        if (mType == TYPE_PHOTO_GRID) {
            v = inflater.inflate(R.layout.fragment_photo_grid, container, false);
        } else {
            v = inflater.inflate(R.layout.fragment_video_grid, container, false);
        }

        mAdapter = new GalleryGridAdapter(mDisplayedMedia, imageDimensions, this);

        // Bind views
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.grid_swipe_refresh_layout);
        mMediaGrid = (RecyclerView) v.findViewById(R.id.image_grid);

        // Setup media grid
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mMediaGrid.setLayoutManager(layoutManager);
        mMediaGrid.setAdapter(mAdapter);

        // Refresh items on swipe up
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
        refresh(); // Refresh items on resume
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Close the selection snackbar once fragment is navigated away from
        if (!isVisibleToUser && numSelected > 0) {
            clearSelected();
        }
    }

    /**
     * Dataset management methods
     */

    private void refresh() {
        boolean fromCache = false;

        // Load results from cache if not internet connection detected
        if (!SystemUtilities.isOnline(getActivity())) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "No Internet Connection, Loading Cached Results.", Snackbar.LENGTH_SHORT).show();
            fromCache = true;
        }

        mSwipeRefreshLayout.setRefreshing(true);

        if (mType == TYPE_PHOTO_GRID) {
            refreshPhotos(fromCache);
        } else {
            refreshVideos(fromCache);
        }
    }

    // Load photos from parse
    private void refreshPhotos(final boolean fromCache) {
        // Query the current users' photos
        ParseQuery<Picture> query = ParseQuery.getQuery(Picture.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(1000);// Max limit for Parse

        if (fromCache) {
            query.fromLocalDatastore();
        }

        query.findInBackground(new FindCallback<Picture>() {
            @Override
            public void done(final List<Picture> pictures, ParseException e) {
                if (e == null) {
                    // Update grid with results
                    setNewPhotos(pictures);

                    if (!fromCache) {
                        // Replace the previously cached results
                        ParseObject.unpinAllInBackground(PIN_LABEL_PHOTO, new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                ParseObject.pinAllInBackground(PIN_LABEL_PHOTO, pictures);
                            }
                        });
                    }
                } else {
                    mSwipeRefreshLayout.setVisibility(View.GONE);
                    SystemUtilities.reportError(TAG, "Error Loading Photos: " + e.getMessage());
                }
            }
        });
    }

    // Replace grid contents with updated photo list
    private void setNewPhotos(List<Picture> pictures) {
        if (pictures == null || pictures.isEmpty()) {
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            // Replace the previous dataset.
            mDisplayedMedia.clear();
            mGridSelectionMap.clear();
            for (Picture picture : pictures) {
                mGridSelectionMap.put(picture.getObjectId(), false);
                mDisplayedMedia.add(picture);
            }
            numSelected = 0;

            // Update grid with new results
            //((PhotoGridAdapter) mAdapter).setData(pictures);
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);


        }
    }

    // Load videos from parse
    private void refreshVideos(final boolean fromCache) {
        // Query the current users' videos
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(1000);// Max limit for Parse

        if (fromCache) {
            query.fromLocalDatastore();
        }

        query.findInBackground(new FindCallback<Video>() {
                                   @Override
                                   public void done(final List<Video> videos, ParseException e) {
                                       if (e == null) {
                                           setNewVideos(videos);
                                           if (!fromCache) {
                                               // Replace the previously cached results
                                               ParseObject.unpinAllInBackground(PIN_LABEL_VIDEO, new DeleteCallback() {
                                                   @Override
                                                   public void done(ParseException e) {
                                                       ParseObject.pinAllInBackground(PIN_LABEL_VIDEO, videos);
                                                   }
                                               });
                                           }
                                       } else {
                                           mSwipeRefreshLayout.setVisibility(View.GONE);
                                           SystemUtilities.reportError(TAG, "Error Loading Videos: " + e.getMessage());
                                       }
                                   }
                               }

        );
    }

    // Replace grid contents with updated video list
    private void setNewVideos(List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            // Replace the previous dataset.
            mDisplayedMedia.clear();
            mGridSelectionMap.clear();
            for (Video video : videos) {
                mGridSelectionMap.put(video.getObjectId(), false);
                mDisplayedMedia.add(video);
            }
            numSelected = 0;

            // Update grid with new results
            //((VideoGridAdapter) mAdapter).setData(videos);
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Media transiton methods
     */

    // Launch PhotoViewActivity to show fullsized zoomable photo
    public void showLargePhoto(View thumbnailView, Picture picture) {
        if (SystemUtilities.isOnline(getActivity())) {
            Intent intent = new Intent(getActivity(), PhotoViewActivity.class);

            // Send the picture thumbnail as an extra, to serve as a placeholder image
            Bitmap thumbnailBitmap = ((BitmapDrawable) ((ImageView) thumbnailView).getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapdata = stream.toByteArray();

            // Send picture id and url with intent
            intent.putExtra(PhotoViewActivity.EXTRA_PHOTO_ID, picture.getObjectId());
            intent.putExtra(PhotoViewActivity.EXTRA_FULLSIZE_URL, picture.getPhoto().getUrl());
            intent.putExtra(PhotoViewActivity.EXTRA_THUMBNAIL_URL, picture.getThumbnail().getUrl());
            intent.putExtra(PhotoViewActivity.EXTRA_THUMBNAIL_BYTES, bitmapdata);

            // Animate the activity transition using the thumbnail as a shared view
            String transitionName = getString(R.string.transition_picture);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                            thumbnailView,   // The view which starts the transition
                            transitionName    // The transitionName of the view we’re transitioning to
                    );

            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
        } else {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "No Internet Connection, Cannot Download Image.", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Launch VideoViewActivity to stream video file
    void playVideo(Video video) {
        if (SystemUtilities.isOnline(getActivity())) {
            Intent videoIntent = new Intent(getActivity(), VideoViewActivity.class);

            // Send video id and url with intent
            videoIntent.putExtra(VideoViewActivity.VIDEO_ID, video.getObjectId());
            videoIntent.putExtra(VideoViewActivity.VIDEO_URL, video.getVideo().getUrl());
            ActivityCompat.startActivity(getActivity(), videoIntent, null);
        } else {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "No Internet Connection, Cannot Stream Video.", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Item multi-select methods
     */

    // Toggle if grid item is selected
    public boolean toggleItemSelected(ParseObject object) {
        boolean isToggled = false;
        if (mGridSelectionMap.get(object.getObjectId())) { // If item is selected...

            // Unselect item
            mGridSelectionMap.put(object.getObjectId(), false);
            --numSelected;

            if (numSelected == 0) {
                // Dismiss selection snackbar if no more items are selected
                mSelectionSnackbar.dismiss();
                ((GalleryActivity) getActivity()).mFab.setVisibility(View.VISIBLE);
            } else {
                mSelectionSnackbar.setText(numSelected + " Selected");
            }
        } else {

            // Mark item as slected
            mGridSelectionMap.put(object.getObjectId(), true);
            ++numSelected;
            isToggled = true;

            // If this is first selected item, show selection snackbar
            if (numSelected == 1) {
                showSelectionSnackbar();
            } else {
                mSelectionSnackbar.setText(numSelected + " Selected");
            }
        }
        return isToggled;
    }

    // Generate a snackbar to show selection text and action
    private void showSelectionSnackbar() {
        mSelectionSnackbar = Snackbar.make(mMediaGrid, "1 Selected", Snackbar.LENGTH_INDEFINITE);
        mSelectionSnackbar.setAction("Delete", snackbackClickListener);
        mSelectionSnackbar.setActionTextColor(getResources().getColor(R.color.md_red_500));
        mSelectionSnackbar.show();
        ((GalleryActivity) getActivity()).mFab.setVisibility(View.GONE);
    }

    // Return a list of all selected items
    private ArrayList<ParseObject> getAllSelected() {
        ArrayList<ParseObject> selectedObjects = new ArrayList<>();

        for (ParseObject object : mDisplayedMedia) {
            if (mGridSelectionMap.get(object.getObjectId())) {
                selectedObjects.add(object);
            }
        }

        return selectedObjects;
    }

    // Unselect all selected items
    public void clearSelected() {
        for (ParseObject object : mDisplayedMedia) {
            if (mGridSelectionMap.get(object.getObjectId())) {
                toggleItemSelected(object);
            }
        }
        ((GalleryActivity) getActivity()).mFab.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
    }

    // Delete all selected items
    private void deleteObjects(final ArrayList<ParseObject> selectedObjects) {
        if (SystemUtilities.isOnline(getActivity())) {
            Toast.makeText(getActivity(), "Deleting Items", Toast.LENGTH_SHORT).show();
            ParseObject.deleteAllInBackground(selectedObjects, new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        ParseObject.unpinAllInBackground(selectedObjects); // Remove deleted items from cache
                        Toast.makeText(getActivity(), "Deletion Complete", Toast.LENGTH_SHORT).show();
                        refresh();
                    } else {
                        String errorReport = "Error Deleting: " + e.getMessage();
                        SystemUtilities.reportError(TAG, errorReport);
                    }
                }
            });
        } else {
            clearSelected();
            Snackbar.make(getActivity().findViewById(android.R.id.content), "No Internet Connection, Cannot Delete Item(s).", Snackbar.LENGTH_SHORT).show();
        }
    }
}
