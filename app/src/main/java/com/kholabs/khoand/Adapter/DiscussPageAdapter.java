package com.kholabs.khoand.Adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kholabs.khoand.R;

/**
 * Created by Aladar-PC2 on 2/7/2018.
 */

public class DiscussPageAdapter extends PagerAdapter {

    private Activity mContext;
    private int mLength = 0;
    private int defaultId = 1000;
    private String[] mData = {"What is your injury question?", "How long have you been dealing with this injury?", "When is it at its worst?"};

    public DiscussPageAdapter(Activity context, int length) {
        mContext = context;
        mLength = length;
    }

    @Override
    public int getCount() {
        return mLength;
    }

    @Override
    public Object instantiateItem(View container, int position) {
        LinearLayout linearLayout = (LinearLayout) View.inflate(mContext, R.layout.fragment_discuss, null);
        EditText edtQuery = (EditText) linearLayout.findViewById(R.id.edtQuery);
        edtQuery.setHint(mData[position]);
        edtQuery.setTag("edt" + position);
        linearLayout.setTag("lin" + position);

        ((ViewPager) container).addView(linearLayout, 0);
        return linearLayout;
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
