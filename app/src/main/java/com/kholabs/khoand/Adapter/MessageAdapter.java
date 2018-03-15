package com.kholabs.khoand.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Fragment.MessageChatFragment;
import com.kholabs.khoand.Interface.FetchMsgCallBack;
import com.kholabs.khoand.Interface.FindOwnerCallBack;
import com.kholabs.khoand.Interface.NotifyListener;
import com.kholabs.khoand.Model.BaseMessage;
import com.kholabs.khoand.Model.ThreadItem;
import com.kholabs.khoand.R;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LiCholMin on 1/6/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private int resource;
    private Context context;
    private ArrayList<ThreadItem> datas;
    private Object syncObj = new Object();
    private List<Runnable> threads = null;

    ItemChangeListener listener = new ItemChangeListener();

    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(View view, int position);
    }

    private final class ItemChangeListener implements NotifyListener {

        @Override
        public void OnAddItem(ThreadItem item, int position) {
            if (datas != null) {
                datas.set(position, item);
                notifyItemChanged(position);
            }
        }

        @Override
        public void OnUpdatedItem(ThreadItem item, int position) {
            if (datas != null) {
                datas.set(position, item);
                notifyItemChanged(position);
            }
        }
    }

    public MessageAdapter(Context _context, ArrayList<ThreadItem> _items, ItemClick _caller) {
        this.context = _context;
        this.datas = _items;
        this.itemClick = _caller;
    }

    public void setListItems(ArrayList<ThreadItem> list) {
        this.datas = list;
        notifyDataSetChanged();
        parseAndNotify();
    }

    public void parseAndNotify() {
        if (datas == null || datas.size() == 0)
            return;

        for (int i = 0; i < datas.size(); i++) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            AvatarRunnable obj = new AvatarRunnable(datas.get(i), i);
            executor.submit(obj);
        }
    }

    public void cancelExecutor()
    {
    }

    public void completeExecutor()
    {
    }

    public void addItem(ThreadItem msgItem) {
        datas.add(msgItem);
    }

    public void setItem(int position, ThreadItem _item) {
        datas.set(position, _item);
    }

    public void addItems(ArrayList<ThreadItem> threadItems) {
        for (int i = 0; i < threadItems.size(); i++) {
            datas.add(threadItems.get(i));
        }
    }

    public void clearAll() {
        if (datas != null) {
            try {
                datas.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            datas = new ArrayList<ThreadItem>();
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adaptermessage, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ThreadItem item = datas.get(position);
        holder.tvChatUser.setText(item.getName());
        holder.tvMsgTime.setText(item.getTimeAgo());
        holder.tvLatestMsg.setText(item.getLatestMsg());

        if (item.getAvatar() != null) {
            holder.civPhoto.setImageBitmap(item.getAvatar());
            item.isAvatarLoaded = true;
        }
        else
            holder.civPhoto.setImageResource(R.drawable.blankprofile);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
;                MyApp.getInstance().setMsgThread(item.getThread());

                if (itemClick != null)
                    itemClick.onClick(view, position);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return datas.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircularImageView civPhoto;
        private TextView tvChatUser;
        private TextView tvLatestMsg;
        private TextView tvMsgTime;

        public ViewHolder(View itemView) {
            super(itemView);
            civPhoto = (CircularImageView) itemView.findViewById(R.id.civPhoto);
            tvChatUser = (TextView) itemView.findViewById(R.id.tvUserName);
            tvLatestMsg = (TextView) itemView.findViewById(R.id.tvLatestMsg);
            tvMsgTime = (TextView) itemView.findViewById(R.id.tvMsgTime);
        }
    }

    public class ThreadWrapper
    {
        public ThreadItem item;
        public int position;
        public ParseUser pUser;
    }

    public void loadThread(ThreadItem item, int position)
    {
        if (item.getpUser() == null)
            return;

        ExecutorService executor = Executors.newFixedThreadPool(3);
        Runnable thread1 = new Runnable() {
            @Override
            public void run() {
                MakeThreadName(item, position);
            }
        };

        executor.submit(thread1);

        Runnable thread2 = new Runnable() {
            @Override
            public void run() {
                GetAvatarTask(item, position);
            }
        };

        executor.submit(thread2);

        Runnable thread3 = new Runnable() {
            @Override
            public void run() {
                if (item.getMessage() == null)
                    FetchLatsetTask(item, position);
                else
                    updateMessageContents(item, position);
            }
        };

        executor.submit(thread3);

    }

    public void updateMessageContents(ThreadItem item, int position)
    {
        if (item.getMessage() == null)
            return;
        ParseObject message = item.getMessage();

        long createTime = message.getCreatedAt().getTime();
        item.setCreatedAt(message.getCreatedAt());
        String timeago = TimeAgo.using(createTime);
        timeago = timeago.replace("just now", "1s");
        timeago = timeago.replace("about an hour ago", "1h");
        timeago = timeago.replace("about a month ago", "1M");
        timeago = timeago.replace("about a year ago", "1Y");

        timeago = timeago.replace("years ago", "Y");
        timeago = timeago.replace("months ago", "M");
        timeago = timeago.replace(" days ago", "d");
        timeago = timeago.replace(" hours ago", "h");
        timeago = timeago.replace(" minutes ago", "mm");
        item.setTimeAgo(timeago);
        item.setLatestMsg(message.getString("message"));

        datas.set(position, item);
        notifyItemChanged(position);
    }

    private void MakeThreadName(ThreadItem item, int position) {
        ParseUser pUser = item.getpUser();
        String name = pUser.getString("name");
        item.setName(name);
        datas.set(position, item);
        notifyItemChanged(position);
    }

    public class AvatarWrapper
    {
        public ThreadItem item;
        public int position;
    }

    public void GetAvatarTask(ThreadItem item, int position)
    {
        ParseUser user = item.getpUser();

        if (item.getAvatar() != null)
            return;

        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null || object == null) {
                    return;
                }

                ParseFile pFile = object.getParseFile("avatar");

                if (pFile == null) {
                    return;
                }

                pFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e != null || data == null) {
                            return;
                        }

                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        item.setAvatar(bmp);
                        bmp = null;

                        setItem(position, item);
                        notifyItemChanged(position);
                    }
                });
            }
        });
    }

    public class GetAvatarTask extends AsyncTask<AvatarWrapper, AvatarWrapper, AvatarWrapper> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected AvatarWrapper doInBackground(AvatarWrapper... datas) {
            AvatarWrapper result = datas[0];
            ThreadItem item = result.item;
            ParseUser user = item.getpUser();
            int Position = result.position;

            if (item.getAvatar() != null) {
                return null;
            }

            user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e != null || object == null) {
                        return;
                    }

                    ParseFile pFile = object.getParseFile("avatar");

                    if (pFile == null) {
                        return;
                    }

                    pFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            if (e != null || data == null) {
                                return;
                            }

                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            item.setAvatar(bmp);
                            bmp = null;

                            result.item = item;
                            publishProgress(result);
                        }
                    });
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(AvatarWrapper result) {

        }

        @Override
        protected void onProgressUpdate(AvatarWrapper... values) {
            AvatarWrapper value = values[0];
            setItem(value.position, value.item);
            notifyItemChanged(value.position);
        }
    }


    public void FetchLatsetTask(ThreadItem item, int position)
    {
        ParseUser user = item.getpUser();

        if (item.getMessage() != null)
            return;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Messages");
        query.setLimit(1);
        query.whereEqualTo("thread", item.getThread());
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0) {
                    return;
                }

                ParseObject msg = objects.get(0);
                item.setMessage(msg);
                updateMessageContents(item, position);
                return;
            }
        });
    }

    public class FetchLatestMsgtask extends AsyncTask<AvatarWrapper, AvatarWrapper, AvatarWrapper> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected AvatarWrapper doInBackground(AvatarWrapper... datas) {
            AvatarWrapper data = datas[0];
            ThreadItem item = data.item;

            if (item.getMessage() != null)
                return  data;

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Messages");
            query.setLimit(1);
            query.whereEqualTo("thread", item.getThread());
            query.orderByDescending("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0) {
                        return;
                    }

                    ParseObject msg = objects.get(0);
                    item.setMessage(msg);
                    data.item = item;
                    publishProgress(data);
                    return;
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(AvatarWrapper result) {
            if (result != null)
                updateMessageContents(result.item, result.position);
        }

        @Override
        protected void onProgressUpdate(AvatarWrapper... values) {
            AvatarWrapper data = values[0];
            if (data != null)
                updateMessageContents(data.item, data.position);
        }
    }

    private void initThread(ThreadItem item, int Index)
    {
        ParseObject thread = item.getThread();

        ParseUser threadOwner = thread.getParseUser("threadOwner");
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (!threadOwner.getObjectId().equals(currentUser.getObjectId())) {
            item.setpUser(threadOwner);
            datas.set(Index, item);
            loadThread(item, Index);
        }
        else
        {
            List<String> people = thread.getList("people");
            if (people == null || people.size() == 0) {
                return;
            }

            String objId = people.get(0);
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.setLimit(1);
            query.whereEqualTo("objectId", objId);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0) {
                        return;
                    }

                    item.setpUser(objects.get(0));
                    datas.set(Index, item);
                    loadThread(item, Index);
                    return;

                }
            });
        }
    }

    private class InitThreadTask extends AsyncTask<ThreadWrapper, ThreadWrapper, ThreadWrapper>
    {
        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected ThreadWrapper doInBackground(ThreadWrapper... parseObjects) {
            ThreadWrapper threadObj = parseObjects[0];
            ParseObject thread = threadObj.item.getThread();

            ParseUser threadOwner = thread.getParseUser("threadOwner");
            ParseUser currentUser = ParseUser.getCurrentUser();

            if (!threadOwner.getObjectId().equals(currentUser.getObjectId())) {
                threadObj.pUser = threadOwner;
                return threadObj;
            }
            else
            {
                List<String> people = thread.getList("people");
                if (people == null || people.size() == 0) {
                    return null;
                }

                String objId = people.get(0);
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.setLimit(1);
                query.whereEqualTo("objectId", objId);
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if (e != null || objects == null || objects.size() == 0) {
                            return;
                        }

                        threadObj.pUser = objects.get(0);
                        publishProgress(threadObj);
                        return;

                    }
                });
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ThreadWrapper... values) {
            ThreadWrapper value = values[0];
            if (value != null)
            {
                ThreadItem item = value.item;
                item.setpUser(value.pUser);
                datas.set(value.position, item);
                loadThread(item, value.position);
            }
        }

        @Override
        protected void onPostExecute(ThreadWrapper result) {
            if (result != null)
            {
                ThreadItem item = result.item;
                item.setpUser(result.pUser);
                datas.set(result.position, item);
                loadThread(item, result.position);
            }
        }
    }

    public class AvatarRunnable implements Runnable {
        private ThreadItem item;
        private int position;

        public AvatarRunnable(ThreadItem _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {
            initThread(item, position);
        }
    }
}
