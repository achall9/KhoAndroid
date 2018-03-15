package com.kholabs.khoand.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.kholabs.khoand.CustomView.PopupWindow.PhotoFullPopupWindow;
import com.kholabs.khoand.Interface.FeedNotifyListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aladar-PC2 on 1/18/2018.
 */

public class FeedListAdapter extends RecyclerView.Adapter<FeedListAdapter.ViewHolder>  {
    private List<Feed> feedList;
    private LayoutInflater inflater;
    private Context mContext = null;
    private Object lockObj = new Object();

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private int updatePos = 0;

    private ExecutorService feedExecutor;
    private List<Runnable> threads;

    public interface FeedListItemListener {
        public void goToPostFragment(Feed item);
    }

    private  FeedListItemListener caller;

    public FeedListAdapter(Context context, FeedListItemListener _caller) {
        feedList = new ArrayList<Feed>();
        this.mContext = context;
        this.caller = _caller;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.threads = null;
        setHasStableIds(true);
    }

    public void setListItems(List<Feed> list) {
        this.feedList = list;
        notifyDataSetChanged();

        for (int i=updatePos; i<feedList.size(); i++)
        {
            Feed item = feedList.get(i);
            ExecutorService executor = Executors.newFixedThreadPool(5);

            AvatarRunnable thread1 = new AvatarRunnable(item, i);
            executor.submit(thread1);

            CheckPostRunnable thread2 = new CheckPostRunnable(item, i);
            executor.submit(thread2);

            CheckVideoRunnable thread3 = new CheckVideoRunnable(item, i);
            executor.submit(thread3);

            loadSupportRunnable thread4 = new loadSupportRunnable(item, i);
            executor.submit(thread4);

            loadCommentRunnable thread5 = new loadCommentRunnable(item, i);
            executor.submit(thread5);
        }


    }

    public void cancelExecutor()
    {
        if (feedExecutor != null)
            feedExecutor.shutdownNow();
    }

    public void completeExecutor()
    {
        if (feedExecutor != null && threads != null)
        {
            for (int i=0; i<threads.size(); i++)
                feedExecutor.submit(threads.get(i));
        }
    }

    public void setUpdatePosition(int Pos) {
        updatePos = Pos;
    }

    public void addItem(Feed contactItem)
    {
        feedList.add(contactItem);
    }

    public void setItem(int position, Feed _item)
    {
        feedList.set(position, _item);
    }

    public void addItems(List<Feed> contactItems)
    {
        for(int i=0;i<contactItems.size();i++)
        {
            feedList.add(contactItems.get(i));
        }
    }

