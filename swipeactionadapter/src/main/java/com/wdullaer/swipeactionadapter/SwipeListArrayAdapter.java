package com.wdullaer.swipeactionadapter;

import android.widget.BaseAdapter;
import android.widget.ListAdapter;

/**
 * Created by plast on 8/23/2016.
 */
public abstract class SwipeListArrayAdapter extends BaseAdapter {
    public abstract boolean isItemSwipeable(int position);
    public abstract int getTagId();
}
