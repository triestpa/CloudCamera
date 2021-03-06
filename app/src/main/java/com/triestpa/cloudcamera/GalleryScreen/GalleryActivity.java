package com.triestpa.cloudcamera.GalleryScreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.triestpa.cloudcamera.R;
import com.triestpa.cloudcamera.UploadsScreen.UploadManager;
import com.triestpa.cloudcamera.LoginScreen.LoginActivity;
import com.triestpa.cloudcamera.Utilities.SystemUtilities;

/**
 * Gallery Activity: Display the photo and video gallerys in a viewpager
 */
public class GalleryActivity extends AppCompatActivity {
    final static String TAG = GalleryActivity.class.getName();

    private SectionsPagerAdapter mSectionsPagerAdapter; // Adapter to load gallery fragments into view pager
    private ViewPager mViewPager; // View pager to display gallery fragments
    FloatingActionButton mFab; // Floating action button to allow easy access to camera activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start login activity no current user
        if (ParseUser.getCurrentUser() == null) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

        setContentView(R.layout.activity_gallery);

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Assign FAB to pop back activity stack when pressed
        mFab = (FloatingActionButton) findViewById(R.id.camera_fab_button);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_log_out:
                // Show a dialog to confirm that user wants to log out
                SystemUtilities.buildDialog(GalleryActivity.this, "Are you sure you want to log out?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseUser.logOutInBackground(new LogOutCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    // Clear the current users' uploads
                                    UploadManager.getInstance().clearUploads();

                                    // Notify user that logout was successful
                                    Toast.makeText(GalleryActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();

                                    // Start login activity
                                    Intent i = new Intent(GalleryActivity.this, LoginActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                } else {
                                    SystemUtilities.reportError(TAG, "Log out error: " + e.getMessage());
                                }
                            }
                        });
                    }
                }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mViewPager == null) {
            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);
        }
    }


    @Override
    public void onBackPressed() {
        // Get the current showing gallery fragment
        GalleryGridFragment currentPage = (GalleryGridFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());

        if (currentPage != null) {
            // If the selection menu is showing, close it
            if (currentPage.numSelected > 0) {
                currentPage.clearSelected();
                return;
            }
        }
        super.onBackPressed();
    }

    // Fragment pager adapter to serve image and video galleries to viewpager
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    return GalleryGridFragment.newInstance(GalleryGridFragment.TYPE_PHOTO_GRID);
                case 1:
                    return GalleryGridFragment.newInstance(GalleryGridFragment.TYPE_VIDEO_GRID);
                default:
                    return GalleryGridFragment.newInstance(GalleryGridFragment.TYPE_PHOTO_GRID);
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "IMAGES";
                case 1:
                    return "VIDEOS";
            }
            return null;
        }
    }
}