    public void clearAll() {
        if (feedList != null)
        {
            try
            {
                feedList.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            feedList = new ArrayList<Feed>();
        }
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapterfeed, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return  vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ViewHolder mHolder = (ViewHolder)holder;
        Feed item = feedList.get(position);
        mHolder.tvFeedName.setText(item.getName());
        mHolder.tvFeedTime.setText(item.getDate());
        mHolder.tvFeedText.setText(item.getContent());
        mHolder.tvAnswer1.setText(item.getHowlong());
        mHolder.tvAnswer2.setText(item.getWorst());

        if (item.getAvatar() != null)
            mHolder.civPhoto.setImageBitmap(item.getAvatar());
        else
            mHolder.civPhoto.setImageResource(R.drawable.blankprofile);

        if (item.getPostImage() != null)
        {
            mHolder.ivPostImage.setImageBitmap(item.getPostImage());
            mHolder.ivPostImage.setVisibility(View.VISIBLE);
        }
        else
            mHolder.ivPostImage.setVisibility(View.GONE);


        if (!item.getPostVideoUrl().equals("")) {
            mHolder.ivPostVideo.setVideoPath(item.getPostVideoUrl());
            mHolder.ivPostVideo.setMediaController(mHolder.mediaController);
            mHolder.videoFrame.setVisibility(View.VISIBLE);
        }
        else
            mHolder.videoFrame.setVisibility(View.GONE);

        if (!item.isSupport)
            mHolder.ivFeedSupport.setImageResource(R.drawable.unlike);
        else
            mHolder.ivFeedSupport.setImageResource(R.drawable.misc_like_red);

        if (item.getnSupportCnt() != -1) {
            if (item.getnSupportCnt() == 1)
                mHolder.tvFeedSupport.setText("1 Support");
            else
                mHolder.tvFeedSupport.setText(String.format("%d Supports", item.getnSupportCnt()));
        }

        if (item.getnCommentCnt() != -1) {
            if (item.getnCommentCnt() == 1)
                mHolder.tvFeedResponse.setText("1 Response");
            else
                mHolder.tvFeedResponse.setText(String.format("%d Responses", item.getnCommentCnt()));
        }

        if (item.getpAnswer() != null) {
            mHolder.ivShare.setVisibility(View.VISIBLE);
            mHolder.tvAccepted.setVisibility(View.VISIBLE);
            mHolder.ivShare.setImageResource(R.drawable.misc_accepted);
        }
        else {
            mHolder.ivShare.setVisibility(View.INVISIBLE);
            mHolder.tvAccepted.setVisibility(View.INVISIBLE);
        }

        mHolder.ivPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PhotoFullPopupWindow(mContext, R.layout.popup_photo_full, view, item.getPostImage());
            }
        });


        mHolder.ivFeedSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.isSupport == false)
                    likeSupport(item, position);
                else
                    dislikeSupport(item, position);
            };
        });

        mHolder.llResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                caller.goToPostFragment(item);
            }
        });

        mHolder.bodyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                caller.goToPostFragment(item);
            }
        });

        mHolder.ivFeedComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                caller.goToPostFragment(item);
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return feedList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return feedList == null ? 0 : feedList.size();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds)
    {
        super.setHasStableIds(hasStableIds);
    }

    private synchronized void likeSupport(final Feed item, final int position)
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
                        feedList.set(position, item);
                        notifyItemChanged(position);

                        return;
                    }

                }
            });
        }
    }

    private synchronized void dislikeSupport(final Feed item, final int position)
    {
        synchronized (lockObj)
        {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Supports");
            query.setLimit(1);
            query.whereEqualTo("post", item.getOrigData());
            query.whereEqualTo("user", ParseUser.getCurrentUser());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0)
                        return;;
                    ParseObject support = objects.get(0);
                    support.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            item.isSupport = false;
                            item.setnSupportCnt(item.getnSupportCnt() - 1);
                            feedList.set(position, item);
                            notifyItemChanged(position);
                        }
                    });
                }
            });
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public MediaController mediaController;
        public FrameLayout videoFrame;
        public CircularImageView civPhoto;
        public TextView tvFeedName;
        public TextView tvFeedTime;
        public TextView tvFeedText;
        public TextView tvAnswer1;
        public TextView tvAnswer2;
        public TextView tvFeedSupport;
        public TextView tvFeedResponse;
        public TextView tvAccepted;
        public ImageView ivShare;
        public ImageView ivFeedSupport;
        public ImageView ivFeedComment;
        public ImageView ivPostImage;
        public CircularImageView playButton;
        public VideoView ivPostVideo;
        public LinearLayout bodyLayout, llResponse;

        public ViewHolder(View itemView) {
            super(itemView);
            bodyLayout = (LinearLayout) itemView.findViewById(R.id.bodyLayout);
            llResponse = (LinearLayout) itemView.findViewById(R.id.llResponse);
            civPhoto = (CircularImageView) itemView.findViewById(R.id.civPhoto);
            tvFeedName = (TextView) itemView.findViewById(R.id.tvFeedName);
            tvFeedTime = (TextView) itemView.findViewById(R.id.tvFeedTime);
            tvFeedText = (TextView) itemView.findViewById(R.id.tvFeedText);
            tvAnswer1 = (TextView) itemView.findViewById(R.id.tvAnswer1);
            tvAnswer2 = (TextView) itemView.findViewById(R.id.tvAnswer2);
            tvFeedSupport = (TextView) itemView.findViewById(R.id.tvSupport);
            tvFeedResponse = (TextView) itemView.findViewById(R.id.tvResponse);
            tvAccepted = (TextView) itemView.findViewById(R.id.tvAccepted);
            ivShare = (ImageView) itemView.findViewById(R.id.ivShare);
            ivFeedSupport = (ImageView) itemView.findViewById(R.id.ivFeedFavorite);
            ivFeedComment = (ImageView) itemView.findViewById(R.id.ivFeedComment);
            ivPostImage = (ImageView) itemView.findViewById(R.id.post_image);
            ivPostVideo = (VideoView) itemView.findViewById(R.id.post_video);
            videoFrame = (FrameLayout) itemView.findViewById(R.id.video_frame);
            playButton = (CircularImageView) itemView.findViewById(R.id.play_button);
        }
    }

    /*
    public void notifyUpdate(Feed item, int Index)  {
        ExecutorService executor = Executors.newCachedThreadPool();
        Runnable thread1 = new Runnable() {
            @Override
            public void run() {
                checkForPostImage(item.getOrigData(), item, Index);
            }
        };

        executor.submit(thread1);

        Runnable thread2 = new Runnable() {
            @Override
            public void run() {
                checkForPostVideo(item.getOrigData(), item, Index);
            }
        };

        executor.submit(thread2);

        Runnable thread3 = new Runnable() {
            @Override
            public void run() {
                GetAvatarPhotoFromData(item.getpUser(), item, Index);
            }
        };

        executor.submit(thread3);

        Runnable thread4 = new Runnable() {
            @Override
            public void run() {
                loadSupportData(item.getOrigData(), item, Index);

            }
        };

        executor.submit(thread4);

        Runnable thread5 = new Runnable() {
            @Override
            public void run() {
                loadCommentData(item.getOrigData(), item, Index);
            }
        };

        executor.submit(thread5);
    }
    */

    public void GetAvatarPhotoFromData(ParseUser user, Feed item, int Position)
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
                        if (bmp != null)
                            bmp = null;

                        if (item.updateCnt == 5)
                            notifyItemChanged(Position);
                    }
                });
            }
        });

    }

    public void checkForPostImage(ParseObject object, Feed item, int position)
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
                bmp = null;

                if (item.updateCnt == 5)
                    notifyItemChanged(position);

            }
        });
    }


    public void checkForPostVideo(ParseObject object, Feed item, int position)
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
            notifyItemChanged(position);


    }

    public void loadSupportData(ParseObject object, Feed item, int position) {
        if (item.getnSupportCnt() != -1)
            return;

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
                    notifyItemChanged(position);

            }
        });

    }


    public   void loadCommentData(ParseObject object, Feed item, int position) {
        if (item.getnCommentCnt() != -1)
            return;

        ParseQuery<ParseObject> queryComm = ParseQuery.getQuery("Comments");
        queryComm.whereEqualTo("post", object);

        try {
            int commCnt = queryComm.count();

            item.setnCommentCnt(commCnt);
            item.updateCnt++;
            if (item.updateCnt == 5)
                notifyItemChanged(position);

        } catch (ParseException e)
        {

        }


    }

    public class AvatarRunnable implements Runnable {
        private Feed item;
        private int position;

        public AvatarRunnable(Feed _item, int _pos)
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
        private Feed item;
        private int position;

        public CheckPostRunnable(Feed _item, int _pos)
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
        private Feed item;
        private int position;

        public CheckVideoRunnable(Feed _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {

            checkForPostVideo(item.getOrigData(), item, position);
        }
    }

    public class loadSupportRunnable implements Runnable {
        private Feed item;
        private int position;

        public loadSupportRunnable(Feed _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {

            loadSupportData(item.getOrigData(), item, position);
        }
    }

    public class loadCommentRunnable implements Runnable {
        private Feed item;
        private int position;

        public loadCommentRunnable(Feed _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {

            loadCommentData(item.getOrigData(), item, position);
        }
    }

}