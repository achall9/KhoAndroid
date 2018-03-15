package com.kholabs.khoand.Fragment;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kholabs.khoand.Activity.CropImageActivity;
import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.CustomView.ActionSheet;
import com.kholabs.khoand.CustomView.PopupWindow.PhotoFullPopupWindow;
import com.kholabs.khoand.CustomView.PullScrollView.PullScrollView;
import com.kholabs.khoand.CustomView.ViewPager.NonSwipeableViewPager;
import com.kholabs.khoand.Fragment.ChildProfileFragment.ChildInfoFragment;
import com.kholabs.khoand.Fragment.ChildProfileFragment.ChildPostFragment;
import com.kholabs.khoand.Fragment.ChildProfileFragment.ChildRewardFragment;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.ProfileHeader.behavior.widget.CircleImageView;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import co.ceryle.segmentedbutton.SegmentedButtonGroup;

import static android.support.v4.content.ContextCompat.checkSelfPermission;


public class InstagramProfileFragment extends Fragment implements ConstValues, View.OnClickListener,
        MainActivity.onKeyBackPressedListener, ActionSheet.ActionSheetListener {

    private static final String ARG_USER = "user";
    private static final String ARG_FROM = "from";
    private final int TAKE_PHOTO_FROM_GALLERY = 101;
    private final int TAKE_PHOTO_FROM_CAMERA = 105;
    private final int PIC_CROP = 106;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration = 1000;

    private View rootView;
    private NonSwipeableViewPager segmentPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /* Inflater Controls */
    private PullScrollView mScrollView;
    private RelativeLayout mHeaderLayout;

    private ImageView ivAvatarBackground;
    private CircleImageView ucAvatar;

    private SegmentedButtonGroup sgButtons;
    private LinearLayout llBack, llSupport, llQuery, llComment, rlBirthday;
    private LinearLayout llSetting, llEdit;
    private Button btMessage;
    private TextView tvSupportCnt, tvPostCnt, tvResponseCnt, tvBirthday;
    private TextView tvProfileName, tvAccountType;


    /* End */

    /* variables & parameters */
    private ParseUser inc_user;
    private Bitmap mAvatarBmp;
    private int fromParent;

    private ActionSheet takePhotoActionSheet = null;
    private Uri mImageViewUri = null;
    private Bitmap mImageThumbnail = null;
    private String tempPhotoUriPath = "";
    private boolean isDataLoaded = false;

    private GetPostCountTask myTask = null;

    private View.OnClickListener snapPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().setTheme(R.style.ActionSheetStyleIOS7);
            takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                    .setCancelButtonTitle(getResources().getString(R.string.cancel))
                    .setOtherButtonTitles(getResources().getString(R.string.action_sheet_choose_from_library),
                            getResources().getString(R.string.action_sheet_take_photo),
                            getResources().getString(R.string.action_sheet_remove))
                    .setCancelableOnTouchOutside(true)
                    .setListener(InstagramProfileFragment.this)
                    .show();

        }
    };


    public InstagramProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.inc_user = MyApp.getInstance().getShareUser();
        this.fromParent = MyApp.getInstance().getFromParent();
        this.rootView = null;
    }

    public static InstagramProfileFragment newInstance(String param1, String param2) {
        InstagramProfileFragment fragment = new InstagramProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_instagram_profile, container, false);
            ((MainActivity) getActivity()).setOnkeyBackPressedListener(this);

            segmentPager = (NonSwipeableViewPager) rootView.findViewById(R.id.viewPager);
            sgButtons = (SegmentedButtonGroup) rootView.findViewById(R.id.sgGroup);

            ivAvatarBackground = (ImageView) rootView.findViewById(R.id.ivAvatar_Overlay);

            ucAvatar = (CircleImageView) rootView.findViewById(R.id.uc_avater);

            llBack = (LinearLayout) rootView.findViewById(R.id.llBack);
            llSetting = (LinearLayout) rootView.findViewById(R.id.llSetting);
            btMessage = (Button) rootView.findViewById(R.id.ibMessage);
            llEdit = (LinearLayout) rootView.findViewById(R.id.llEdit);

            tvSupportCnt = (TextView) rootView.findViewById(R.id.tvSupportCnt);
            tvPostCnt = (TextView) rootView.findViewById(R.id.tvPostCnt);
            tvResponseCnt = (TextView) rootView.findViewById(R.id.tvResponseCnt);
            tvBirthday = (TextView) rootView.findViewById(R.id.tvBirthday);
            tvProfileName = (TextView) rootView.findViewById(R.id.tvProfileName);
            tvAccountType = (TextView) rootView.findViewById(R.id.tvAccountType);

            llSupport = (LinearLayout) rootView.findViewById(R.id.llSupport);
            llQuery = (LinearLayout) rootView.findViewById(R.id.llPost);
            llComment = (LinearLayout) rootView.findViewById(R.id.llResponse);
            rlBirthday = (LinearLayout) rootView.findViewById(R.id.rlBirthday);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initTab();
                    initViewPager();
                    initEventandVisible();
                    loadData();
                }
            }, 500);
        }

        return rootView;
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0) {
            int permissionCheck = checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                getActivity().startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
                //Intent i = new Intent(rootView.getContext(), TherapistSignupActivity.class);
                //getActivity().startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
            }
        } else if (index == 1) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x01);
                } else {
                    cameraIntent();

                }
            } else {
                cameraIntent();
            }
        } else if (index == 2) {
            mImageThumbnail = null;
            mImageViewUri = null;
            ucAvatar.setImageResource(R.drawable.blankprofile);
            ucAvatar.invalidate();

            mAvatarBmp = null;
            ivAvatarBackground.setImageResource(R.drawable.blankprofile);
            ivAvatarBackground.invalidate();
            removePhotoFromProfile();
        }
    }

    private void loadData()
    {
        loadProfile();
        new GetPostCountTask().execute();
    }

    private void initTab() {
        sgButtons.setOnClickedButtonListener(new SegmentedButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(int position) {
                segmentPager.setCurrentItem(position);
            }
        });

        sgButtons.setPosition(0);
    }

    private void initEventandVisible()
    {
        if (fromParent == ProfileFrom.fromFeedPage)
        {
            llBack.setVisibility(View.VISIBLE);
            btMessage.setVisibility(View.VISIBLE);
            llSetting.setVisibility(View.GONE);
            llEdit.setVisibility(View.GONE);
        } else
        {
            llBack.setVisibility(View.GONE);
            btMessage.setVisibility(View.GONE);
            llSetting.setVisibility(View.VISIBLE);
            llEdit.setVisibility(View.VISIBLE);
        }

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                //fragmentManager.popBackStack(FragmentPopKeys.FEEDSEARCH, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
            }
        });
        llSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isAvatarLoaded = false;
                if (mAvatarBmp != null)
                    isAvatarLoaded = true;

                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.profile_root_frame, ProfileSettingFragment.newInstance(isAvatarLoaded), "Settings")
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });

        btMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromParent == ProfileFrom.fromMessage)
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                    ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
                }
                else
                {
                    MainActivity activity = (MainActivity)getActivity();
                    activity.setPagerFragment(1);
                    MyApp.getInstance().setShareUser(inc_user);
                }
            }
        });

        llEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.profile_root_frame, new ProfileEditFragment(), "Edit")
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
        });

        llSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.getInstance().setShareUser(inc_user);
                MyApp.getInstance().setFromParent(fromParent);

                if (fromParent == ProfileFrom.fromProfile)
                {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.profile_root_frame, new SupportingFragment(), "Support")
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
                else if (fromParent == ProfileFrom.fromFeedPage)
                {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.feed_root_frame, new SupportingFragment(), "Support")
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
            }
        });

        llQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.getInstance().setShareUser(inc_user);
                MyApp.getInstance().setFromParent(fromParent);

                if (fromParent == ProfileFrom.fromProfile)
                {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.profile_root_frame, new QuestionFragment(), "Questions")
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
                else if (fromParent == ProfileFrom.fromFeedPage)
                {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.feed_root_frame, new QuestionFragment(), "Questions")
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
            }
        });

        llComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.getInstance().setShareUser(inc_user);
                MyApp.getInstance().setFromParent(fromParent);

                if (fromParent == ProfileFrom.fromProfile)
                {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.profile_root_frame, new CommentFragment(), "Comments")
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
                else if (fromParent == ProfileFrom.fromFeedPage)
                {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.feed_root_frame, new CommentFragment(), "Comments")
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                }
            }
        });

        if (fromParent == ProfileFrom.fromProfile)
        {
            ucAvatar.setOnClickListener(snapPhotoClickListener);
        }
        else
        {
            ucAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new PhotoFullPopupWindow(getContext(), R.layout.popup_photo_full, view, mAvatarBmp);
                }
            });
        }
    }

    private void initViewPager()
    {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        //new setAdapterTask().execute();

        segmentPager.setAdapter(mSectionsPagerAdapter);
        segmentPager.setOffscreenPageLimit(3);
        segmentPager.setCurrentItem(0);

        segmentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                sgButtons.setPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        /*(mImageViewUri = Uri.fromFile(new File(MyUtils.getAppDataFolder("UserProfile") +
                String.valueOf(System.currentTimeMillis()) + ".jpg"));
        tempPhotoUriPath = mImageViewUri.getPath();
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageViewUri);*/
        getActivity().startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA);
    }

    private void performCrop(Bitmap sendBitmap)
    {
        Intent intent = new Intent(getActivity() , CropImageActivity.class);
        intent.putExtra("imageBitmap" , sendBitmap);
        getActivity().startActivityForResult(intent , PIC_CROP);
    }

    private void loadProfile()
    {
        GetUserInformation(inc_user);
        GetAvatarPhotoFromData(inc_user);
        MyApp.getInstance().setShareUser(inc_user);
        MyApp.getInstance().setFromParent(fromParent);
    }

    public class GetPostCountTask extends AsyncTask<Void, String, Integer>
    {

        @Override
        protected Integer doInBackground(Void... voids) {
            if (inc_user == null)
                return null;
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Supports");
            query.whereEqualTo("user", inc_user);
            query.include("post");
            query.include("post.owner");
            try {
                int supportCnt = query.count();
                publishProgress("support", Integer.toString(supportCnt));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            query = ParseQuery.getQuery("Comments");
            query.whereEqualTo("user", inc_user);
            try {
                int commentCnt = query.count();
                publishProgress("comment", Integer.toString(commentCnt));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            query = ParseQuery.getQuery("Posts");
            query.whereEqualTo("owner", inc_user);
            try {
                int postCnt = query.count();
                publishProgress("post", Integer.toString(postCnt));
            } catch (ParseException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String...queries)
        {
            String strType = queries[0];
            int countVal = Integer.valueOf(queries[1]);
            if (strType.equals("support"))
                tvSupportCnt.setText(String.valueOf(countVal));
            else if (strType.equals("comment"))
                tvResponseCnt.setText(String.valueOf(countVal));
            else if (strType.equals("post"))
                tvPostCnt.setText(String.valueOf(countVal));
        }

        @Override
        protected void onPostExecute(Integer result)
        {

        }
    }

    public void GetUserInformation(ParseUser user)
    {
        String name = user.getString("name");
        tvProfileName.setText(name);

        Date bio = user.getDate("dob");
        if (bio == null)
            rlBirthday.setVisibility(View.INVISIBLE);
        else
        {
            String day          = (String) DateFormat.format("dd",   bio); // 20
            String monthNumber  = (String) DateFormat.format("MM",   bio); // 06
            String year         = (String) DateFormat.format("yyyy", bio);

            int mYear = Integer.valueOf(year);
            int mMonth = Integer.valueOf(monthNumber);
            int mDay = Integer.valueOf(day);

            String age = getAge(mYear, mMonth, mDay);
            tvBirthday.setText(age);
        }

        boolean isType = user.getBoolean("isTherapist");
        if (isType == true)
            tvAccountType.setText("Therapist");
        else
            tvAccountType.setText("Athlete");
    }

    private String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }

    public void GetAvatarPhotoFromData(ParseUser user)
    {
        if (mAvatarBmp != null)
        {
            ivAvatarBackground.setImageBitmap(mAvatarBmp);
            ucAvatar.setImageBitmap(mAvatarBmp);

            return;
        }

        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null || object == null)
                    return;
                ParseFile pFile = object.getParseFile("avatar");

                if (pFile == null)
                    return;

                pFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e != null || data == null)
                            return;

                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        mAvatarBmp = bmp;
                        ivAvatarBackground.setImageBitmap(bmp);
                        ucAvatar.setImageBitmap(bmp);
                    }
                });

            }
        });
    }

    @Override
    public void onClick(View view) {

    }


    @Override
    public void onBack() {
        if (fromParent == ProfileFrom.fromFeedPage)
        {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.popBackStack();
            ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
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
                            mAvatarBmp = mImageThumbnail;
                            ucAvatar.setImageBitmap(mImageThumbnail);
                            ivAvatarBackground.setImageBitmap(mImageThumbnail);

                            uploadPhotoToProfile(mImageThumbnail);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TAKE_PHOTO_FROM_CAMERA:
                    if (data != null)
                    {
                        mImageThumbnail = (Bitmap)data.getExtras().get("data");
                        performCrop(mImageThumbnail);
                    }
                    break;
                case PIC_CROP:
                    if (data != null)
                    {
                        mImageThumbnail = data.getParcelableExtra("saveImageBitmap");
                        ucAvatar.setImageBitmap(mImageThumbnail);
                        ivAvatarBackground.setImageBitmap(mImageThumbnail);
                        mAvatarBmp = mImageThumbnail;
                        uploadPhotoToProfile(mImageThumbnail);
                    }

            }
        }
    }

    private byte[] convertImageToBytes(Bitmap bmp){
        if (bmp == null)
            return null;

        byte[] data = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();

        return data;
    }

    private void uploadPhotoToProfile(Bitmap bmp)
    {
        byte[] dataArr = convertImageToBytes(bmp);

        if (dataArr == null)
            return;

        ParseFile imageFile = new ParseFile("userAvatar.jpg", dataArr);

        imageFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                {
                    //inc_user.remove("avatar");
                    inc_user.put("avatar", imageFile);
                    inc_user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                }
            }
        });
    }

    private void removePhotoFromProfile()
    {
        inc_user.remove("avatar");
        inc_user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraIntent();
            } else {

            }
        } else {

        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            android.support.v4.app.Fragment fragment;
            Bundle args = new Bundle();
            switch (position) {
                case 0:
                    fragment = new ChildInfoFragment();
                    break;
                case 1:
                    fragment = new ChildPostFragment();
                    break;
                case 2:
                    fragment = new ChildRewardFragment();
                    break;
                default:
                    fragment = new ChildInfoFragment();
                    break;
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    private class setAdapterTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }
}
