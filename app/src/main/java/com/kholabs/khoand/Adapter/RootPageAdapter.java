package com.kholabs.khoand.Adapter;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.kholabs.khoand.Fragment.FeedFragment;
import com.kholabs.khoand.R;

/**
 * Created by Aladar-PC2 on 2/12/2018.
 */

public class RootPageAdapter extends PagerAdapter {
    private Activity mContext;
    private int mLength = 0;
    private int defaultId = 1000;

    public RootPageAdapter(Activity context, int length) {
        mContext = context;
        mLength = length;
    }

    @Override
    public int getCount() {
        return mLength;
    }

    @Override
    public Object instantiateItem(View container, int position) {
        FrameLayout framelayout = (FrameLayout) View.inflate(mContext, R.layout.fragment_feed_fragment_root, null);;
        if (position == 0) {
            framelayout = (FrameLayout) View.inflate(mContext, R.layout.fragment_feed_fragment_root, null);

        }
        else if (position == 1) {
            framelayout = (FrameLayout) View.inflate(mContext, R.layout.fragment_message_fragment_root, null);
        }
        else if (position == 2) {
            framelayout = (FrameLayout) View.inflate(mContext, R.layout.fragment_profile_fragment_root, null);
        }
        return framelayout;
    }

    @Override
    public void destroyItem(View container, int position, Object view) {
        ((ViewPager) container).removeView((View) view);
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);
    }

    @Override
    public void finishUpdate(View container) {
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(View container) {
    }
}
