package com.triestpa.cloudcamera.Utilities;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

/**
 * Coordinator Layout Behavior to make FAB scroll in sync with toolbar
 * Adapted from <https://mzgreen.github.io/2015/06/23/How-to-hideshow-Toolbar-when-list-is-scrolling%28part3%29/>
 */
public class ScrollingButtonBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private int toolbarHeight;

    public ScrollingButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.toolbarHeight = SystemUtilities.getToolbarHeight(context);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
        if (dependency instanceof AppBarLayout) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;
            int distanceToScroll = fab.getHeight() + fabBottomMargin; // Scroll the full size of fab and distance to bottom
            float ratio = (float)dependency.getY()/(float)toolbarHeight; // Scroll in sync with the toolbar
            fab.setTranslationY((distanceToScroll * ratio) + toolbarHeight - fabBottomMargin); // Move FAB
        }
        return true;
    }
}