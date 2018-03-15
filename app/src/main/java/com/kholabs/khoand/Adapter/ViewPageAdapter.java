package com.kholabs.khoand.Adapter;

/**
 * Created by Aladar-PC2 on 1/9/2018.
 */

import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.List;

public class ViewPageAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> infos;
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public ViewPageAdapter(FragmentManager fm, List<Fragment> infos) {
        super(fm);
        this.infos = infos;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object objet) {
        super.destroyItem(container, position, objet);
    }


    @Override
    public Object instantiateItem(ViewGroup arg0, int position) {
        android.support.v4.app.Fragment ff = (android.support.v4.app.Fragment) super.instantiateItem(arg0, position);
        return ff;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {

        return infos.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

}
