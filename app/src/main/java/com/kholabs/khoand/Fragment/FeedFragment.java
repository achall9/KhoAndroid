package com.kholabs.khoand.Fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Adapter.DiscussPageAdapter;
import com.kholabs.khoand.Adapter.FeedListAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.Common.FragmentBase;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.CustomView.ActionSheet;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshBase;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshRecyclerView;
import com.kholabs.khoand.Interface.SaveMediaCallBack;
import com.kholabs.khoand.Model.Feed;
import com.kholabs.khoand.Model.ParseMedia;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.EndlessRecyclerOnScrollListener;
import com.kholabs.khoand.receiver.CustomPushReceiver;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.LoopingCirclePageIndicator;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.ceryle.segmentedbutton.SegmentedButtonGroup;

import static com.kholabs.khoand.Activity.MainActivity.keyword;


public class FeedFragment extends FragmentBase implements View.OnClickListener, ConstValues,
        FeedListAdapter.FeedListItemListener,
        ActionSheet.ActionSheetListener {
    private final int TAKE_PHOTO_FROM_GALLERY = 101;
    private final int TAKE_VIDEO_FROM_GALLERY = 102;
    private ActionSheet takePhotoActionSheet = null;
    private ActionSheet takeVideoFromCameraActionSheet = null;

    private boolean isTakingPostPhoto = false;
    private boolean isTakingPostVideo = false;

    private View rootView;
    private Uri mImageViewUri = null;
    private Uri mVideoUri = null;
    private Bitmap mImageThumbnail = null;
    private Bitmap mVideoThumbnail = null;

    private boolean bFadeFixed = true;
    int mCurrIndex = 0;

    public static ArrayList<Feed> selectData = new ArrayList<Feed>();
    private FeedListAdapter adapter;

    private ProgressBar progressBar;
    private LinearLayoutManager linearLayoutManager;
    private PullToRefreshRecyclerView mRecyclerView;

    private EditText etSearch;
    private ImageView ivBell, ivFilter;

    private ExpandableLayout expandFilter;
    private LinearLayout filterArea;
    private LinearLayout queryFixed, queryList, queryLayout, collaseLayout, statusLayout;
    private Button btnQueryNext, btnFilterApply;
    private ImageView imgPhotoUpload, imgVideoUpload;
    private SegmentedButtonGroup sgFilterLocation, sgFilterAnswer, sgFilterOrder;

    /* Discuss Adapter View */
    private ViewPager mPager;
    private DiscussPageAdapter mDiscussAdapter;
    private ViewGroup mFrameLayout;
    /* End */

    /* Blur Progress Layout */
    private RelativeLayout progLayout;
    private TextView tvProgTitle, tvProgSubTitle;
    private ProgressBar pvUploadbar;
    private Button btnProgCancel;
    /* */

    private int feedFilter;
    private int feedType;
    private int loadCount = 0;
    private int temPage = 0;
    private int itemsPerPage = 10;

    private boolean setFiltered = false;
    private boolean isDialogShow = false;

    private int fLocation = -1;
    private int fAnswer = -1;
    private int fOrder = -1;

    private String strContent, strHowlong, strWorst;

    private MediaUploadTask mediaTask;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    private View.OnClickListener snapPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().setTheme(R.style.ActionSheetStyleIOS7);
            isTakingPostPhoto = true;
            isTakingPostVideo = false;

            if (takePhotoActionSheet == null && mImageViewUri == null) {
                takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_image))
                        .setCancelableOnTouchOutside(true)
                        .setListener(FeedFragment.this)
                        .show();
            } else {
                if (mImageViewUri == null) {

                    takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_image))
                            .setCancelableOnTouchOutside(true)
                            .setListener(FeedFragment.this)
                            .show();
                } else {

                    takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_replace_image),
                                    getResources().getString(R.string.action_sheet_remove_image))
                            .setCancelableOnTouchOutside(true)
                            .setImageVisiblity(true)
                            .setImageBitmap(mImageThumbnail)
                            .setListener(FeedFragment.this)
                            .show();
                }
            }

        }
    };

    private View.OnClickListener snapPostVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().setTheme(R.style.ActionSheetStyleIOS7);
            isTakingPostPhoto = false;
            isTakingPostVideo = true;

            if (takeVideoFromCameraActionSheet == null && mVideoUri == null) {

                takeVideoFromCameraActionSheet = ActionSheet.createBuilder(getContext(), getChildFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_video))
                        .setCancelableOnTouchOutside(true)
                        .setListener(FeedFragment.this)
                        .show();
            } else {
                if (mVideoUri == null) {
                    takeVideoFromCameraActionSheet = ActionSheet.createBuilder(getContext(), getChildFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_video))
                            .setCancelableOnTouchOutside(true)
                            .setListener(FeedFragment.this)
                            .show();
                } else {
                    takeVideoFromCameraActionSheet = ActionSheet.createBuilder(getContext(), getChildFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_replace_video),
                                    getResources().getString(R.string.action_sheet_remove_video))
                            .setCancelableOnTouchOutside(true)
                            .setImageVisiblity(true)
                            .setImageBitmap(mVideoThumbnail)
                            .setListener(FeedFragment.this)
                            .show();
                }
            }
        }
    };


    public FeedFragment() {

    }

    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        this.rootView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_feed, container, false);

            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            mRecyclerView = (PullToRefreshRecyclerView) rootView.findViewById(R.id.list_feed);
            linearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.getRefreshableView().setLayoutManager(linearLayoutManager);

            adapter = new FeedListAdapter(getActivity(), this);

            etSearch = (EditText) rootView.findViewById(R.id.etSearch);
            etSearch.clearFocus();

            ivBell = (ImageView) rootView.findViewById(R.id.ivBell);
            ivFilter = (ImageView) rootView.findViewById(R.id.ivFilter);

            expandFilter = (ExpandableLayout) rootView.findViewById(R.id.expandable_filter);
            filterArea = (LinearLayout) rootView.findViewById(R.id.filter_layout);
            expandFilter.setVisibility(View.GONE);

            //Layout viewId
            queryFixed = (LinearLayout) rootView.findViewById(R.id.query_fixed);
            queryList = (LinearLayout) rootView.findViewById(R.id.query_list);
            queryLayout = (LinearLayout) rootView.findViewById(R.id.query_layout);
            statusLayout = (LinearLayout) rootView.findViewById(R.id.status_layout);
            collaseLayout = (LinearLayout) rootView.findViewById(R.id.collapse_layout);
            collaseLayout.setVisibility(View.GONE);

            imgPhotoUpload = (ImageView) rootView.findViewById(R.id.img_photoupload);
            imgVideoUpload = (ImageView) rootView.findViewById(R.id.img_videoupload);

            btnQueryNext = (Button) rootView.findViewById(R.id.btnQuestNext);
            btnFilterApply = (Button) rootView.findViewById(R.id.ibApply);
            btnFilterApply.setOnClickListener(this);

            queryFixed.setVisibility(View.VISIBLE);
            queryFixed.setClickable(true);
            queryList.setVisibility(View.GONE);
            bFadeFixed = true;

            sgFilterLocation = (SegmentedButtonGroup) rootView.findViewById(R.id.sg_location);
            sgFilterAnswer = (SegmentedButtonGroup) rootView.findViewById(R.id.sg_answers);
            sgFilterOrder = (SegmentedButtonGroup) rootView.findViewById(R.id.sg_orders);

            /*Blur ProgressBar layout*/
            progLayout = (RelativeLayout) rootView.findViewById(R.id.progress_layout);
            progLayout.setVisibility(View.GONE);
            tvProgTitle = (TextView) rootView.findViewById(R.id.txt_progtitle);
            tvProgSubTitle = (TextView) rootView.findViewById(R.id.txt_progwaiting);
            pvUploadbar = (ProgressBar) rootView.findViewById(R.id.progress_uploading);
            pvUploadbar.setMax(100);
            btnProgCancel = (Button) rootView.findViewById(R.id.btn_progcancel);

            initVariables();
            initEvent();

            mPager = (ViewPager) rootView.findViewById(R.id.questPager);
            mFrameLayout = (ViewGroup) rootView.findViewById(R.id.pagesContainer);

            checkAccountType();
            initViewPager();
            selectData.clear();

            mRecyclerView.getRefreshableView().setHasFixedSize(true);
            mRecyclerView.getRefreshableView().setAdapter(adapter);
            mRecyclerView.setScrollingWhileRefreshingEnabled(true);
            mRecyclerView.setMode(PullToRefreshBase.Mode.BOTH);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPager.setAdapter(mDiscussAdapter);
                    mPager.setOffscreenPageLimit(3);
                    mPager.setCurrentItem(0);

                    LoopingCirclePageIndicator circlePageIndicator = new LoopingCirclePageIndicator(getActivity());
                    circlePageIndicator.setViewPager(mPager);
                    circlePageIndicator.setStrokeColor(Color.parseColor("#f0f0f0"));
                    //circlePageIndicator.setBackgroundColor();
                    mFrameLayout.addView(circlePageIndicator);
                    reloadData(true);
                }
            }, 500);
        } else {

        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivBell:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
                        .replace(R.id.feed_root_frame, FeedNotificationFragment.newInstance("", ""), "Notification")
                        .addToBackStack(null)
                        .commit();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.ivFilter:
                if (!setFiltered) {
                    setFiltered = true;
                    expandFilter.setVisibility(View.VISIBLE);
                    expandFilter.expand();
                } else {
                    setFiltered = false;
                    expandFilter.collapse();
                }
                break;
            case R.id.ibApply:
                loadCount = 0;
                expandFilter.collapse();
                reloadData(true);
                break;
            case R.id.btnQuestNext: {
                int mCurrPos = mPager.getCurrentItem();

                if (!isVerifyFilled(mCurrPos))
                    return;

                if (btnQueryNext.getText().toString().equals("Post")) {
                    createDiscussPost();
                }

                if (mCurrPos != 2)
                    mPager.setCurrentItem(mCurrPos + 1);
            }
            break;
            case R.id.btn_progcancel:
                pvUploadbar.setProgress(0);
                progLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(CustomPushReceiver.intentAction));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
    }

    private void checkAccountType()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) return;
                ParseUser user = (ParseUser) object;
                boolean isTherapist = user.getBoolean("isTherapist");
                if (isTherapist) {
                    statusLayout.setBackgroundResource(R.color.colorPrimary);
                    queryLayout.setVisibility(View.GONE);
                    mPager.setVisibility(View.GONE);
                    mFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initVariables() {
        feedFilter = FeedFilter.FeedFilterLatest;
        feedType = FeedType.FeedTypeQuestions;
        strContent = "";
        strHowlong = "";
        strWorst = "";

    }


    private void initEvent() {
        mRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadCount = 0;
                reloadData(false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadCount++;
                reloadData(false);
            }
        });

        queryFixed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bFadeFixed = false;
                expand(collaseLayout);
                fadeInOut(bFadeFixed);
            }
        });


        ivBell.setOnClickListener(this);
        ivFilter.setOnClickListener(this);

        mRecyclerView.getRefreshableView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (bFadeFixed == false) {
                    bFadeFixed = true;
                    collapse(collaseLayout);
                    fadeInOut(bFadeFixed);
                    mCurrIndex = 0;
                    mPager.setCurrentItem(0);
                    btnQueryNext.setText("Next");
                }
                return false;
            }
        });

        etSearch.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ((MainActivity) getActivity()).hideKeyboardFrom();

                    keyword = etSearch.getText().toString();
                    if (keyword.equals("")) {
                        return false;
                    }
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.feed_root_frame, FeedSearchFragment.newInstance(keyword), "Search")
                            .addToBackStack(null)
                            .commit();
                    getActivity().invalidateOptionsMenu();
                    return true;
                }
                return false;
            }
        });

        btnQueryNext.setOnClickListener(this);

        imgVideoUpload.setOnClickListener(snapPostVideoListener);
        imgPhotoUpload.setOnClickListener(snapPhotoClickListener);


        sgFilterLocation.setOnClickedButtonListener(new SegmentedButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(int position) {
                fLocation = position + 1;
            }
        });

        sgFilterAnswer.setOnClickedButtonListener(new SegmentedButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(int position) {
                fAnswer = position + 1;
            }
        });

        sgFilterOrder.setOnClickedButtonListener(new SegmentedButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(int position) {
                fOrder = position + 1;
            }
        });
    }

    private void initViewPager() {
        mDiscussAdapter = new DiscussPageAdapter(getActivity(), 3);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2)
                    btnQueryNext.setText("Post");
                else
                    btnQueryNext.setText("Next");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void createDiscussPost() {
        EditText firstQuery = (EditText) mPager.findViewWithTag("edt" + 0);
        EditText secondQuery = (EditText) mPager.findViewWithTag("edt" + 1);
        EditText thirdQuery = (EditText) mPager.findViewWithTag("edt" + 2);

        strContent = firstQuery.getText().toString();
        strHowlong = secondQuery.getText().toString();
        strWorst = thirdQuery.getText().toString();

        ParseObject postObj = new ParseObject("Posts");
        postObj.put("owner", ParseUser.getCurrentUser());
        postObj.put("content", strContent);
        postObj.put("howLong", strHowlong);
        postObj.put("worst", strWorst);

        btnQueryNext.setEnabled(false);
        btnQueryNext.setAlpha(0.5f);

        ParseMediaUpload mediaObj = new ParseMediaUpload(getContext(), postObj);
        mediaObj.initializeParseMedia(mImageViewUri, mVideoUri, "image", "video");

        mediaObj.saveInBackgroundWithMedia(new SaveMediaCallBack() {
            @Override
            public void onSuccess(Object object) {
                btnQueryNext.setEnabled(true);
                btnQueryNext.setAlpha(1.0f);

                firstQuery.setText("");
                secondQuery.setText("");
                thirdQuery.setText("");

                loadCount = 0;
                reloadData(true);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private boolean isVerifyFilled(int position) {
        EditText firstQuery = (EditText) mPager.findViewWithTag("edt" + 0);
        if (firstQuery == null || firstQuery.getText().toString().equals("")) {
            MyApp.getInstance().alertDisplayer("Error", getString(R.string.alert_fill_first_query));
            return false;
        }

        EditText secondQuery = (EditText) mPager.findViewWithTag("edt" + 1);
        if (position >= 1) {
            if (secondQuery == null || secondQuery.getText().toString().equals("")) {
                MyApp.getInstance().alertDisplayer("Error", getString(R.string.alert_fill_miss_information));
                return false;
            }
        }

        EditText thirdQuery = (EditText) mPager.findViewWithTag("edt" + 2);
        if (position == 2) {
            if (thirdQuery == null || thirdQuery.getText().toString().equals("")) {
                MyApp.getInstance().alertDisplayer("Error", getString(R.string.alert_fill_miss_information));
                return false;
            }
        }

        return true;
    }


    private Feed GetDataFromParseObject(ParseObject object, int position) {
        ParseUser pUser = object.getParseUser("owner");
        Feed newItem = new Feed();
        newItem.setName(pUser.getString("name"));
        long createTime = object.getCreatedAt().getTime();
        String timeago = TimeAgo.using(createTime);
        newItem.setDate(timeago);
        newItem.setContent(object.getString("content"));
        newItem.setHowlong(object.getString("howLong"));
        newItem.setWorst(object.getString("worst"));
        newItem.setpAnswer(object.getParseObject("acceptedComment"));
        newItem.setpUser(pUser);
        newItem.setOrigData(object);
        newItem.setId(position + 500);
        return newItem;

    }

    @Override
    public void goToPostFragment(Feed item) {
        MyApp.getInstance().setPostFeed(item);
        MyApp.getInstance().setFromParent(PostPageFrom.fromFeedPage);

        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.feed_root_frame, new FeedPostFragment(), "Post")
                .addToBackStack(null)
                .commit();
        getActivity().invalidateOptionsMenu();

    }

    public static void expand(final LinearLayout v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    v.requestLayout();
                } else {
                    v.getLayoutParams().height = (int) (targetHeight * interpolatedTime);
                    v.requestLayout();
                }

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(400);
        v.startAnimation(a);
    }

    public static void collapse(final LinearLayout v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(400);
        v.startAnimation(a);
    }

    public void fadeInOut(boolean isShrink) {
        if (!isShrink) {
            queryList.animate()
                    .alpha(1.0f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            queryList.setVisibility(View.VISIBLE);
                        }
                    });
            queryFixed.animate()
                    .alpha(0.0f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            queryFixed.setVisibility(View.GONE);
                        }
                    });

            TranslateAnimation anim = new TranslateAnimation(150, 0, 0, 0); //first 0 is start point, 150 is end point horizontal
            anim.setDuration(500); // 1000 ms = 1second
            btnQueryNext.startAnimation(anim);
            anim.setFillAfter(true);

        } else {
            queryList.animate()
                    .alpha(0.0f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            queryList.setVisibility(View.GONE);
                        }
                    });
            queryFixed.animate()
                    .alpha(1.0f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            queryFixed.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void reloadData(boolean isDialogShow) {
        if (loadCount > 0) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
            query.setLimit(itemsPerPage);
            query.include("owner");
            query.include("acceptedComment");
            query.orderByDescending("createdAt");

            if (fOrder == 1)
                query.orderByDescending("createdAt");
            else if (fOrder == 2)
                query.orderByAscending("createdAt");


            if (loadCount > 0) {
                query.setSkip(itemsPerPage * loadCount);

                if (isDialogShow)
                    showDialog();

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e != null || objects == null || objects.size() == 0) {
                            mRecyclerView.onRefreshComplete();
                            if (isDialogShow)
                                hideDialog();
                            return;
                        }

                        mRecyclerView.onRefreshComplete();
                        ArrayList<Feed> addItems = new ArrayList<>();

                        for (int i = 0; i < objects.size(); i++) {
                            Feed item = GetDataFromParseObject(objects.get(i), i);
                            addItems.add(item);
                        }

                        int startPos = selectData.size();
                        selectData.addAll(addItems);
                        adapter.setUpdatePosition(startPos);
                        adapter.setListItems(selectData);

                        if (isDialogShow)
                            showDialog();
                        scrollToLastPosition(300);

                    }
                });
            }

            return;
        }

        if (loadCount == 0) {
            //Proceed As Normal
            if (getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (selectData != null) {
                        selectData.clear();
                    }
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
                    query.setLimit(itemsPerPage);
                    query.include("owner");
                    query.include("acceptedComment");
                    query.orderByDescending("createdAt");
                    if (fOrder == 1)
                        query.orderByDescending("createdAt");
                    else if (fOrder == 2)
                        query.orderByAscending("createdAt");

                    if (loadCount > 0)
                        query.setSkip(itemsPerPage * loadCount);

                    if (isDialogShow)
                        showDialog();

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e != null || objects == null || objects.size() == 0) {
                                mRecyclerView.onRefreshComplete();
                                if (isDialogShow)
                                    hideDialog();
                                return;
                            }

                            mRecyclerView.onRefreshComplete();

                            //mScrollListener.setLoading(false);

                            for (int i = 0; i < objects.size(); i++) {
                                Feed item = GetDataFromParseObject(objects.get(i), i);
                                selectData.add(item);
                            }

                            adapter.setUpdatePosition(0);
                            adapter.setListItems(selectData);
                            if (isDialogShow)
                                hideDialog();
                            scrollToTopPosition(300);


                        }
                    });
                }
            });

        }
    }

    private void scrollToTopPosition(int delayTimeMills) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null)
                    mRecyclerView.getRefreshableView().smoothScrollToPosition(0);

            }
        }, delayTimeMills);
    }

    private void scrollToLastPosition(int delayTimeMills) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null)
                    mRecyclerView.getRefreshableView().smoothScrollToPosition(adapter.getItemCount() - 1);

            }
        }, delayTimeMills);
    }

    public Bitmap getThumbnailFromVideoUri(Uri videoUri) {
        Bitmap thumb = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getContext(), videoUri);
        try {
            thumb = retriever.getFrameAtTime(1000);
        } catch (Exception e) {
            System.out.println("Exception trying to play file subset");
        }
        return thumb;
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            if (isTakingPostPhoto == true) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("image/*");
                    getActivity().startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
                }
            } else if (isTakingPostVideo == true) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("video/*");
                    getActivity().startActivityForResult(i, TAKE_VIDEO_FROM_GALLERY);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
                }
            }

        } else if (index == 1) {
            if (isTakingPostPhoto == true) {
                mImageThumbnail = null;
                mImageViewUri = null;
                imgPhotoUpload.setImageResource(R.drawable.misc_attach);
                imgPhotoUpload.invalidate();
            } else if (isTakingPostVideo == true) {
                mVideoThumbnail = null;
                mVideoUri = null;
                imgVideoUpload.setImageResource(R.drawable.misc_attach);
                imgVideoUpload.invalidate();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ActivityResultBus.getInstance().register(mActivityResultSubscriber);
    }

    @Override
    public void onStop() {
        super.onStop();
        ActivityResultBus.getInstance().unregister(mActivityResultSubscriber);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        //mediaTask.cancel(true);
    }

    private Object mActivityResultSubscriber = new Object() {
        @Subscribe
        public void onActivityResultReceived(ActivityResultEvent event) {
            int requestCode = event.getRequestCode();
            int resultCode = event.getResultCode();
            Intent data = event.getData();
            onActivityResult(requestCode, resultCode, data);
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Don't forget to check requestCode before continuing your job
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO_FROM_GALLERY:
                    if (data != null) {
                        mImageViewUri = data.getData();
                        try {
                            mImageThumbnail = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageViewUri);
                            imgPhotoUpload.setImageBitmap(mImageThumbnail);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TAKE_VIDEO_FROM_GALLERY:
                    if (data != null) {
                        mVideoUri = data.getData();
                        mVideoThumbnail = getThumbnailFromVideoUri(mVideoUri);
                        imgVideoUpload.setImageBitmap(mVideoThumbnail);
                    }
                    break;
            }
        }
    }



    public class ParseMediaUpload {

        private ParseMedia pMedia;
        private Context mContext;
        private ParseObject _original;

        public ParseMediaUpload() {}

        public ParseMediaUpload(Context context, ParseObject data) {
            this._original = data;
            this.mContext = context;
        }

        public void initializeParseMedia(Uri _uri1, Uri _uri2, String _imgKey, String _vidKey)
        {
            pMedia = new ParseMedia();
            pMedia.setImageKey(_imgKey);
            pMedia.setVideoKey(_vidKey);
            pMedia.setByteImage(convertImageToBytes(_uri1));
            pMedia.setByteVideo(convertVideoToBytes(_uri2));
        }

        private byte[] convertImageToBytes(Uri uri){
            if (uri == null)
                return null;

            byte[] data = null;
            try {
                ContentResolver cr = mContext.getContentResolver();
                InputStream inputStream = cr.openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                data = baos.toByteArray();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return data;
        }

        private byte[] convertVideoToBytes(Uri uri){
            if (uri == null)
                return null;

            byte[] videoBytes = null;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                FileInputStream fis = new FileInputStream(new File(getRealPathFromURI(mContext, uri)));

                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = fis.read(buf)))
                    baos.write(buf, 0, n);

                videoBytes = baos.toByteArray();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return videoBytes;
        }

        private String getRealPathFromURI(Context context, Uri contentUri) {
            Cursor cursor = null;
            try {
                String[] proj = { MediaStore.Video.Media.DATA };
                cursor = context.getContentResolver().query(contentUri, proj, null,
                        null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        public AsyncTask<MediaWrapper, Void, MediaWrapper> saveInBackgroundWithMedia(SaveMediaCallBack callback) {
            MediaWrapper result = new MediaWrapper();
            result.media = pMedia;
            result.comment = _original;

            mediaTask = new MediaUploadTask(mContext, callback);
            mediaTask.execute(result);
            return mediaTask;
        }

    }

    /*
    public class loadDataTask extends AsyncTask<Void, HashMap<String, Object>, Void> {
        @Override
        protected void onPreExecute()
        {
            if (isDialogShow)
                showDialog();
        }

        @Override
        protected Void doInBackground(Void... datas) {
            if (loadCount > 0) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
                query.setLimit(itemsPerPage);
                query.include("owner");
                query.include("acceptedComment");
                query.orderByDescending("createdAt");

                if (fOrder == 1)
                    query.orderByDescending("createdAt");
                else if (fOrder == 2)
                    query.orderByAscending("createdAt");


                if (loadCount > 0) {
                    query.setSkip(itemsPerPage * loadCount);

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e != null || objects == null || objects.size() == 0) {
                                HashMap<String, Object> mObj = new HashMap<>();
                                mObj.put("loadmore", true);
                                mObj.put("objects", null);
                                publishProgress(mObj);
                                return;
                            }

                            HashMap<String, Object> mObj = new HashMap<>();
                            mObj.put("loadmore", true);
                            mObj.put("objects", objects);
                            publishProgress(mObj);
                        }
                    });
                }

            }

            if (loadCount == 0)
            {
                if (selectData != null) {
                    selectData.clear();
                }
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
                query.setLimit(itemsPerPage);
                query.include("owner");
                query.include("acceptedComment");
                query.orderByDescending("createdAt");
                if (fOrder == 1)
                    query.orderByDescending("createdAt");
                else if (fOrder == 2)
                    query.orderByAscending("createdAt");

                if (loadCount > 0)
                    query.setSkip(itemsPerPage * loadCount);

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e != null || objects == null || objects.size() == 0) {
                            HashMap<String, Object> mObj = new HashMap<>();
                            mObj.put("loadmore", false);
                            mObj.put("objects", null);
                            publishProgress(mObj);
                            return;
                        }

                        HashMap<String, Object> mObj = new HashMap<>();
                        mObj.put("loadmore", false);
                        mObj.put("objects", objects);
                        publishProgress(mObj);
                    }
                });

            }

            return  null;
        }


        @Override
        protected void onPostExecute(Void result) {

            String sData = "abcde";
        }

        @Override
        protected void onProgressUpdate(HashMap<String, Object>... values) {
            if (values == null)
                return;
            HashMap<String, Object> mObj = values[0];
            boolean isLoadMore = (boolean)mObj.get("loadmore");
            List<ParseObject> objects = (List<ParseObject>)mObj.get("objects");
             if (isLoadMore)
            {
                if (objects == null)
                {
                    mRecyclerView.onRefreshComplete();
                    if (isDialogShow)
                        hideDialog();
                }
                else if (objects != null)
                {
                    mRecyclerView.onRefreshComplete();
                    ArrayList<Feed> addItems = new ArrayList<>();

                    for (int i=0; i<objects.size(); i++)
                    {
                        Feed item = GetDataFromParseObject(objects.get(i), i);
                        addItems.add(item);
                    }

                    int startPos = selectData.size() + 1;
                    selectData.addAll(addItems);
                    adapter.setListItems(selectData);
                    adapter.setUpdatePosition(startPos);

                    if (isDialogShow)
                        hideDialog();
                    scrollToLastPosition(300);
                }
            } else
            {
                if (objects == null)
                {
                    mRecyclerView.onRefreshComplete();
                    if (isDialogShow)
                        hideDialog();
                } else if (objects != null)
                {
                    mRecyclerView.onRefreshComplete();

                    for (int i=0; i<objects.size(); i++)
                    {
                        Feed item = GetDataFromParseObject(objects.get(i), i);
                        selectData.add(item);
                    }

                    adapter.setUpdatePosition(0);
                    adapter.setListItems(selectData);
                    if (isDialogShow)
                        hideDialog();
                    scrollToTopPosition(300);
                }
            }

        }
    }
    */

    public class MediaWrapper {
        ParseMedia media;
        ParseObject comment;
    }

    public class MediaUploadTask extends AsyncTask<MediaWrapper, Void, MediaWrapper> {

        private SaveMediaCallBack<String> mCallBack;
        private Context mContext;
        public Exception mException;


        public MediaUploadTask(Context context, SaveMediaCallBack callback) {
            mCallBack = callback;
            mContext = context;
        }

        @Override
        protected MediaWrapper doInBackground(MediaWrapper... params) {
            MediaWrapper data = params[0];
            return data;
        }

        @Override
        protected void onPostExecute(MediaWrapper result) {
            final ParseMedia pMedia = result.media;
            final ParseObject _original = result.comment;

            try {
                if (pMedia.getByteImage() == null && pMedia.getByteVideo() == null)
                {
                    progLayout.setVisibility(View.VISIBLE);
                    tvProgTitle.setText("Uploading Post");
                    tvProgSubTitle.setText("please wait...");
                    pvUploadbar.setProgress(0);

                    _original.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            pvUploadbar.setProgress(100);
                            progLayout.setVisibility(View.GONE);
                            mCallBack.onSuccess("success");
                            return ;
                        }
                    });
                }
                else  if (pMedia.getByteImage() != null)
                {
                    final ParseFile imageFile = new ParseFile(pMedia.getByteImage());
                    progLayout.setVisibility(View.VISIBLE);
                    tvProgTitle.setText("Uploading Image");
                    tvProgSubTitle.setText("Image Uploading.. please wait");
                    pvUploadbar.setProgress(0);

                    imageFile.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            // Handle success or failure here ...
                            if (e == null)
                            {
                                _original.put(pMedia.getImageKey(), imageFile);
                                if (pMedia.getByteVideo() == null)
                                {
                                    tvProgTitle.setText("Uploading Post");
                                    tvProgSubTitle.setText("Please wait...");

                                    _original.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            progLayout.setVisibility(View.GONE);
                                            mCallBack.onSuccess("success");
                                            return ;
                                        }
                                    });

                                }
                                else
                                {
                                    tvProgTitle.setText("Uploading Video");
                                    tvProgSubTitle.setText("Video Uploading.. please wait");
                                    pvUploadbar.setProgress(0);

                                    final ParseFile videoFile = new ParseFile("video.mp4", pMedia.getByteVideo(), "video/mp4");
                                    videoFile.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            // Handle success or failure here ...
                                            if (e == null) {
                                                _original.put(pMedia.getVideoKey(), videoFile);
                                                tvProgTitle.setText("Uploading Post");
                                                tvProgSubTitle.setText("Please wait...");

                                                _original.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        progLayout.setVisibility(View.GONE);
                                                        mCallBack.onSuccess("success");
                                                        return;
                                                    }
                                                });
                                            }
                                        }
                                    }, new ProgressCallback() {
                                        @Override
                                        public void done(Integer percentDone) {
                                            pvUploadbar.setProgress(percentDone);
                                        }
                                    });
                                }

                            }

                        }
                    }, new ProgressCallback() {
                        public void done(Integer percentDone) {
                            // Update your progress spinner here. percentDone will be between 0 and 100.
                            pvUploadbar.setProgress(percentDone);
                        }
                    });
                }
                else if (pMedia.getByteVideo() != null)
                {
                    final ParseFile videoFile = new ParseFile("video.mp4", pMedia.getByteVideo(), "video/mp4");
                    progLayout.setVisibility(View.VISIBLE);
                    tvProgTitle.setText("Uploading Video");
                    tvProgSubTitle.setText("Video Uploading.. please wait");
                    pvUploadbar.setProgress(0);

                    videoFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            // Handle success or failure here ...
                            if (e == null) {
                                _original.put(pMedia.getVideoKey(), videoFile);
                                if (pMedia.getByteImage() != null)
                                {
                                    tvProgTitle.setText("Uploading Image");
                                    tvProgSubTitle.setText("Image Uploading.. please wait");
                                    pvUploadbar.setProgress(0);

                                    final ParseFile imageFile = new ParseFile(pMedia.getByteImage());
                                    imageFile.saveInBackground(new SaveCallback() {
                                        public void done(ParseException e) {
                                            // Handle success or failure here ...
                                            if (e == null) {
                                                _original.put(pMedia.getImageKey(), imageFile);
                                                tvProgTitle.setText("Uploading Post");
                                                tvProgSubTitle.setText("Please wait...");

                                                _original.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        progLayout.setVisibility(View.GONE);
                                                        mCallBack.onSuccess("success");
                                                    }
                                                });
                                            }
                                        }
                                    }, new ProgressCallback() {
                                        @Override
                                        public void done(Integer percentDone) {
                                            pvUploadbar.setProgress(percentDone);
                                        }
                                    });
                                }
                                else
                                {

                                    tvProgTitle.setText("Uploading Post");
                                    tvProgSubTitle.setText("Please wait...");

                                    _original.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            progLayout.setVisibility(View.GONE);
                                            mCallBack.onSuccess("success");
                                        }
                                    });
                                }
                            }
                        }
                    }, new ProgressCallback() {
                        @Override
                        public void done(Integer percentDone) {
                            pvUploadbar.setProgress(percentDone);
                        }
                    });
                }


            } catch (Exception e) {
                mCallBack.onFailure(e);
            }
        }
    }

}
