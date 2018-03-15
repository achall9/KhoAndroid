package com.kholabs.khoand.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.jarvanmo.exoplayerview.media.MediaSourceCreator;
import com.jarvanmo.exoplayerview.media.SimpleMediaSource;
import com.jarvanmo.exoplayerview.ui.ExoVideoView;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.CustomView.PopupWindow.PhotoFullPopupWindow;
import com.kholabs.khoand.Model.Comment;
import com.kholabs.khoand.R;
import com.parse.Parse;
import com.parse.ParseObject;
import com.pkmmte.view.CircularImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by LiCholMin on 12/17/2017.
 */

public class CommentAdapter extends BaseAdapter {

    private List<Comment> commentList;
    private LayoutInflater inflater;
    private Context mContext = null;
    private TrackSelector track;
    private LoadControl control;

    public interface CommentItemListener {
        public void editItemListener(Comment item, int position);
        public void goToProfileFragment(Comment item, int position);
    }

    private  CommentItemListener caller;

    public CommentAdapter(Context _context, CommentItemListener _caller, TrackSelector _track, LoadControl _control) {
        commentList = new ArrayList<Comment>();
        this.mContext = _context;
        this.caller = _caller;
        this.track = _track;
        this.control = _control;
        this.inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setListItems(List<Comment> list) {
        this.commentList = list;
    }

    public void addItem(Comment contactItem)
    {
        commentList.add(contactItem);
    }

    public void setItem(int position, Comment _item)
    {
        commentList.set(position, _item);
    }

    public void addItems(List<Comment> contactItems)
    {
        for(int i=0;i<contactItems.size();i++)
        {
            commentList.add(contactItems.get(i));
        }
    }

    public void clearAll() {
        if (commentList != null)
        {
            try
            {
                commentList.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            commentList = new ArrayList<Comment>();
        }
        notifyDataSetChanged();
    }


    public void removeItem(int position) { commentList.remove(position); }

    @Override
    public int getCount() {
        return commentList == null ? 0 : commentList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder ;

        final Comment data = commentList.get(position);
        if (data == null)
            return null;

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.adaptercomment, null);
            mHolder = new ViewHolder();

            mHolder.llContent = (LinearLayout) convertView.findViewById(R.id.ll_content);
            mHolder.llAnswered = (LinearLayout) convertView.findViewById(R.id.llAnswered);
            mHolder.civPhoto = (CircularImageView) convertView.findViewById(R.id.civPhoto);

            mHolder.tvCommentName = (TextView) convertView.findViewById(R.id.tvCommentName);
            mHolder.tvCommentTime = (TextView) convertView.findViewById(R.id.tvCommentTime);
            mHolder.tvCommentText = (TextView) convertView.findViewById(R.id.tvCommentText);
            mHolder.ivVerified = (ImageView) convertView.findViewById(R.id.ivVerified);

            mHolder.ivPostImage = (ImageView) convertView.findViewById(R.id.post_image);

            mHolder.ivPostVideo = (ExoVideoView) convertView.findViewById(R.id.post_video);
            mHolder.videoFrame = (RelativeLayout) convertView.findViewById(R.id.video_frame);
            mHolder.playButton = (CircularImageView) convertView.findViewById(R.id.play_button);
            mHolder.ivAccepted = (ImageView) convertView.findViewById(R.id.ivAccepted);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder)convertView.getTag();
        }

        mHolder.refreshView(data);

