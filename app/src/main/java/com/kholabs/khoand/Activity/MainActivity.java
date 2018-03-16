package com.kholabs.khoand.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.ContentFrameLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.CustomView.NavigationView.BottomNavigationEx;
import com.kholabs.khoand.CustomView.ViewPager.NonSwipeableViewPager;
import com.kholabs.khoand.Fragment.ContentFragment;
import com.kholabs.khoand.Fragment.FeedPostFragment;
import com.kholabs.khoand.Fragment.MessageChatFragment;
import com.kholabs.khoand.Fragment.MessageFragment;
import com.kholabs.khoand.Fragment.RootFragment.FeedFragmentRoot;
import com.kholabs.khoand.Fragment.RootFragment.MessageFragmentRoot;
import com.kholabs.khoand.Fragment.RootFragment.ProfileFragmentRoot;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.ParseUtil.ParseUtils;
import com.kholabs.khoand.Utils.ParseUtil.PrefManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends FragmentActivity {

    public static String keyword = "";

    NonSwipeableViewPager mPager;
    private SectionsPagerAdapter mRootAdapter;

    private final int TAKE_PHOTO_FROM_GALLERY = 101;
    private final int TAKE_VIDEO_FROM_GALLERY = 102;
    private final int TAKE_PHOTO_FROM_CAMERA = 105;
    private final int PIC_CROP = 106;

    private static List<String> fragments = new ArrayList<String>();

    private RelativeLayout rootLayout;
    private BottomNavigationEx bottomNavigationView;
    private MenuItem prevMenuItem = null;
    private PrefManager pref;

    public interface onKeyBackPressedListener {
        public void onBack();
    }

    private onKeyBackPressedListener mOnkeyBackPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        mPager = (NonSwipeableViewPager) findViewById(R.id.pager);
        initUserdata();
        initViewPager();


        bottomNavigationView = (BottomNavigationEx)
                findViewById(R.id.navigation);

        bottomNavigationView.setTextVisibility(false);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_feed:
                                setDrawableCurrentItem(0);
                                mPager.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPager.setCurrentItem(0);
                                    }
                                });

                                break;
                            case R.id.action_message:
                                setDrawableCurrentItem(1);
                                mPager.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPager.setCurrentItem(1);
                                    }
                                });
                                break;
                            case R.id.action_profile:
                                setDrawableCurrentItem(2);
                                mPager.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPager.setCurrentItem(2);
                                    }
                                });
                                break;
                        }
                        return true;
                    }
                });

        pref = new PrefManager(getApplicationContext());

        String email = pref.getEmail();
        if (email != null)
            ParseUtils.subscribeWithEmail(email);
        else {
            ParseUser currentUser = ParseUser.getCurrentUser();
            email = currentUser.getEmail();
            pref.createLoginSession(email);
            ParseUtils.subscribeWithEmail(email);
        }

        MyApp.getInstance().setCurrentActivity(this);
    }

    private void setDrawableCurrentItem(int pos)
    {
        if (pos == 0) {
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.tb_feed_selected);
            bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.tb_messages_unselected);
            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.tb_profile_unselected);
        } else if (pos ==1) {
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.tb_feed_unselected);
            bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.tb_messages_selected);
            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.tb_profile_unselected);
        } else if (pos == 2) {
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.tb_feed_unselected);
            bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.tb_messages_unselected);
            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.tb_profile_selected);
        }
    }


    public void setPagerFragment(int a)
    {
        mPager.setCurrentItem(a);
        if (a == 1)
        {
            Fragment ft = getSupportFragmentManager().findFragmentById(R.id.message_root_frame);
            if (ft instanceof MessageFragment)
            {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.message_root_frame, new MessageChatFragment(), "MessageChat")
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    public void openMessageThread()
    {
        mPager.setCurrentItem(1);

        Fragment ft = getSupportFragmentManager().findFragmentById(R.id.message_root_frame);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.message_root_frame, new MessageChatFragment(), "MessageChat")
                .addToBackStack(null)
                .commit();
    }

    public void openPostPage()
    {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.feed_root_frame, new FeedPostFragment(), "Post")
                .addToBackStack(null)
                .commit();
    }

    private void initUserdata()
    {
        if (ParseUser.getCurrentUser() != null)
        {
            MyApp.getInstance().setShareUser(ParseUser.getCurrentUser());
            MyApp.getInstance().setFromParent(ConstValues.ProfileFrom.fromProfile);
        }
    }

    private void initViewPager()
    {
        fragments.add(FeedFragmentRoot.class.getName());
        fragments.add(MessageFragmentRoot.class.getName());
        fragments.add(ProfileFragmentRoot.class.getName());

        mRootAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mPager.setAdapter(mRootAdapter);
        mPager.setOffscreenPageLimit(3);
        mPager.setPageTransformer(true, new DepthPageTransformer());

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else
                {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setOnkeyBackPressedListener(onKeyBackPressedListener listener) {
        mOnkeyBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (mOnkeyBackPressedListener != null)
            mOnkeyBackPressedListener.onBack();
    }


    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO_FROM_GALLERY || requestCode == TAKE_VIDEO_FROM_GALLERY || requestCode == TAKE_PHOTO_FROM_CAMERA || requestCode == PIC_CROP) {
                ActivityResultBus.getInstance().postQueue(
                        new ActivityResultEvent(requestCode, resultCode, data));
            }
        }
    }

    public void hideKeyboardFrom() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            rootLayout.requestFocus();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public List<String> fragmentsA;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentsA = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return Fragment.instantiate(MainActivity.this, fragmentsA.get(position));
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    public void runRemovePostTask(ParseObject post)
    {
        new removePostTask().execute(post);
    }

    private class removePostTask extends AsyncTask<ParseObject, Void, ParseObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ParseObject doInBackground(ParseObject... results) {
            ParseObject result = results[0];
            return result;
        }

        @Override
        protected void onPostExecute(ParseObject result) {
            ExecutorService executor = Executors.newFixedThreadPool(2);

            Runnable thread1 = new Runnable() {
                @Override
                public void run() {
                    removeAllComments(result);
                }
            };

            executor.submit(thread1);

            Runnable thread2 = new Runnable() {
                @Override
                public void run() {
                    removeAllSupports(result);
                }
            };

            executor.submit(thread2);
        }
    }


    private void removeAllSupports(ParseObject post)
    {
        ParseQuery<ParseObject> q_supports = ParseQuery.getQuery("Supports");
        q_supports.whereEqualTo("post", post);
        q_supports.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0)
                    return;
                for (ParseObject support : objects) {
                    try {
                        support.delete();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

    }

    private void removeAllComments(ParseObject post)
    {
        ParseQuery<ParseObject> q_comments = ParseQuery.getQuery("Comments");
        q_comments.whereEqualTo("post", post);
        q_comments.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0)
                    return;
                for (ParseObject comment : objects) {
                    try {
                        comment.delete();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
}
