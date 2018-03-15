package com.kholabs.khoand.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.CustomView.ViewPager.NonSwipeableViewPager;
import com.kholabs.khoand.Fragment.IntroFragment.PageFive;
import com.kholabs.khoand.Fragment.IntroFragment.PageFour;
import com.kholabs.khoand.Fragment.IntroFragment.PageOne;
import com.kholabs.khoand.Fragment.IntroFragment.PageSeven;
import com.kholabs.khoand.Fragment.IntroFragment.PageSix;
import com.kholabs.khoand.Fragment.IntroFragment.PageThree;
import com.kholabs.khoand.Fragment.IntroFragment.PageTwo;
import com.kholabs.khoand.R;
import com.viewpagerindicator.LoopingCirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {

    private final int TAKE_PHOTO_FROM_GALLERY = 101;
    private final int TAKE_PHOTO_FROM_CAMERA = 105;
    private final int PIC_CROP = 106;

    private Button btnStep;
    private NonSwipeableViewPager introPager;
    private FrameLayout mCircleLayout;
    private IntroPagerAdapter mAdapter;
    private List<Fragment> fragments = new ArrayList<>();
    private boolean isAvatarLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        Intent intent = getIntent();
        if (intent != null)
        {
            isAvatarLoaded = intent.getBooleanExtra("isAvatarLoaded", false);
        }
        else
            isAvatarLoaded = false;

        btnStep = (Button)findViewById(R.id.btStep);
        introPager = (NonSwipeableViewPager)findViewById(R.id.introPager);
        mCircleLayout = (FrameLayout)findViewById(R.id.pagesContainer);

        initViewPager();
        initEvent();
    }

    private void initViewPager()
    {
        fragments.add(new PageOne());
        fragments.add(new PageTwo());
        if (!isAvatarLoaded)
            fragments.add(new PageThree());
        fragments.add(new PageFour());
        fragments.add(new PageFive());
        fragments.add(new PageSix());
        fragments.add(new PageSeven());

        mAdapter = new IntroPagerAdapter(getSupportFragmentManager(), fragments);

        introPager.setAdapter(mAdapter);
        if (isAvatarLoaded)
            introPager.setOffscreenPageLimit(6);
        else
            introPager.setOffscreenPageLimit(7);

        introPager.setCurrentItem(0);

        LoopingCirclePageIndicator circlePageIndicator = new LoopingCirclePageIndicator(this);
        circlePageIndicator.setViewPager(introPager);
        circlePageIndicator.setStrokeColor(Color.parseColor("#f0f0f0"));
        circlePageIndicator.setExtraSpacing(30);
        circlePageIndicator.setPageColor(Color.parseColor("#909090"));
        //circlePageIndicator.setBackgroundColor();
        mCircleLayout.addView(circlePageIndicator);
    }

    private void initEvent()
    {
        btnStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nPage = introPager.getCurrentItem() + 1;
                if (isAvatarLoaded && nPage > 5)
                    finish();
                else if (nPage > 6)
                    finish();
                introPager.setCurrentItem(nPage);
                btnStep.setText("Next");
            }
        });
    }

    public class IntroPagerAdapter extends FragmentStatePagerAdapter {
        public List<Fragment> fragmentsA;

        public IntroPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            fragmentsA = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentsA.get(position);
        }

        @Override
        public int getCount() {
            return (fragmentsA != null) ? fragmentsA.size() : 0;
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO_FROM_GALLERY || requestCode == TAKE_PHOTO_FROM_CAMERA || requestCode == PIC_CROP) {
                ActivityResultBus.getInstance().postQueue(
                        new ActivityResultEvent(requestCode, resultCode, data));
            }
        }
    }
}
