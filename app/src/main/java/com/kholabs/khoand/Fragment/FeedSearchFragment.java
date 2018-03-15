package com.kholabs.khoand.Fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;

import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.FragmentPopKeys;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.R;

import java.util.ArrayList;
import java.util.List;

import static com.kholabs.khoand.Activity.MainActivity.keyword;

public class FeedSearchFragment extends Fragment implements View.OnClickListener,
        FeedSearchUserFragment.ParentFragmentListener,
        FeedSearchPostFragment.ParentFragmentListener,
        MainActivity.onKeyBackPressedListener
{
    private static final String ARG_SEARCHNAME = "search_name";
    private String searchName = "";

    PagerSlidingTabStrip tabs;
    LinearLayout mTabsLinearLayout;

    ViewPager viewPager;
    LinearLayout llBack;
    EditText etSearch;

    SectionsPagerAdapter adapter;
    private Fragment searchFragment, postFragment;
    private View rootView;
    private List<Fragment> listFragment = new ArrayList<Fragment>();;

    public FeedSearchFragment() {
    }
    public static FeedSearchFragment newInstance(String username) {
        FeedSearchFragment fragment = new FeedSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCHNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = null;
        if (getArguments() != null) {
            searchName = getArguments().getString(ARG_SEARCHNAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_search, container, false);
            ((MainActivity)getActivity()).setOnkeyBackPressedListener(this);
            tabs= (PagerSlidingTabStrip)rootView.findViewById(R.id.tabs);
            tabs.setShouldExpand(true);
            tabs.setIndicatorColor(R.color.colorPrimary);
            tabs.setDividerColor(R.color.colorPrimary);
            tabs.setIndicatorHeight(R.dimen.height_indicator_tab);

            viewPager= (ViewPager)rootView.findViewById(R.id.viewPager);
            llBack= (LinearLayout) rootView.findViewById(R.id.llBack);
            etSearch = (EditText) rootView.findViewById(R.id.etSearch);

            etSearch.setText(searchName);
            llBack.setOnClickListener(this);

            searchFragment = FeedSearchUserFragment.newInstance(searchName, FeedSearchFragment.this);
            postFragment = FeedSearchPostFragment.newInstance(searchName, FeedSearchFragment.this);

            listFragment.clear();
            listFragment.add(searchFragment);
            listFragment.add(postFragment);

            adapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager(), listFragment);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(2);
            tabs.setViewPager(viewPager);

            setUpTabStrip();
            viewPager.setCurrentItem(0);

            initEvent();
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack(FragmentPopKeys.FEED, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llBack:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
                break;

        }
    }

    public void setUpTabStrip() {

        //your other customizations related to tab strip...blahblah
        // Set first tab selected
        mTabsLinearLayout = ((LinearLayout) tabs.getChildAt(0));
        for (int i = 0; i < mTabsLinearLayout.getChildCount(); i++) {
            TextView tv = (TextView) mTabsLinearLayout.getChildAt(i);

            if (i == 0) {
                tv.setBackgroundResource(R.color.colorGreen);
                tv.setTextColor(Color.DKGRAY);
            } else {
                tv.setBackgroundResource(R.color.colorPrimary);
                tv.setTextColor(Color.GREEN);
            }
        }


    }

    private void sendReloadRequest()
    {
        Fragment frag = listFragment.get(viewPager.getCurrentItem());
        if (frag != null && frag instanceof FeedSearchUserFragment) {
            ((FeedSearchUserFragment)frag).setKeyWord(searchName);
        } else if (frag != null && frag instanceof  FeedSearchPostFragment) {
            ((FeedSearchPostFragment)frag).setKeyWord(searchName);
        }

    }

    public void initEvent()
    {
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    searchName = etSearch.getText().toString();
                    if (searchName.equals("")) {
                        return false;
                    }

                    sendReloadRequest();
                    return true;
                }
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {

                // do this instead, assuming your adapter reference
                // is named mAdapter:
                for(int i=0; i < mTabsLinearLayout.getChildCount(); i++){
                    TextView tv = (TextView) mTabsLinearLayout.getChildAt(i);
                    if(i == position){
                        tv.setBackgroundResource(R.color.colorGreen);
                        tv.setTextColor(Color.DKGRAY);
                    } else {
                        tv.setBackgroundResource(R.color.colorPrimary);
                        tv.setTextColor(Color.GREEN);
                    }
                }

                sendReloadRequest();
            }

            @Override
            public void onPageScrollStateChanged(int state) {  }
        });
    }

    @Override
    public void GoToNextFragment() {
        MyApp.getInstance().setFromParent(ConstValues.ProfileFrom.fromFeedPage);

        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.feed_root_frame, new InstagramProfileFragment(), "Profile")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void GoToNextPostFragment() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.feed_root_frame, new FeedPostFragment(), "Post")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBack() {
        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
        ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        String[] title = { "Users", "Posts"};
        private List<Fragment> infos;

        public SectionsPagerAdapter(FragmentManager fm, List<Fragment> infos) {
            super(fm);
            this.infos = infos;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object objet) {
            super.destroyItem(container, position, objet);
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            return createdFragment;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return infos.get(position);
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }
}
