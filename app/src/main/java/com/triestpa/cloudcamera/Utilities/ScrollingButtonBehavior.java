package com.triestpa.cloudcamera.Utilities;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.triestpa.cloudcamera.R;

/**
 * Coordinator Layout Behavior to make FAB scroll in sync with toolbar
 * Adapted from <https://mzgreen.github.io/2015/06/23/How-to-hideshow-Toolbar-when-list-is-scrolling%28part3%29/>
 */
public class ScrollingButtonBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private int toolbarHeight;
    private int fabMargin;
    private float minY;

    public ScrollingButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.toolbarHeight = SystemUtilities.getToolbarHeight(context);
        this.fabMargin = (int) context.getResources().getDimension(R.dimen.fab_margin);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        return super.layoutDependsOn(parent, fab, dependency) || (dependency instanceof AppBarLayout);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        if (dependency instanceof AppBarLayout) {
            int distanceToScroll = fab.getHeight() + fabMargin; // Scroll the full size of fab and distance to bottom

            // Normalize value
            float currentY = dependency.getY();
            if (currentY < minY) {
                minY = currentY;
            }
            currentY = currentY - minY;


            float ratio = currentY/(float)toolbarHeight; // Scroll in sync with the toolbar
            Log.d("ScrollBehavior", "Ratio: " + ratio);
            fab.setTranslationY(distanceToScroll - (distanceToScroll * ratio));// Move fab
        }
        return true;
    }
}