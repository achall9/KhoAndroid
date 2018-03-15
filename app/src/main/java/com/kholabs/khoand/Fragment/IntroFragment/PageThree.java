package com.kholabs.khoand.Fragment.IntroFragment;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kholabs.khoand.Activity.CropImageActivity;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.CustomView.ActionSheet;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.ProfileHeader.behavior.widget.CircleImageView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class PageThree extends Fragment implements ActionSheet.ActionSheetListener{
    private View rootView;
    private CircleImageView cvAvatar;
    private ActionSheet takePhotoActionSheet = null;

    private final int TAKE_PHOTO_FROM_GALLERY = 101;
    private final int TAKE_PHOTO_FROM_CAMERA = 105;
    private final int PIC_CROP = 106;

    private ParseUser currUser;
    private Uri mImageViewUri = null;
    private Bitmap mImageThumbnail = null;

    public PageThree() {
        // Required empty public constructor
    }

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
                    .setListener(PageThree.this)
                    .show();

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currUser = ParseUser.getCurrentUser();
        rootView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_page_three, container, false);
            cvAvatar = (CircleImageView) rootView.findViewById(R.id.uc_avatar);

            cvAvatar.setOnClickListener(snapPhotoClickListener);
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
            cvAvatar.setImageResource(R.drawable.blankprofile);
            cvAvatar.invalidate();

             removePhotoFromProfile();
        }
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

    private void removePhotoFromProfile()
    {
        currUser.remove("avatar");
        currUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
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
                    currUser.put("avatar", imageFile);
                    currUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                }
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
                            cvAvatar.setImageBitmap(mImageThumbnail);

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
                        cvAvatar.setImageBitmap(mImageThumbnail);

                        uploadPhotoToProfile(mImageThumbnail);
                    }

            }
        }
    }
}
