package com.triestpa.cloudcamera;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class ImageGridFragment extends Fragment {
    private OnImageGridInteractionListener mListener;
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

        mAdapter = new PhotoGridAdapter(new ArrayList<Picture>(), getActivity());
        mImageGrid.setAdapter(mAdapter);

        refreshPhotos();

        return v;
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnImageGridInteractionListener) {
            mListener = (OnImageGridInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnImageGridInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
