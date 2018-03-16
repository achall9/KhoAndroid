package com.kholabs.khoand.Fragment;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Adapter.CommentAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.Common.FragmentBase;
import com.kholabs.khoand.Common.FragmentPopKeys;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.CustomView.ActionSheet;
import com.kholabs.khoand.CustomView.ItemView.FeedCardView;
import com.kholabs.khoand.Interface.SaveMediaCallBack;
import com.kholabs.khoand.Model.Comment;
import com.kholabs.khoand.Model.Feed;
import com.kholabs.khoand.Model.ParseMedia;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.BackPressImpl;
import com.kholabs.khoand.Utils.OnBackPressListener;
import com.kholabs.khoand.Utils.ParseUtil.NotificationUtils;
import com.kholabs.khoand.Utils.ParseUtil.ParseUtils;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FeedPostFragment extends FragmentBase implements View.OnClickListener, ActionSheet.ActionSheetListener,
                                                CommentAdapter.CommentItemListener, ConstValues, MainActivity.onKeyBackPressedListener
{

    private static final String ARG_PARAM = "headeritem";

    private final int TAKE_PHOTO_FROM_GALLERY = 101;
    private final int TAKE_VIDEO_FROM_GALLERY = 102;

    ArrayList<Comment> commentArray = new ArrayList<>();
    CommentAdapter adapter;

    private View rootView;
    private FeedCardView headerView;
    private LinearLayout llBack;
    private ListView listComment;
    private EditText etComment;
    private ImageView ivAttach, ivRemove;
    private TextView tvPost;

    /* Blur Dialog Layout*/
    private RelativeLayout dialogLayout;
    private Button btnSelectImage, btnSelectVideo, btnSelectCancel;
    /* */

    /* Blur Progress Layout */
    private RelativeLayout progLayout;
    private TextView tvProgTitle, tvProgSubTitle;
    private ProgressBar pvUploadbar;
    private Button btnProgCancel;
    /* */
    public String ncObjId;
    public Feed headerItem;
    public ParseObject post;

    private ActionSheet takePhotoActionSheet = null;
    private ActionSheet takeVideoFromCameraActionSheet = null;
    private ActionSheet commentAction = null;

    private boolean isTakingPostPhoto = false;
    private boolean isTakingPostVideo = false;
    private boolean isEditingComment = false;
    private boolean isRemovable = false;
    private boolean bannedPost = false;

    private Uri mImageViewUri = null;
    private Uri mVideoUri = null;
    private Bitmap mImageThumbnail = null;
    private Bitmap mVideoThumbnail = null;

    private Comment _editItem, _tempItem;
    private int _editPos = 0, _tempPos = 0;
    private int nActionCase = 0;
    private int fromParent = -1;

    private TrackSelector trackSelector;
    private LoadControl loadControl;
    private ExecutorService executor;

    private View.OnClickListener snapPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getActivity().setTheme(R.style.ActionSheetStyleIOS7);
            dialogLayout.setVisibility(View.GONE);
            isTakingPostPhoto = true;
            isTakingPostVideo = false;
            isEditingComment = false;

            if (takePhotoActionSheet == null && mImageViewUri == null) {
                isRemovable = false;
                takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_image))
                        .setCancelableOnTouchOutside(true)
                        .setListener(FeedPostFragment.this)
                        .show();
            } else {
                if (mImageViewUri == null) {
                    isRemovable = false;

                    takePhotoActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_image))
                            .setCancelableOnTouchOutside(true)
                            .setListener(FeedPostFragment.this)
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
                            .setListener(FeedPostFragment.this)
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
            isEditingComment = false;

            dialogLayout.setVisibility(View.GONE);

            if (takeVideoFromCameraActionSheet == null && mVideoUri == null) {
                isRemovable = false;

                takeVideoFromCameraActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_video))
                        .setCancelableOnTouchOutside(true)
                        .setListener(FeedPostFragment.this)
                        .show();
            } else {
                if (mVideoUri == null) {
                    isRemovable = false;

                    takeVideoFromCameraActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_add_video))
                            .setCancelableOnTouchOutside(true)
                            .setListener(FeedPostFragment.this)
                            .show();
                } else {
                    isRemovable = true;

                    takeVideoFromCameraActionSheet = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_replace_video),
                                    getResources().getString(R.string.action_sheet_remove_video))
                            .setCancelableOnTouchOutside(true)
                            .setImageVisiblity(true)
                            .setImageBitmap(mVideoThumbnail)
                            .setListener(FeedPostFragment.this)
                            .show();
                }
            }
        }
    };

    public FeedPostFragment()
    {

    }

    public static FeedPostFragment newInstance(Feed item) {
        FeedPostFragment fragment = new FeedPostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Feed _item = MyApp.getInstance().getPostFeed();
        if (_item != null) {
            this.headerItem = _item;
            this.post = headerItem.getOrigData();
        }

        fromParent = MyApp.getInstance().getFromParent();
        ncObjId = MyApp.getInstance().getNcObjectId();

        this._editItem = null;
        this._editPos = 0;
        this._tempItem = null;
        this._tempPos = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (rootView ==  null)
        {
            rootView = inflater.inflate(R.layout.fragment_post, container, false);
            ((MainActivity)getActivity()).setOnkeyBackPressedListener(this);

            llBack = (LinearLayout) rootView.findViewById(R.id.llBack);
            listComment = (ListView) rootView.findViewById(R.id.listComments);
            etComment = (EditText) rootView.findViewById(R.id.etComment);
            ivAttach = (ImageView) rootView.findViewById(R.id.ivAttach);
            ivRemove = (ImageView) rootView.findViewById(R.id.iv_remove);
            ivRemove.setVisibility(View.GONE);

            tvPost = (TextView) rootView.findViewById(R.id.tvPost);

            if (headerItem != null)
            {
                headerView = new FeedCardView(getActivity(), headerItem);
                listComment.addHeaderView(headerView);
            }

            adapter = new CommentAdapter(getActivity(), FeedPostFragment.this, trackSelector, loadControl);
            adapter.setListItems(commentArray);
            listComment.setAdapter(adapter);

        /*Blur Layout*/
            dialogLayout = (RelativeLayout) rootView.findViewById(R.id.dialog_layout);
            dialogLayout.setVisibility(View.GONE);
            btnSelectCancel = (Button) rootView.findViewById(R.id.btn_cancel);
            btnSelectImage = (Button) rootView.findViewById(R.id.btn_select_image);
            btnSelectVideo = (Button) rootView.findViewById(R.id.btn_select_video);
        /*End*/

        /*Blur ProgressBar layout*/
            progLayout = (RelativeLayout) rootView.findViewById(R.id.progress_layout);
            progLayout.setVisibility(View.GONE);
            tvProgTitle = (TextView) rootView.findViewById(R.id.txt_progtitle);
            tvProgSubTitle = (TextView) rootView.findViewById(R.id.txt_progwaiting);
            pvUploadbar = (ProgressBar) rootView.findViewById(R.id.progress_uploading);
            pvUploadbar.setMax(100);
            btnProgCancel = (Button) rootView.findViewById(R.id.btn_progcancel);

            createDefaultExoPlayer();
            adapter = new CommentAdapter(getActivity(), this, trackSelector, loadControl);
            adapter.addItems(commentArray);

            checkBannedPost();
            checkRemovePost();
            initEvent();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    reloadResponse();
                }
            }, 500);
        }
        else
        {
            reloadResponse();
        }
        IntentFilter msgReceiverIntent = new IntentFilter();
        msgReceiverIntent.addAction("android.intent.action.IM_NEW_COMMENT");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mCommentReceiver, msgReceiverIntent);
        return rootView;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocalBroadcastManager.getInstance((getActivity())).unregisterReceiver(mCommentReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llBack:
                /*
                if (fromParent == PostPageFrom.fromFeedPage)
                {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.popBackStack(FragmentPopKeys.FEED, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                else if (fromParent == PostPageFrom.fromProfilePage)
                {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.popBackStack(FragmentPopKeys.PROFILEPAGE, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else if (fromParent == PostPageFrom.fromSearchFeed)
                {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack(FragmentPopKeys.FEEDSEARCH, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                */
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
                break;
            case R.id.ivAttach:
                dialogLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.tvPost:
                ActionSendPost(false);
                break;
            case R.id.iv_remove:
                removePost();
                break;
            case R.id.btn_cancel:
                dialogLayout.setVisibility(View.GONE);
                break;
            case R.id.btn_progcancel:
                progLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    private void initEvent() {
        llBack.setOnClickListener(this);
        ivAttach.setOnClickListener(this);
        tvPost.setOnClickListener(this);
        ivRemove.setOnClickListener(this);

        btnSelectVideo.setOnClickListener(snapPostVideoListener);
        btnSelectImage.setOnClickListener(snapPhotoClickListener);
        btnSelectCancel.setOnClickListener(this);
        btnProgCancel.setOnClickListener(this);
    }

    private void createDefaultExoPlayer()
    {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        loadControl = new DefaultLoadControl();


    }

    private void checkBannedPost()
    {
        ParseUser user = ParseUser.getCurrentUser();
        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                boolean isTherapist = object.getBoolean("isTherapist");
                if (isTherapist) {
                    boolean verified = object.getBoolean("verifiedTherapist");
                    if (!verified)
                        bannedPost = true;

                }
            }
        });
    }

    private void checkRemovePost()
    {

        ParseUser pOwner = post.getParseUser("owner");
        if (pOwner.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
            ivRemove.setVisibility(View.VISIBLE);
    }

    private void acceptAsAnswer()
    {
        if (_tempItem == null) return;

        Comment _item = _tempItem;
        int _pos = _tempPos;

        ParseObject comment = _item.getOrigData();
        ParseObject post = comment.getParseObject("post");
        post.put("acceptedComment", comment);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                updateView(_item, _pos);
            }
        });

    }

    private void removeAcceptedAnswer()
    {
        if (_tempItem == null) return;
        Comment _item = _tempItem;
        int _pos = _tempPos;

        ParseObject comment = _item.getOrigData();
        ParseObject post = comment.getParseObject("post");
        post.remove("acceptedComment");
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                updateView(_item, _pos);
            }
        });

    }

    private void EditingComment()
    {
        if (_tempItem == null)
            return;
        _editItem = _tempItem;
        _editPos = _tempPos;
        etComment.setText(_editItem.getContent());
        etComment.requestFocus();
        _editItem.isEditingMode = true;
    }

    private void RemoveSure()
    {
        if (_editItem == null)
            return;

        ParseUser sendUser = _editItem.getpUser();
        ParseUser user = ParseUser.getCurrentUser();
        if (!sendUser.getObjectId().equals(user.getObjectId()))
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        TextView myMsg = new TextView(getActivity());
        myMsg.setText("Are you sure?");
        myMsg.setTextColor(Color.WHITE);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        TextView txtTitle = new TextView(getActivity());
        txtTitle.setText("Remove Comment");

        //title.setBackgroundColor(Color.DKGRAY);
        txtTitle.setPadding(10, 10, 10, 10);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextSize(20);
        txtTitle.setTextColor(Color.WHITE);
        builder.setCustomTitle(txtTitle);
        builder.setCancelable(false);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ParseObject comment = _editItem.getOrigData();
                comment.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (adapter != null) {
                            adapter.removeItem(_editPos);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

                if (_editItem != null && _editPos > _tempPos)
                    _editPos--;

                _tempItem = null;
                _tempPos = 0;
                dialogInterface.dismiss();
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _tempItem = null;
                _tempPos = 0;
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();


    }


    private void removePost() {
        ParseUser postUser = post.getParseUser("owner");
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (!postUser.getObjectId().equals(currentUser.getObjectId()))
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        TextView myMsg = new TextView(getActivity());
        myMsg.setText("Are you sure you want to remove your post?");
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        TextView txtTitle = new TextView(getActivity());
        txtTitle.setText("Remove Comment");

        //title.setBackgroundColor(Color.DKGRAY);
        txtTitle.setPadding(10, 10, 10, 10);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextSize(20);
        builder.setCustomTitle(txtTitle);
        builder.setCancelable(false);

        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progLayout.setVisibility(View.VISIBLE);
                tvProgTitle.setText("Removing Post");
                tvProgSubTitle.setText("please wait...");
                pvUploadbar.setProgress(0);

                post.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        progLayout.setVisibility(View.GONE);
                        ((MainActivity)getActivity()).runRemovePostTask(post);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.popBackStack();
                        ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);

                    }
                });

                dialogInterface.dismiss();
            }
        });


        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _tempItem = null;
                _tempPos = 0;
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void ActionSendPost(boolean isUploading)
    {
        String comment = etComment.getText().toString();
        if (comment.length() == 0 || comment.equals("Type comment here"))
        {
            MyApp.getInstance().alertDisplayer("Warning", "Missing Comment");
            return;
        }

        if (bannedPost) {
            MyApp.getInstance().alertDisplayer("Warning", getString(R.string.alert_profile_unverified));
            return;
        }

        ParseUser currentUser = ParseUser.getCurrentUser();

        ParseObject commentObj = new ParseObject("Comments");
        commentObj.put("text", comment);
        commentObj.put("post", post);
        commentObj.put("user", currentUser);

        /*  check for edit mode: */
        if (_editItem != null && _editItem.isEditingMode == true)
        {
            applyEditComment();
            prepareForNewComment();
            _editItem = null;
            _editPos = 0;
            return;
        }

        AddUploadComment(commentObj);

        /* Notification part */
        String message = String.format("%s Commented on your post : %s", currentUser.get("name"), comment);
        ParseUser user = post.getParseUser("owner");
        if (!user.getObjectId().equals(currentUser.getObjectId()))
            NotificationUtils.sendPush(message, user, "Posts", post);

    }


    private void AddUploadComment(ParseObject commentObj)
    {
        ParseMediaUpload mediaObj = new ParseMediaUpload(getContext(), commentObj);
        mediaObj.initializeParseMedia(mImageViewUri, mVideoUri, "image", "video");

        mediaObj.saveInBackgroundWithMedia(new SaveMediaCallBack() {
            @Override
            public void onSuccess(Object object) {
                new loadDataTask().execute();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void prepareForNewComment()
    {
        etComment.setText("Type comment here");
        ivAttach.setImageResource(R.drawable.misc_attach);
        mImageViewUri = null;
        mVideoUri = null;
        mImageThumbnail = null;
        mVideoThumbnail = null;

    }

    private void applyEditComment()
    {
        if (_editItem == null)
            return;

        View v = listComment.getChildAt(_editPos -
                listComment.getFirstVisiblePosition() + 1);

        if(v == null)
            return;

        final CommentAdapter.ViewHolder mHolder = (CommentAdapter.ViewHolder)v.getTag();
        mHolder.setViewAlpha(40);

        ParseObject commentObj = _editItem.getOrigData();
        String strUpdate = etComment.getText().toString();
        commentObj.put("text", strUpdate);

        if (_editItem.getPostImage() == null && _editItem.getPostVideoPath().equals(""))
        {
            commentObj.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    _editItem.setContent(strUpdate);
                    adapter.setItem(_editPos, _editItem);
                    mHolder.setViewAlpha(255);
                    mHolder.refreshView(_editItem);
                }
            });
        }
        else
        {
            ParseMediaUpload mediaObj = new ParseMediaUpload(getContext(), commentObj);
            mediaObj.initializeParseMedia(mImageViewUri, mVideoUri, "image", "video");

            mediaObj.saveInBackgroundWithMedia(new SaveMediaCallBack() {
                @Override
                public void onSuccess(Object object) {
                     mHolder.setViewAlpha(255);
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
        }

    }

    public class loadDataTask extends AsyncTask<Void, List<ParseObject>, Void> {
        @Override
        protected void onPreExecute()
        {
           showDialog();
        }

        @Override
        protected Void doInBackground(Void... datas) {

            if (post == null) {
                return null;
            }

            if (commentArray != null)
                commentArray.clear();
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Comments");
            query.whereEqualTo("post", post);
            query.orderByDescending("updatedAt");
            query.include("post");
            query.include("user");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null || objects == null | objects.size() == 0)
                    {
                        List<ParseObject> threads = null;
                        publishProgress(threads);
                        return;
                    }

                    publishProgress(objects);
                }
            });

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

        }

        @Override
        protected void onProgressUpdate(List<ParseObject>... values) {
            if (values == null) {
                headerView.noComments();
                hideDialog();
                return;
            }

            List<ParseObject> result = values[0];
            if (result == null) {
                headerView.noComments();
                hideDialog();
            }
            else
            {
                hideDialog();
                for (int i=0; i<result.size(); i++)
                {
                    Comment item = GetDataFromParseObject(result.get(i));
                    commentArray.add(item);
                }

                adapter.setListItems(commentArray);
                adapter.notifyDataSetChanged();

                //for (int i=0; i<commentArray.size(); i++)
                //    ParseandNotify(commentArray.get(i), i);
            }

        }

    }


    private void reloadResponse()
    {
        if (post == null) return;

        if (commentArray != null)
            commentArray.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comments");
        query.whereEqualTo("post", post);
        query.orderByDescending("updatedAt");
        query.include("post");
        query.include("user");

        showDialog();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null | objects.size() == 0)
                {
                    hideDialog();
                    headerView.noComments();
                    return;
                }

                for (int i=0; i<objects.size(); i++)
                {
                    Comment item = GetDataFromParseObject(objects.get(i));
                    commentArray.add(item);
                }

                hideDialog();
                adapter = new CommentAdapter(getActivity(), FeedPostFragment.this, trackSelector, loadControl);
                adapter.setListItems(commentArray);
                listComment.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                ParseandNotify();
            }
        });
    }


    private Comment GetDataFromParseObject(ParseObject object)
    {
        ParseUser pUser = object.getParseUser("user");
        Comment item = new Comment();

        item.isTherapist = pUser.getBoolean("isTherapist");
        item.verifedTherapist = pUser.getBoolean("verifiedTherapist");
        item.setContent(object.getString("text"));
        long createTime = object.getCreatedAt().getTime();
        String timeago = TimeAgo.using(createTime);
        item.setDate(timeago);
        item.setpUser(pUser);
        item.setOrigData(object);
        return item;
    }

    public class AvatarWrapper
    {
        public Comment item;
        public int position;
    }


    public void ParseandNotify() {

        if (commentArray == null || commentArray.size() == 0)
            return;

        for (int i=0; i<commentArray.size(); i++)
        {
            ExecutorService executor = Executors.newFixedThreadPool(3);

            Comment item = commentArray.get(i);
            AvatarRunnable thread1 = new AvatarRunnable(item, i);
            executor.submit(thread1);

            CheckPostRunnable thread2 = new CheckPostRunnable(item, i);
            executor.submit(thread2);

            CheckVideoRunnable thread3 = new CheckVideoRunnable(item, i);
            executor.submit(thread3);
        }
    }

    private void destroyPlayers()
    {
        if (listComment == null) return;
        for (int i=0; i<listComment.getCount(); i++)
        {
            View v = listComment.getChildAt(i-listComment.getFirstVisiblePosition() + 1);
            if (v == null)
                continue;
            CommentAdapter.ViewHolder mHolder = (CommentAdapter.ViewHolder)v.getTag();
            mHolder.destroyPlayer();
        }


    }

    private void pausePlayers()
    {
        if (listComment == null) return;
        for (int i=0; i<listComment.getCount(); i++)
        {
            View v = listComment.getChildAt(i-listComment.getFirstVisiblePosition() + 1);
            if (v == null)
                continue;
            CommentAdapter.ViewHolder mHolder = (CommentAdapter.ViewHolder)v.getTag();
            mHolder.pausePlayer();
        }

    }

    private void resumePlayers()
    {
        if (listComment == null) return;
        for (int i=0; i<listComment.getCount(); i++)
        {
            View v = listComment.getChildAt(i-listComment.getFirstVisiblePosition() + 1);
            if (v == null)
                continue;
            CommentAdapter.ViewHolder mHolder = (CommentAdapter.ViewHolder)v.getTag();
            mHolder.startPlayer();
        }

    }

    private void updateView(Comment item, int index){
        View v = listComment.getChildAt(index -
                listComment.getFirstVisiblePosition() + 1);

        if(v == null)
            return;

        CommentAdapter.ViewHolder mHolder = (CommentAdapter.ViewHolder)v.getTag();
        mHolder.refreshView(item);
    }


    public void GetAvatarPhotoFromData(ParseUser user, final Comment _item, final int position)
    {
        if (_item.getAvatar() != null)
            return;


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
                        _item.setAvatar(bmp);
                        bmp = null;

                        if (adapter != null) {
                            adapter.setItem(position, _item);
                            updateView(_item, position);
                        }

                    }
                });

            }
        });
    }

    public void checkForPostImage(ParseObject object, final Comment _item, final int position)
    {
        if (_item.getPostImage() != null)
            return;

        ParseFile pFile = object.getParseFile("image");
        if (pFile == null)
            return;

        pFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e != null || data == null)
                    return;

                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                _item.setPostImage(bmp);
                bmp = null;

                if (adapter != null)
                {
                    adapter.setItem(position, _item);
                    updateView(_item, position);
                }
            }
        });

    }

    public void checkForPostVideo(ParseObject object, final Comment _item, final int position)
    {
        if (!_item.getPostVideoPath().equals(""))
            return;

        ParseFile pFile = object.getParseFile("video");
        if (pFile == null)
            return;

        //Uri video = Uri.parse(pFile.getUrl());
        _item.setPostVideoPath(pFile.getUrl());
        if (adapter != null)
        {
            adapter.setItem(position, _item);
            updateView(_item, position);
        }

    }

    public Bitmap getThumbnailFromVideoUri(Uri videoUri)
    {
        Bitmap thumb = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getContext(), videoUri);
        try {
            thumb = retriever.getFrameAtTime(1000);
        } catch (Exception e)
        {
            System.out.println("Exception trying to play file subset");
        }
        return thumb;
    }


    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
        if (isCancel)
        {
        }
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        if (index == 0)
        {
            if (isTakingPostPhoto == true)
            {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("image/*");
                    getActivity().startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
                    //Intent i = new Intent(rootView.getContext(), TherapistSignupActivity.class);
                    //getActivity().startActivityForResult(i, TAKE_PHOTO_FROM_GALLERY);
                }
                else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
                }
            }
            else if (isTakingPostVideo == true)
            {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("video/*");
                    getActivity().startActivityForResult(i, TAKE_VIDEO_FROM_GALLERY);
                }
                else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
                }
            }
            else if (isEditingComment == true)
            {

            }

        } else if (index == 1)
        {
            if (isTakingPostPhoto == true)
            {
                mImageThumbnail = null;
                mImageViewUri = null;
                ivAttach.setImageResource(R.drawable.misc_attach);
                ivAttach.invalidate();
            }
            else if (isTakingPostVideo == true)
            {
                mVideoThumbnail = null;
                mVideoUri = null;
                ivAttach.setImageResource(R.drawable.misc_attach);
                ivAttach.invalidate();
            }
            else if (isEditingComment == true)
            {
                _editItem = _tempItem;
                _editPos = _tempPos;
                RemoveSure();
            }
        } else if (index == 2)
        {
            if (isEditingComment == true)
                EditingComment();
        } else if (index == 3)
        {
            if (isEditingComment == true)
            {
                if (nActionCase == 2)
                    acceptAsAnswer();
                else if (nActionCase == 3)
                    removeAcceptedAnswer();
            }
        }
    }

    @Override
    public void editItemListener(Comment item, int position) {
        isTakingPostPhoto = false;
        isEditingComment =  true;
        isTakingPostVideo = false;

        getActivity().setTheme(R.style.ActionSheetStyleIOS7);
        ParseUser commentOwner = item.getpUser();
        ParseUser current = ParseUser.getCurrentUser();
        ParseObject post = item.getOrigData().getParseObject("post");
        ParseUser postOwner = post.getParseUser("owner");
        ParseObject accepted = post.getParseObject("acceptedComment");

        String commentObjId = item.getOrigData().getObjectId();

        if (commentOwner.getObjectId().equals(current.getObjectId()))
        {
            if (postOwner.getObjectId().equals(current.getObjectId()))
            {
                if (commentObjId.equals(accepted.getObjectId()))
                {
                    nActionCase = 3;
                    commentAction = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_report),
                                    getResources().getString(R.string.action_sheet_remove_comment),
                                    getResources().getString(R.string.action_sheet_edit_comment),
                                    getResources().getString(R.string.action_sheet_remove_answer))
                            .setListener(FeedPostFragment.this)
                            .setCancelableOnTouchOutside(true)
                            .show();

                } else
                {
                    nActionCase = 2;
                    commentAction = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                            .setCancelButtonTitle(getResources().getString(R.string.cancel))
                            .setOtherButtonTitles(getResources().getString(R.string.action_sheet_report),
                                    getResources().getString(R.string.action_sheet_remove_comment),
                                    getResources().getString(R.string.action_sheet_edit_comment),
                                    getResources().getString(R.string.action_sheet_accept_answer))
                            .setListener(FeedPostFragment.this)
                            .setCancelableOnTouchOutside(true)
                            .show();
                }
            } else
            {
                nActionCase = 1;
                commentAction = ActionSheet.createBuilder(getContext(), getActivity().getSupportFragmentManager())
                        .setCancelButtonTitle(getResources().getString(R.string.cancel))
                        .setOtherButtonTitles(getResources().getString(R.string.action_sheet_report),
                                getResources().getString(R.string.action_sheet_remove_comment),
                                getResources().getString(R.string.action_sheet_edit_comment))
                        .setListener(FeedPostFragment.this)
                        .setCancelableOnTouchOutside(true)
                        .show();
            }

        }

        _tempItem = item;
        _tempPos = position;
    }

    @Override
    public void goToProfileFragment(Comment item, int position) {
        if (item == null) return;
        MyApp.getInstance().setFromParent(ProfileFrom.fromFeedPage);
        MyApp.getInstance().setShareUser(item.getpUser());

        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.feed_root_frame, new InstagramProfileFragment(), "Profile")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBack() {
        /*
        if (fromParent == PostPageFrom.fromFeedPage)
        {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(FragmentPopKeys.FEED, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else if (fromParent == PostPageFrom.fromProfilePage)
        {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(FragmentPopKeys.PROFILEPAGE, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (fromParent == PostPageFrom.fromSearchFeed)
        {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack(FragmentPopKeys.FEEDSEARCH, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        */

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
        ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
    }


    /*
    private class UpdateListDataTask extends AsyncTask<ArrayList<Comment>, Void, ArrayList<Comment>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Comment> doInBackground(ArrayList<Comment>[] arrayLists) {
            if (arrayLists == null)
                return  null;

            ArrayList<Comment> result = arrayLists[0];
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Comment> result) {
            for (int i=0; i<result.size(); i++)
            {
                ParseandNotify(result.get(i), i);
            }
        }
    }
    */


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
    public  void onPause()
    {
        super.onPause();
        if (listComment.getCount() > 0)
            pausePlayers();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        if (listComment.getCount() > 0)
            destroyPlayers();

        /*
        if (headerView != null)
            headerView.cancelExecutor();

        if (executor != null)
            executor.shutdownNow();
            */
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
                            ivAttach.setImageBitmap(mImageThumbnail);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TAKE_VIDEO_FROM_GALLERY:
                    if (data != null) {
                        mVideoUri = data.getData();
                        mVideoThumbnail = getThumbnailFromVideoUri(mVideoUri);
                        ivAttach.setImageBitmap(mVideoThumbnail);
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

        public AsyncTask<Wrapper, Void, Wrapper> saveInBackgroundWithMedia(SaveMediaCallBack callback) {
            Wrapper result = new Wrapper();
            result.media = pMedia;
            result.comment = _original;

            return new MediaUploadTask(mContext, callback).execute(result);

        }
    }


    public class NotiGroup {
        ParseUser user;
        ParseObject comment;
    }

    /*
    public class NotificationTask extends AsyncTask<NotiGroup, Void, NotiGroup> {

        @Override
        protected NotiGroup doInBackground(NotiGroup... notiGroups) {
            NotiGroup data = notiGroups[0];
            return data;
        }

        @Override
        protected void onPostExecute(NotiGroup result) {
            final ParseUser pUser = result.user;
            final ParseObject pComment = result.comment;
            ParseUser currentUser = ParseUser.getCurrentUser();

            String message = String.format("%s Commented on %s's post: %s", currentUser.get("name"), pUser.get("name"), pComment.get("text"));
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Comments");
            query.whereEqualTo("post", pComment.get("post"));
            query.orderByDescending("updatedAt");
            query.include("post");
            query.include("user");
            query.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0)
                        return;
                    List<ParseUser> allCommentUsers = new ArrayList<>();
                    allCommentUsers.add(pComment.getParseUser("user"));
                    for (ParseObject allComments : objects)
                    {
                        boolean isExist = false;
                        for (ParseUser commentUser : allCommentUsers)
                        {
                            ParseUser iUser = allComments.getParseUser("user");
                            if ((iUser.getObjectId().equals(commentUser.getObjectId())) ||
                                (currentUser.getObjectId().equals(commentUser.getObjectId())))
                            {
                                isExist = true;
                                break;
                            }
                        }

                        if (!isExist)
                        {
                            allCommentUsers.add(allComments.getParseUser("user"));
                            NotificationUtils.sendPush(message, allComments.getParseUser("user"), "Posts", post);

                        }
                    }
                }
            });

        }
    }
    */

    public class Wrapper {
        ParseMedia media;
        ParseObject comment;
    }

    public class MediaUploadTask extends AsyncTask<Wrapper, Void, Wrapper> {

        private SaveMediaCallBack<String> mCallBack;
        private Context mContext;
        public Exception mException;


        public MediaUploadTask(Context context, SaveMediaCallBack callback) {
            mCallBack = callback;
            mContext = context;
        }

        @Override
        protected Wrapper doInBackground(Wrapper... params) {
            Wrapper data = params[0];
            return data;
        }

        @Override
        protected void onPostExecute(Wrapper result) {
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

    private final BroadcastReceiver mCommentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadResponse();
        }
    };

    public class AvatarRunnable implements Runnable {
        private Comment item;
        private int position;

        public AvatarRunnable(Comment _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {

            GetAvatarPhotoFromData(item.getpUser(), item, position);
        }
    }

    public class CheckPostRunnable implements Runnable {
        private Comment item;
        private int position;

        public CheckPostRunnable(Comment _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {

            checkForPostImage(item.getOrigData(), item, position);
        }
    }

    public class CheckVideoRunnable implements Runnable {
        private Comment item;
        private int position;

        public CheckVideoRunnable(Comment _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {

            checkForPostVideo(item.getOrigData(), item, position);
        }
    }
}
