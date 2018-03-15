package com.kholabs.khoand.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.kholabs.khoand.Model.Feed;
import com.kholabs.khoand.Model.Notification;
import com.kholabs.khoand.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Aladar-PC2 on 2/21/2018.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>  {

    private Context mContext;
    private ArrayList<Notification> datas;
    private NotificationTapListener caller;

    public interface NotificationTapListener {
        public void goToMessageChat(ParseObject thread);
        public void goToPostPage(Feed data, String commentId);
    }

    public NotificationAdapter(Context _mContext, NotificationTapListener _caller)
    {
        this.mContext = _mContext;
        this.caller = _caller;
    }

    public void setListItems(ArrayList<Notification> list) {
        this.datas = list;
    }
    public void addItem(Notification msgItem)
    {
        datas.add(msgItem);
    }

    public void setItem(int position, Notification _item)
    {
        datas.set(position, _item);
    }

    public void addItems(ArrayList<Notification> threadItems)
    {
        for(int i = 0; i< threadItems.size(); i++)
        {
            datas.add(threadItems.get(i));
        }
    }

    public void clearAll() {
        if (datas != null)
        {
            try
            {
                datas.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            datas = new ArrayList<Notification>();
        }
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapternotification, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Notification item = datas.get(position);
        holder.tvTitle.setText(item.getStrTitle());
        holder.tvContent.setText(item.getStrContent());
        holder.tvTime.setText(item.getStrDate());

        holder.bodyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tappedNotification(item.getData());
            }
        });


    }
    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    private void tappedNotification(HashMap<String, String> userInfo)
    {
        String nClass = userInfo.get("class");
        String objId = userInfo.get("objectId");

        if (nClass == null || objId == null)
            return;

        String queryClass = nClass;
        if (nClass.equals("Messages"))
            queryClass = "Threads";
        else if (nClass.equals("Comments") || nClass.equals("Posts"))
            queryClass = "Posts";

        ParseQuery<ParseObject> query = ParseQuery.getQuery(queryClass);
        if (queryClass.equals("Threads"))
            query.whereEqualTo("objectId", objId);
        if (queryClass.equals("Posts"))
        {
            query.whereEqualTo("objectId", objId);
            query.include("owner");
        }

        String finalQueryClass = queryClass;
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0)
                    return;

                ParseObject object = objects.get(0);
                if (finalQueryClass.equals("Threads"))
                {
                    caller.goToMessageChat(object);
                }
                else if (finalQueryClass.equals("Posts"))
                {
                    String commentId = "";
                    if (userInfo.keySet().contains("commentId"))
                        commentId = userInfo.get("commentId");

                    Feed item = GetDataFromParseObject(object);
                    caller.goToPostPage(item, commentId);

                }
            }
        });
    }

    private Feed GetDataFromParseObject(ParseObject object)
    {
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
        newItem.isNotification = true;
        return newItem;

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout bodyLayout;
        private TextView tvTitle;
        private TextView tvContent;
        private TextView tvTime;

        public ViewHolder(View itemView) {
            super(itemView);
            bodyLayout = (LinearLayout) itemView.findViewById(R.id.bodyLayout);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvContent = (TextView) itemView.findViewById(R.id.tvContent);
            tvTime = (TextView) itemView.findViewById(R.id.tvDateTime);
        }
    }

}