        mHolder.tvCommentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                caller.editItemListener(data, position);
            }
        });

        mHolder.ivPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PhotoFullPopupWindow(mContext, R.layout.popup_photo_full, view, data.getPostImage());
            }
        });

        mHolder.civPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                caller.goToProfileFragment(data, position);
            }
        });

        return convertView;
    }


    public class ViewHolder {
        private int position = 0;

        private LinearLayout llContent, llAnswered;
        private RelativeLayout videoFrame;
        private CircularImageView playButton;
        private ExoVideoView ivPostVideo;
        private ImageView ivPostImage;

        private CircularImageView civPhoto;
        private ImageView ivVerified;
        private ImageView ivAccepted;

        private TextView tvCommentName;
        private TextView tvCommentText;
        private TextView tvCommentTime;
        public SimpleExoPlayer player;

        public void refreshView(Comment item)
        {
            tvCommentText.setText(item.getContent());
            tvCommentName.setText(item.getName());
            tvCommentTime.setText(item.getDate());

            if (item.getAvatar() != null) {
                civPhoto.setImageBitmap(item.getAvatar());
                item.isImageLoaded = true;
            } else
                civPhoto.setImageResource(R.drawable.blankprofile);

            if (item.getPostImage() != null)
            {
                ivPostImage.setImageBitmap(item.getPostImage());
                item.isPostImageLoaded = true;
                ivPostImage.setVisibility(View.VISIBLE);
            }
            else
                ivPostImage.setVisibility(View.GONE);

            if (item.getPostVideoPath().equals("") == false) {
                videoFrame.setVisibility(View.VISIBLE);
                playButton.setImageResource(R.drawable.player_play);
                prepareMediaForView(item);
            }
            else
                videoFrame.setVisibility(View.GONE);

            if (item.verifedTherapist == true && item.isTherapist)
            {
                tvCommentText.setTextColor(Color.WHITE);
                llContent.setBackgroundResource(R.drawable.layout_bg_blue);
                ivVerified.setVisibility(View.VISIBLE);
            }
            else {
                llContent.setBackgroundResource(R.drawable.layout_bg);
                ivVerified.setVisibility(View.GONE);
            }

            checkAccepted(item);
        }

        public void checkAccepted(Comment item)
        {
            ParseObject comment = item.getOrigData();
            ParseObject post = comment.getParseObject("post");
            ParseObject accepted = post.getParseObject("acceptedComment");

            if (accepted != null && accepted.getObjectId().equals(comment.getObjectId()))
                llAnswered.setVisibility(View.VISIBLE);
            else
                llAnswered.setVisibility(View.GONE);
        }


        public void pausePlayer() {
            if (player != null)
            {
                player.setPlayWhenReady(false);
                player.getPlaybackState();
            }

        }

        public void startPlayer() {
            if (player != null) {
                player.setPlayWhenReady(true);
                player.getPlaybackState();
            }
        }

        public void destroyPlayer()
        {
            if (player != null)
            {
                player.release();
                ivPostVideo.releasePlayer();
            }
        }


        private void prepareMediaForView(Comment item)
        {
            if (ivPostVideo.getPlayer() != null)
                return;

            player = ExoPlayerFactory.newSimpleInstance(mContext, track, control);
            ivPostVideo.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            ivPostVideo.requestFocus();
            ivPostVideo.setPlayer(player);
            ivPostVideo.hideController();
            ivPostVideo.setUseController(false);

            Uri mp4VideoUri = Uri.parse(item.getPostVideoPath());
            DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, ".App.MyApp"), bandwidthMeterA);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
            player.prepare(videoSource);
            player.setPlayWhenReady(false);
            player.seekTo(1500);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (player.getPlayWhenReady() == false){
                        player.setPlayWhenReady(true);
                        playButton.setImageResource(R.drawable.player_pause);
                    } else
                    {
                        player.setPlayWhenReady(false);
                        playButton.setImageResource(R.drawable.player_play);
                    }

                }
            });
        }

        public void setViewAlpha(int alpha)
        {
            ColorStateList txtColorList =  tvCommentText.getTextColors();
            ColorStateList hintColorHist = tvCommentText.getHintTextColors();

            tvCommentText.setTextColor(txtColorList.withAlpha(alpha));
            tvCommentText.setHintTextColor(hintColorHist.withAlpha(alpha));

        }
    }
}