package com.triestpa.cloudcamera.Gallery.PhotoGallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PhotoGridFragment extends Fragment {
    private final static String TAG = PhotoGridFragment.class.getName();
    private RecyclerView mImageGrid;
    private PhotoGridAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected HashMap<String, Boolean> mGridSelectionMap;
    protected int numSelected;

    public PhotoGridFragment() {
        // Required empty public constructor
    }

    public static PhotoGridFragment newInstance() {
        return new PhotoGridFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_photo_grid, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.photo_grid_swipe_refresh_layout);
        mImageGrid = (RecyclerView) v.findViewById(R.id.image_grid);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mImageGrid.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mImageGrid.setLayoutManager(layoutManager);

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        int imageDimensions = metrics.widthPixels / 3;

        mAdapter = new PhotoGridAdapter(new ArrayList<Picture>(), imageDimensions, this);

        mImageGrid.setAdapter(mAdapter);

        mGridSelectionMap = new HashMap<>();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPhotos();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwipeRefreshLayout.setRefreshing(true);
        refreshPhotos();
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

    public boolean togglePictureSelected(Picture picture) {
        boolean isToggled = false;
        if (mGridSelectionMap.get(picture.getObjectId())) {
            mGridSelectionMap.put(picture.getObjectId(), false);
            --numSelected;
        }
        else {
            mGridSelectionMap.put(picture.getObjectId(), true);
            ++numSelected;
            isToggled = true;
        }
        return isToggled;
    }

    private void refreshPhotos() {
        ParseQuery<Picture> query = ParseQuery.getQuery(Picture.class);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<Picture>() {
            @Override
            public void done(List<Picture> pictures, ParseException e) {
                if (e == null) {
                    if (pictures == null || pictures.isEmpty()) {
                        mSwipeRefreshLayout.setVisibility(View.GONE);
                    }
                    else {
                        mAdapter.setData((ArrayList<Picture>) pictures);
                        for (Picture picture : pictures) {
                            mGridSelectionMap.put(picture.getObjectId(), false);
                        }
                        numSelected = 0;
                        mAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    mSwipeRefreshLayout.setVisibility(View.GONE);
                    Log.e(TAG, e.getMessage());
                    SystemUtilities.showToastMessage("Error Loading Photos: " + e.getMessage());
                }
            }
        });
    }
}
