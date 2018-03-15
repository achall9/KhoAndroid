package com.kholabs.khoand.CustomView.ItemView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.Fragment.InstagramProfileFragment;
import com.kholabs.khoand.Model.Feed;
import com.kholabs.khoand.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pkmmte.view.CircularImageView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FeedCardView extends LinearLayout {

    private Context mContext = null;
    private Feed mainItem;
    private LayoutInflater inflater;

    private MediaController mMediaController;
    private FrameLayout videoFrame;
    private CircularImageView civPhoto;
    private TextView tvFeedName;
    private TextView tvFeedTime;
    private TextView tvFeedText;
    private TextView tvAnswer1;
    private TextView tvAnswer2;
    private TextView tvFeedSupport;
    private TextView tvFeedResponse;
    private ImageView ivShare;
    private TextView tvAccepted;
    private ImageView ivFeedSupport;
    private ImageView ivFeedComment;
    private ImageView ivPostImage;
    private CircularImageView playButton;
    private VideoView ivPostVideo;
    private LinearLayout bodyLayout;
    private TextView tvNoComment;
    private Object lockObj = new Object();
    private ExecutorService executor;

    public FeedCardView(Context context, Feed item) {
        super(context);
        this.mContext = context;
        this.mainItem = item;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.include_commentheader , this , true);

        bodyLayout = (LinearLayout) findViewById(R.id.bodyLayout);
        civPhoto = (CircularImageView) findViewById(R.id.civPhoto);

        tvFeedName = (TextView) findViewById(R.id.tvFeedName);
        tvFeedTime = (TextView) findViewById(R.id.tvFeedTime);
        tvFeedText = (TextView) findViewById(R.id.tvFeedText);
        tvAnswer1 = (TextView) findViewById(R.id.tvAnswer1);
        tvAnswer2 = (TextView) findViewById(R.id.tvAnswer2);
        tvFeedSupport = (TextView) findViewById(R.id.tvSupport);
        tvFeedResponse = (TextView) findViewById(R.id.tvResponse);
        tvAccepted = (TextView) findViewById(R.id.tvAccepted);
        ivShare = (ImageView) findViewById(R.id.ivShare);
        ivFeedSupport = (ImageView) findViewById(R.id.ivFeedFavorite);
        ivFeedComment = (ImageView) findViewById(R.id.ivFeedComment);

        ivPostImage = (ImageView) findViewById(R.id.post_image);

        ivPostVideo = (VideoView) findViewById(R.id.post_video);
        videoFrame = (FrameLayout) findViewById(R.id.video_frame);
        playButton = (CircularImageView) findViewById(R.id.play_button);

        tvNoComment = (TextView) findViewById(R.id.txtNoComment);
        tvNoComment.setVisibility(View.GONE);
        refreshView(item);

        if (item.isNotification)
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestNetworkData(item);
                }
            }, 500);
        }

        ivFeedSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.isSupport == false)
                    likeSupport(item);
                else
                    dislikeSupport(item);
            };
        });

        civPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.getInstance().setFromParent(ConstValues.ProfileFrom.fromFeedPage);
                MyApp.getInstance().setShareUser(item.getpUser());

                ((MainActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.feed_root_frame, new InstagramProfileFragment(), "Profile")
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    public void noComments()
    {
        tvNoComment.setVisibility(View.VISIBLE);
    }

    public void refreshView(Feed item)
    {
        tvFeedName.setText(item.getName());
        tvFeedTime.setText(item.getDate());
        tvFeedText.setText(item.getContent());
        tvAnswer1.setText(item.getHowlong());
        tvAnswer2.setText(item.getWorst());

        if (!item.isImageLoaded)
        {
            civPhoto.setImageResource(R.drawable.blankprofile);
            if (item.getAvatar() != null) {
                civPhoto.setImageBitmap(item.getAvatar());
                item.setAvatar(null);

                item.isImageLoaded = true;
            }
            else {
            }
        }


        if (!item.isPostImageLoaded)
        {
            if (item.getPostImage() != null)
            {
                ivPostImage.setImageBitmap(item.getPostImage());
                ivPostImage.setVisibility(View.VISIBLE);
                item.isPostImageLoaded = true;
                item.setPostImage(null);
            }
            else {
                ivPostImage.setVisibility(View.GONE);
            }
        }

        if (!item.getPostVideoUrl().equals("")) {
            ivPostVideo.setVideoPath(item.getPostVideoUrl());
            ivPostVideo.setMediaController(mMediaController);
            videoFrame.setVisibility(View.VISIBLE);
        }
        else
            videoFrame.setVisibility(View.GONE);

        if (!item.isSupport)
            ivFeedSupport.setImageResource(R.drawable.unlike);
        else
            ivFeedSupport.setImageResource(R.drawable.misc_like_red);

        if (item.getnSupportCnt() != -1) {
            if (item.getnSupportCnt() == 1)
                tvFeedSupport.setText("1 Support");
            else
                tvFeedSupport.setText(String.format("%d Supports", item.getnSupportCnt()));
        }

        if (item.getnCommentCnt() != -1) {
            if (item.getnCommentCnt() == 1)
                tvFeedResponse.setText("1 Response");
            else
                tvFeedResponse.setText(String.format("%d Responses", item.getnCommentCnt()));
        }
        if (item.getpAnswer() != null) {
            ivShare.setVisibility(View.VISIBLE);
            tvAccepted.setVisibility(View.VISIBLE);
            ivShare.setImageResource(R.drawable.misc_accepted);
        }
        else {
            ivShare.setVisibility(View.INVISIBLE);
            tvAccepted.setVisibility(View.GONE);
        }

    }

    public synchronized void requestNetworkData(Feed item)
    {
        executor = Executors.newFixedThreadPool(5);

        Runnable thread1 = new Runnable() {
            @Override
            public void run() {
                checkForPostImage(item.getOrigData(), item);
            }
        };

        executor.submit(thread1);

        Runnable thread2 = new Runnable() {
            @Override
            public void run() {
                checkForPostVideo(item.getOrigData(), item);
            }
        };

        executor.submit(thread2);

        Runnable thread3 = new Runnable() {
            @Override
            public void run() {
                GetAvatarPhotoFromData(item.getpUser(), item);
            }
        };

        executor.submit(thread3);

        Runnable thread4 = new Runnable() {
            @Override
            public void run() {
                loadSupportData(item.getOrigData(), item);

            }
        };

        executor.submit(thread4);

        Runnable thread5 = new Runnable() {
            @Override
            public void run() {
                loadCommentData(item.getOrigData(), item);
            }
        };

        executor.submit(thread5);
    }


    public void cancelExecutor()
    {
        if (executor != null)
            executor.shutdownNow();
    }

    public void GetAvatarPhotoFromData(ParseUser user, Feed item)
    {
        if (item.getAvatar() != null) {
            item.updateCnt++;
            return;
        }

        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null || object == null) {
                    item.updateCnt++;
                    return;
                }

                ParseFile pFile = object.getParseFile("avatar");

                if (pFile == null) {
                    item.updateCnt++;
                    return;
                }

                pFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e != null || data == null) {
                            item.updateCnt++;
                            return;
                        }

                        item.updateCnt++;
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        item.setAvatar(bmp);
                        bmp = null;

                        if (item.updateCnt == 5)
                            refreshView(item);
                    }
                });
            }
        });

    }

    public void checkForPostImage(ParseObject object, Feed item)
    {
        if (item.getPostImage() != null) {
            item.updateCnt++;
            return;
        }

        ParseFile pFile = object.getParseFile("image");
        if (pFile == null) {
            item.updateCnt++;
            return;
        }

        pFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {

                if (e != null || data == null) {
                    item.updateCnt++;
                    return;
                }

                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                item.setPostImage(bmp);
                item.updateCnt++;

                if (item.updateCnt == 5)
                    refreshView(item);

            }
        });
    }


    public void checkForPostVideo(ParseObject object, Feed item)
    {
        if (!item.getPostVideoUrl().equals("")) {
            item.updateCnt++;
            return;
        }


        ParseFile pFile = object.getParseFile("video");
        if (pFile == null) {
            item.updateCnt++;
            return;
        }

        item.updateCnt++;
        item.setPostVideoUrl(pFile.getUrl());

        if (item.updateCnt == 5)
            refreshView(item);


    }

    public void loadSupportData(ParseObject object, Feed item) {
        if (item.getnSupportCnt() != -1) {
            item.updateCnt++;
            return;
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Supports");
        query.whereEqualTo("post", object);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (objects == null)
                {
                    item.updateCnt++;
                    return;
                }

                ParseUser current = ParseUser.getCurrentUser();

                item.isSupport = false;
                for (int i = 0; i < objects.size(); i++) {
                    ParseUser user = objects.get(i).getParseUser("user");
                    String currId = current.getObjectId();
                    String userId = user.getObjectId();
                    if (current.getObjectId().equals(user.getObjectId()))
                        item.isSupport = true;
                }

                int count = objects.size();
                item.setnSupportCnt(count);
                item.updateCnt++;
                if (item.updateCnt == 5)
                    refreshView(item);

            }
        });

    }


    public   void loadCommentData(ParseObject object, Feed item) {
        if (item.getnCommentCnt() != -1) {
            item.updateCnt++;
            return;
        }

        ParseQuery<ParseObject> queryComm = ParseQuery.getQuery("Comments");
        queryComm.whereEqualTo("post", object);

        try {
            int commCnt = queryComm.count();

            item.setnCommentCnt(commCnt);
            item.updateCnt++;
            if (item.updateCnt == 5)
                refreshView(item);

        } catch (ParseException e)
        {

        }


    }


    private synchronized void likeSupport(final Feed item)
    {
        synchronized (lockObj)
        {
            final ParseObject like = new ParseObject("Supports");
            like.put("post", item.getOrigData());
            like.put("user", ParseUser.getCurrentUser());

            item.isSupport = true;
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Supports");
            query.whereEqualTo("post", item.getOrigData());
            query.whereEqualTo("user", ParseUser.getCurrentUser());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects == null)
                        return;;
                    if (objects.size() == 0)
                    {
                        like.saveInBackground();
                        item.setnSupportCnt(item.getnSupportCnt() + 1);
                        item.isSupport = true;
                        refreshView(item);
                        return;
                    }

                }
            });
        }

    }

    private synchronized void dislikeSupport(final Feed item)
    {
        synchronized (lockObj)
        {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Supports");
            query.setLimit(1);
            query.whereEqualTo("post", item.getOrigData());
            query.whereEqualTo("user", ParseUser.getCurrentUser());

            item.isSupport = false;

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0)
                        return;;
                    ParseObject support = objects.get(0);
                    support.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            item.setnSupportCnt(item.getnSupportCnt() - 1);
                            item.isSupport = false;
                            refreshView(item);
                        }
                    });
                }
            });
        }
    }

}
