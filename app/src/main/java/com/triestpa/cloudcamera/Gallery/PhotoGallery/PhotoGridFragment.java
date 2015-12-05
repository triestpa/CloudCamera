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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class PhotoGridFragment extends Fragment {
    private final static String TAG = PhotoGridFragment.class.getName();
    RecyclerView mImageGrid;
    PhotoGridAdapter mAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

    public PhotoGridFragment() {
        // Required empty public constructor
    }

    public static PhotoGridFragment newInstance() {
        PhotoGridFragment fragment = new PhotoGridFragment();
        return fragment;
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
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.image_grid_swipe_refresh_layout);
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

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPhotos();
            }
        });

        mSwipeRefreshLayout.setRefreshing(true);
        refreshPhotos();

        return v;
    }

    public void showLargePhoto(View thumbnailView, String fullSizeURL, String thumbnailURL) {
        Intent intent = new Intent(getActivity(), PhotoViewActivity.class);

        Bitmap thumbnailBitmap = ((BitmapDrawable)((ImageView)thumbnailView).getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();

        intent.putExtra(PhotoViewActivity.EXTRA_FULLSIZE_URL, fullSizeURL);
        intent.putExtra(PhotoViewActivity.EXTRA_THUMBNAIL_URL, thumbnailURL);
        intent.putExtra(PhotoViewActivity.EXTRA_THUMBNAIL_BYTES, bitmapdata);

        String transitionName = getString(R.string.transition_picture);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        thumbnailView,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning to
                );

        ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
    }

    protected void refreshPhotos() {
        ParseQuery<Picture> query = ParseQuery.getQuery(Picture.class);
        query.findInBackground(new FindCallback<Picture>() {
            @Override
            public void done(List<Picture> pictures, ParseException e) {
                mAdapter.setData((ArrayList<Picture>) pictures);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
