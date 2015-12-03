package com.triestpa.cloudcamera.Gallery.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.triestpa.cloudcamera.Gallery.Adapters.PhotoGridAdapter;
import com.triestpa.cloudcamera.Gallery.PhotoViewActivity;
import com.triestpa.cloudcamera.Model.Picture;
import com.triestpa.cloudcamera.R;

import java.util.ArrayList;
import java.util.List;


public class ImageGridFragment extends Fragment {
    private final static String TAG = ImageGridFragment.class.getName();
    RecyclerView mImageGrid;
    PhotoGridAdapter mAdapter;

    public ImageGridFragment() {
        // Required empty public constructor
    }

    public static ImageGridFragment newInstance() {
        ImageGridFragment fragment = new ImageGridFragment();
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
        View v = inflater.inflate(R.layout.fragment_image_grid, container, false);

        mImageGrid = (RecyclerView) v.findViewById(R.id.image_grid);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mImageGrid.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        mImageGrid.setLayoutManager(layoutManager);

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        int imageDimensions = metrics.widthPixels / 2;

        mAdapter = new PhotoGridAdapter(new ArrayList<Picture>(), imageDimensions, this);

        mImageGrid.setAdapter(mAdapter);

        refreshPhotos();

        return v;
    }

    public void showLargePhoto(View v, String url) {
        Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
        intent.putExtra("URL", url);
        startActivity(intent);
    }

    protected void refreshPhotos() {
        ParseQuery<Picture> query = ParseQuery.getQuery(Picture.class);
        query.findInBackground(new FindCallback<Picture>() {
            @Override
            public void done(List<Picture> pictures, ParseException e) {
                mAdapter.setData((ArrayList<Picture>) pictures);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}
