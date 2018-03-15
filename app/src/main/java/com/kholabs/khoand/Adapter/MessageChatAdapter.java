package com.kholabs.khoand.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kholabs.khoand.Model.BaseMessage;
import com.kholabs.khoand.R;
import com.parse.ParseUser;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;

/**
 * Created by LiCholMin on 1/6/2018.
 */

public class MessageChatAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private ArrayList<BaseMessage> mMessageList;
    private ParseUser currentUser;

    public interface MessageItemListener {
        public void goToProfileFragment(BaseMessage item);
    }

    private MessageItemListener caller;

    public MessageChatAdapter(Context _context, ArrayList<BaseMessage>  _items, MessageItemListener _caller) {
        mContext = _context;
        mMessageList = _items;
        currentUser = ParseUser.getCurrentUser();
        this.caller = _caller;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        BaseMessage message = mMessageList.get(position);
        if (message.getSendUser().getObjectId().equals(currentUser.getObjectId()))
            return VIEW_TYPE_MESSAGE_SENT;
        else
            return VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_send, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseMessage message = mMessageList.get(position);

        switch (holder.getItemViewType())
        {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder)holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder)holder).bind(message);
                break;
        }
    }

    public void setListItems(ArrayList<BaseMessage> list) {
        this.mMessageList = list;
    }


    public void addItem(BaseMessage msgItem)
    {
        mMessageList.add(msgItem);
    }

    public void setItem(int position, BaseMessage _item)
    {
        mMessageList.set(position, _item);
    }

    public void addItems(ArrayList<BaseMessage> threadItems)
    {
        for(int i = 0; i< threadItems.size(); i++)
        {
            mMessageList.add(threadItems.get(i));
        }
    }

    public void clearAll() {
        if (mMessageList != null)
        {
            try
            {
                mMessageList.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            mMessageList = new ArrayList<BaseMessage>();
        }
        notifyDataSetChanged();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        CircularImageView civPhoto;
        ImageView ivFavourite;
        TextView messageText, timeText, nameText;

        SentMessageHolder(View itemView) {
            super(itemView);

            civPhoto = (CircularImageView) itemView.findViewById(R.id.civPhoto);

            ivFavourite = (ImageView) itemView.findViewById(R.id.ivFavorite);
            nameText = (TextView) itemView.findViewById(R.id.tvUserName);
            messageText = (TextView) itemView.findViewById(R.id.tvMsgText);
            timeText = (TextView) itemView.findViewById(R.id.tvMsgTime);
        }

        void bind(BaseMessage message) {
            messageText.setText(message.getMessage());
            nameText.setText(message.getName());
            timeText.setText(message.getTimeAt());

            if (!message.isAvatarLoaded)
            {
                if (message.getAvatar() != null) {
                    civPhoto.setImageBitmap(message.getAvatar());
                    message.isAvatarLoaded = true;
                    message.setAvatar(null);
                }
                else
                {
                    civPhoto.setImageResource(R.drawable.blankprofile);
                }
            }

            if (message.verifedTherapist == true && message.isTherapist)
                ivFavourite.setVisibility(View.VISIBLE);
            else
                ivFavourite.setVisibility(View.GONE);

            civPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    caller.goToProfileFragment(message);
                }
            });
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        CircularImageView civPhoto;
        ImageView ivFavourite;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            civPhoto = (CircularImageView) itemView.findViewById(R.id.civPhoto);
            ivFavourite = (ImageView) itemView.findViewById(R.id.ivFavorite);
            nameText = (TextView) itemView.findViewById(R.id.tvUserName);
            messageText = (TextView) itemView.findViewById(R.id.tvMsgText);
            timeText = (TextView) itemView.findViewById(R.id.tvMsgTime);
        }

        void bind(BaseMessage message) {
            messageText.setText(message.getMessage());
            nameText.setText(message.getName());
            timeText.setText(message.getTimeAt());

            if (!message.isAvatarLoaded)
            {
                if (message.getAvatar() != null) {
                    civPhoto.setImageBitmap(message.getAvatar());
                    message.isAvatarLoaded = true;
                    message.setAvatar(null);
                }
                else
                {
                    civPhoto.setImageResource(R.drawable.blankprofile);
                }
            }

            if (message.verifedTherapist == true && message.isTherapist)
                ivFavourite.setVisibility(View.VISIBLE);
            else
                ivFavourite.setVisibility(View.GONE);

            civPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    caller.goToProfileFragment(message);
                }
            });
        }
    }
}