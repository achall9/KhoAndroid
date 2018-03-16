package com.kholabs.khoand.Fragment;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Adapter.DropDownListAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.Common.FragmentBase;
import com.kholabs.khoand.CustomView.ActionSheet;
import com.kholabs.khoand.Dialog.AddFieldDialog;
import com.kholabs.khoand.Dialog.DateDialog;
import com.kholabs.khoand.Interface.SaveMediaCallBack;
import com.kholabs.khoand.Model.ParseMedia;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.MyUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TherapistInfoFragment extends FragmentBase implements View.OnClickListener,
        DropDownListAdapter.DropDownItemListener, AddFieldDialog.AddFieldDialogCompliant,
        MainActivity.onKeyBackPressedListener, ActionSheet.ActionSheetListener {

    private final int TAKE_PHOTO_FROM_GALLERY = 101;

    private View rootView;
    private LinearLayout llBack;
    private ImageView ivVerifyMark;
    private TextView tvVerifylabel, tvSelectSport;
    private Button btnUpload, btnSubmit;
    private EditText etDesc, etAddress, etCity, etCountry;

    private ExpandableLayout expandLayout;
    private RecyclerView mRecyclerPopup;
    private DropDownListAdapter sportsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    /* Blur Progress Layout */
    private RelativeLayout progLayout;
    private TextView tvProgTitle, tvProgSubTitle;
    private ProgressBar pvUploadbar;
    private Button btnProgCancel;

    private String existingImageURL = "";
    private ActionSheet takePhotoActionSheet = null;
    private Uri mImageViewUri = null;
    private Bitmap mImageThumbnail = null;
    private ArrayList<String> items = new ArrayList<String>();

    private ParseUser currUser;

    private boolean isRemovable = false;
    private boolean isAction = false;
    private boolean expanded;

    public TherapistInfoFragment() {
    }
    public static TherapistInfoFragment newInstance() {
        TherapistInfoFragment fragment = new TherapistInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        currUser = ParseUser.getCurrentUser();
        rootView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_therapist_info, container, false);
            ((MainActivity)getActivity()).setOnkeyBackPressedListener(this);

            llBack = (LinearLayout) rootView.findViewById(R.id.llBack);
            ivVerifyMark = (ImageView) rootView.findViewById(R.id.iv_verifyMark);
            tvVerifylabel = (TextView) rootView.findViewById(R.id.tvVerifyLabel);
            tvSelectSport = (TextView) rootView.findViewById(R.id.tvSelectSport);
            etDesc = (EditText) rootView.findViewById(R.id.et_desc);
            etAddress = (EditText) rootView.findViewById(R.id.etAddress);
            etCity = (EditText) rootView.findViewById(R.id.etCity);
            etCountry = (EditText) rootView.findViewById(R.id.etCountry);
            btnUpload = (Button) rootView.findViewById(R.id.btn_upload);
            btnSubmit = (Button) rootView.findViewById(R.id.btn_submit);

            expandLayout = (ExpandableLayout) rootView.findViewById(R.id.expandable_list);
            expandLayout.setVisibility(View.GONE);
            expandLayout.collapse();

            mRecyclerPopup = (RecyclerView) rootView.findViewById(R.id.listSports);
            mRecyclerPopup.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerPopup.setLayoutManager(mLayoutManager);
            mRecyclerPopup.scrollToPosition(0);

            /*Blur ProgressBar layout*/
            progLayout = (RelativeLayout) rootView.findViewById(R.id.progress_layout);
            progLayout.setVisibility(View.GONE);
            tvProgTitle = (TextView) rootView.findViewById(R.id.txt_progtitle);
            tvProgSubTitle = (TextView) rootView.findViewById(R.id.txt_progwaiting);
            pvUploadbar = (ProgressBar) rootView.findViewById(R.id.progress_uploading);
            pvUploadbar.setMax(100);
            btnProgCancel = (Button) rootView.findViewById(R.id.btn_progcancel);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkVerification();
                    initEventListener();
                    initializeSelectList();
                }
            }, 500);
        }

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_upload:
                actionUpload();
                break;
            case  R.id.btn_submit:
                actionSubmit();
                break;
            case R.id.llBack:
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
                break;
        }
    }

    @Override
    public void onBack() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
        ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
    }

    private void initEventListener()
    {
        btnUpload.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        llBack.setOnClickListener(this);

        etDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void checkVerification()
    {
        if (currUser == null)
            return;

        showDialog();
        currUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                hideDialog();

                if (e != null) return;
                ParseUser user = (ParseUser)object;
                boolean isVerified = user.getBoolean("verifiedTherapist");
                if (isVerified)
                {
                    ivVerifyMark.setImageResource(R.drawable.vf_verified);
                    tvVerifylabel.setText("Verified");
                }

                HashMap<String, Object> info = (HashMap<String, Object>)user.get("therapistInfo");

                if (info == null)
                    return;

                for (String key : info.keySet())
                {
                    if (key.equals("pending"))
                    {
                        String strPending = String.valueOf(info.get(key));
                        boolean isPending = Boolean.valueOf(strPending);

                        if (isPending)
                        {
                            ivVerifyMark.setImageResource(R.drawable.misc_pending);
                            tvVerifylabel.setText("Pending");
                        } else
                        {
                            ivVerifyMark.setImageResource(R.drawable.vf_verified);
                            tvVerifylabel.setText("Verified");
                        }
                    }

                    if (key.equals("imageURL")) {
                        existingImageURL = (String)info.get(key);
                        btnUpload.setText("Actions");
                    }

                    if (key.equals("extra")) {
                        String extra = (String)info.get(key);
                        etDesc.setText(extra);
                    }


                    if (key.equals("address")) {
                        String address = (String)info.get(key);
                        etAddress.setText(address);
                    }

                    if (key.equals("state")) {
                        String state = (String)info.get(key);
                        etCity.setText(state);
                    }

                    if (key.equals("country")) {
                        String country = (String)info.get(key);
                        etCountry.setText(country);
                    }

                    if (key.equals("type")) {
                        String sportsString = "Select Sports";
                        List<String> sportsArr = (List<String>)info.get(key);

                        if (sportsArr != null) {
                            sportsString = MyUtils.JoinString(sportsArr);
                            tvSelectSport.setText(sportsString);
                            if (sportsAdapter != null)
                                sportsAdapter.setCheckArray(tvSelectSport.getText().toString());
                        }
                    }
                }

            }
        });

    }

    private void actionUpload()
    {
        if (btnUpload.getText().toString().equals("Upload"))
        {
            getActivity().setTheme(R.style.ActionSheetStyleIOS7);
            isAction = false;
            if (takePhotoActionSheet == null && mImageViewUri == null) {
                isRemovable = false;
                takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_image))
                        .setCancelableOnTouchOutside(true)
                        .setListener(TherapistInfoFragment.this)
                        .show();
            } else {
                if (mImageViewUri == null) {
                    isRemovable = false;
                    takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_image))
                            .setCancelableOnTouchOutside(true)
                            .setListener(TherapistInfoFragment.this)
                            .show();
                } else {
                    isRemovable = true;
                    takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_replace_image),
                                    getResources().getString(R.string.action_sheet_remove_image))
                            .setCancelableOnTouchOutside(true)
                            .setImageVisiblity(true)
                            .setImageBitmap(mImageThumbnail)
                            .setListener(TherapistInfoFragment.this)
                            .show();
                }
            }
        } else if (btnUpload.getText().toString().equals("Actions"))
        {
            getActivity().setTheme(R.style.ActionSheetStyleIOS7);
            isAction = true;
            isRemovable = true;
            takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                    .setCancelButtonTitle(getResources().getString(R.string.cancel))
                    .setOtherButtonTitles(getResources().getString(R.string.action_sheet_remove_file))
                    .setCancelableOnTouchOutside(true)
                    .setImageVisiblity(true)
                    .setListener(TherapistInfoFragment.this)
                    .show();
        }
    }

    private void initializeSelectList()
    {
        items.add("Acupuncturists");
        items.add("Athletic Trainer");
        items.add("Chiropractor");
        items.add("Massage");
        items.add("Physical Therapist");
        items.add("Physiotherapist");
        items.add("Therapist");
        items.add("Other");
        items.add("Add Custom Field");

        sportsAdapter = new DropDownListAdapter(getContext(), items, tvSelectSport, TherapistInfoFragment.this);
        mRecyclerPopup.setAdapter(sportsAdapter);
        mRecyclerPopup.setItemAnimator(new DefaultItemAnimator());

        tvSelectSport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!expanded) {
                    expanded = true;
                    expandLayout.expand();
                } else
                {
                    expanded = false;
                    expandLayout.collapse();

                }
            }
        });
    }

    private void actionSubmit() {
        String name = currUser.getString("name");
        String phone = currUser.getString("phone");
        Date dob = currUser.getDate("dob");
        String bio = currUser.getString("bio");
        ParseFile avatar = currUser.getParseFile("avatar");

        if (name == null || name.length() == 0) {
            MyApp.getInstance().alertDisplayer("Error", "Please go back and fill out your name before you submit your therapist application");
            return;
        }

        /*
        if (phone == null || phone.length() == 0) {
            MyApp.getInstance().alertDisplayer("Error", "Missing Phone number. Please go back and fill out your number before you submit your therapist application");
            return;
        }
        */

        if (dob == null) {
            MyApp.getInstance().alertDisplayer("Error", "Missing Date Of Birth. Please go back and enter your date of birth before you submit your therapist application");
            return;
        }

        /*
        if (bio == null || bio.length() == 0) {
            MyApp.getInstance().alertDisplayer("Error", "Missing Profile Bio. Please go back and enter some information about yourself in the Bio section before submitting your therapist application");
            return;
        }
        */

        if (avatar == null) {
            MyApp.getInstance().alertDisplayer("Error", "Please add a profile picture before submitting your therapist application. To do this, go back , tap on your Profile picture and upload one before submitting your therapist application");
            return;
        }

        //Some Coding
        HashMap<String, Object> info = new HashMap<>();

        List<String> sportsArr = MyUtils.getArrayString(tvSelectSport.getText().toString());
        if (sportsArr != null && sportsArr.size() == 1)
        {
            String first = sportsArr.get(0);
            if (first.length() > 0)
                info.put("type", sportsArr);
        } else if (sportsArr != null)
            info.put("type", sportsArr);


        if (etAddress.getText().length() != 0)
            info.put("address", etAddress.getText().toString());

        if (etCity.getText().length() != 0)
            info.put("state", etCity.getText().toString());

        if (etCountry.getText().length() != 0)
            info.put("country", etCountry.getText().toString());

        if (etDesc.getText().length() != 0) {
            if (!etDesc.getText().toString().equals("Type information here..")) {
                info.put("extra", etDesc.getText().toString());
            }
        }

        info.put("pending", "true");
        currUser.put("therapistInfo", info);

        //Some coding
        if (mImageViewUri != null)
        {
            ParseMediaUpload mediaUPload = new ParseMediaUpload(getContext());
            mediaUPload.initializeParseMedia(mImageViewUri);


            mediaUPload.saveInBackgroundWithMedia(info, new SaveMediaCallBack() {
                @Override
                public void onSuccess(Object object) {
                    mImageViewUri = null;
                    mImageThumbnail = null;
                    checkVerification();
                    MyApp.getInstance().alertDisplayer("Pending", "Your Therapist application is now pending. You will be notified shortly!");
                }

                @Override
                public void onFailure(Exception e) {

                }
            });

            return;
        }
        else
        {
            showDialog();
            currUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    hideDialog();

                    checkVerification();
                    MyApp.getInstance().alertDisplayer("Pending", "Your Therapist application is now pending. You will be notified shortly!");
                }
            });
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
                            btnUpload.setText("Actions");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void addToPopup(String field) {
        items.add(items.size()-1, field);
        sportsAdapter.addItem(field);
    }

    @Override
    public void AddCustomField(int position) {
        AddFieldDialog dialog = new AddFieldDialog(TherapistInfoFragment.this);
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        dialog.show(ft, "Add Custom Field");
    }

    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {

    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0)
        {
            if (isAction)
            {
                if (existingImageURL != null) {
                    return;
                }

                currUser.remove("therapistDocument");
                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        existingImageURL = null;
                        mImageViewUri = null;
                        mImageThumbnail = null;
                        btnUpload.setText("Upload");
                    }
                });

            }
            else
            {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("image/*");
                    getActivity().startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
                }
                else
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
            }
        }
        else if (index == 1)
        {
            mImageThumbnail = null;
            mImageViewUri = null;
            btnUpload.setText("Upload");
        }

    }

    public class ParseMediaUpload {

        private ParseMedia pMedia;
        private Context mContext;
        private ParseObject _original;

        public ParseMediaUpload() {}

        public ParseMediaUpload(Context context) {
            this.mContext = context;
        }

        public void initializeParseMedia(Uri imageUri)
        {
            pMedia = new ParseMedia();
            pMedia.setImageKey("image");
            pMedia.setByteImage(convertImageToBytes(imageUri));
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

        public void saveInBackgroundWithMedia(HashMap<String, Object> info, SaveMediaCallBack callback) {
            progLayout.setVisibility(View.VISIBLE);
            tvProgSubTitle.setText("please wait...");
            pvUploadbar.setProgress(0);

            if (pMedia.getByteImage() != null) {
                tvProgTitle.setText("Uploading Image");
                pvUploadbar.setProgress(0);

                final ParseFile imageFile = new ParseFile(pMedia.getByteImage());
                imageFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            info.put("imageURL", imageFile.getUrl());
                            currUser.put("therapistInfo", info);
                        }

                        tvProgTitle.setText("Upload Post");
                        currUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                progLayout.setVisibility(View.GONE);
                                callback.onSuccess("success");
                                return;
                            }
                        });
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
                tvProgTitle.setText("Submitting info");
                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        callback.onSuccess("success");
                    }
                });
            }

        }
    }
}