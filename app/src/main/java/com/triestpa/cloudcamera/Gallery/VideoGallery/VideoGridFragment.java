package com.triestpa.cloudcamera.Gallery.VideoGallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.triestpa.cloudcamera.Model.Video;
import com.triestpa.cloudcamera.R;

import java.util.ArrayList;
import java.util.List;


public class VideoGridFragment extends Fragment {
    private final static String TAG = VideoGridFragment.class.getName();
    private RecyclerView mImageGrid;
    private VideoGridAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public VideoGridFragment() {
        // Required empty public constructor
    }

    public static VideoGridFragment newInstance() {
        return new VideoGridFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video_grid, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.video_grid_swipe_refresh_layout);
        mImageGrid = (RecyclerView) v.findViewById(R.id.image_grid);


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mImageGrid.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mImageGrid.setLayoutManager(layoutManager);

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        int imageDimensions = metrics.widthPixels / 3;

        mAdapter = new VideoGridAdapter(new ArrayList<Video>(), imageDimensions, this);

        mImageGrid.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshVideos();
            }
        });

        mSwipeRefreshLayout.setRefreshing(true);
        refreshVideos();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwipeRefreshLayout.setRefreshing(true);
        refreshVideos();
    }

    void playVideo(Video video) {
        Intent videoIntent = new Intent(getActivity(), VideoViewActivity.class);
        videoIntent.putExtra(VideoViewActivity.VIDEO_ID, video.getObjectId());
        videoIntent.putExtra(VideoViewActivity.VIDEO_URL, video.getVideo().getUrl());
        ActivityCompat.startActivity(getActivity(), videoIntent, null);
    }

    private void refreshVideos() {
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<Video>() {
            @Override
            public void done(List<Video> videos, ParseException e) {
                if (e == null) {
                    if (videos == null || videos.isEmpty()) {
                        mSwipeRefreshLayout.setVisibility(View.GONE);
                    }
                    else {
                        mAdapter.setData((ArrayList<Video>) videos);
                        mAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }
}
