package com.kholabs.khoand.Fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Adapter.MessageChatAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshBase;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshRecyclerView;
import com.kholabs.khoand.Interface.FindThreadCallBack;
import com.kholabs.khoand.Model.BaseMessage;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.ParseUtil.NotificationUtils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MessageChatFragment extends Fragment implements View.OnClickListener, MainActivity.onKeyBackPressedListener, MessageChatAdapter.MessageItemListener{
    private ParseUser inc_user;
    private ParseObject inc_thread;
    private ParseUser curr_user;

    private ArrayList<BaseMessage> messageItems = new ArrayList<>();
    private MessageChatAdapter adapter;
    private PullToRefreshRecyclerView msgRecycler;
    private LinearLayoutManager linearLayoutManager;
    private View rootView;
    private TextView tvNoMessage, tvMsgSend;
    private LinearLayout llBack;
    private EditText etMessage;

    private int loadCount = 0;
    private int itemsPerPage = 15;
    private int startPos = 0;
    private int endPos = 0;

    private ExecutorService listExecutor;
    private ExecutorService executor;
    private List<Runnable> threads = null;
    //private Bitmap myAvatar;
    //private Bitmap otherAvatar;

    public MessageChatFragment() {
    }
    
    public static MessageChatFragment newInstance() {
        MessageChatFragment fragment = new MessageChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.inc_thread = MyApp.getInstance().getMsgThread();
        this.inc_user = MyApp.getInstance().getShareUser();
        this.curr_user = ParseUser.getCurrentUser();

        //this.myAvatar = null;
        //this.otherAvatar = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_chat, container, false);
            ((MainActivity)getActivity()).setOnkeyBackPressedListener(this);
            llBack = (LinearLayout)rootView.findViewById(R.id.llBack);
            etMessage = (EditText)rootView.findViewById(R.id.etComment);
            tvMsgSend = (TextView)rootView.findViewById(R.id.tvChat);

            msgRecycler = (PullToRefreshRecyclerView) rootView.findViewById(R.id.recycle_message_list);
            linearLayoutManager = new LinearLayoutManager(getActivity());
            msgRecycler.getRefreshableView().setLayoutManager(linearLayoutManager);
            tvNoMessage = (TextView) rootView.findViewById(R.id.txtNoMessage);
            tvNoMessage.setVisibility(View.GONE);

            adapter = new MessageChatAdapter(getActivity(), messageItems, MessageChatFragment.this);

            msgRecycler.getRefreshableView().setHasFixedSize(true);
            msgRecycler.getRefreshableView().setAdapter(adapter);
            msgRecycler.setScrollingWhileRefreshingEnabled(true);
            msgRecycler.setMode(PullToRefreshBase.Mode.BOTH);

            initEvent();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadCount = 0;
                    setupThread();
                }
            }, 500);


        }
        else
        {
            loadCount = 0;
            setupThread();
        }


        IntentFilter msgReceiverIntent = new IntentFilter();
        msgReceiverIntent.addAction("android.intent.action.IM_NEW_MSG");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, msgReceiverIntent);

        return rootView;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocalBroadcastManager.getInstance((getActivity())).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //executor.shutdownNow();
    }

    private void initEvent() {
        llBack.setOnClickListener(this);
        tvMsgSend.setOnClickListener(this);

        msgRecycler.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadCount++;
                setupThread();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {

            }
        });
    }

    private void setupThread() {
        if (getActivity() == null)
            return;

        if (inc_thread != null)
        {
            loadMessages();
            return;
        }

        if (inc_user != null)
        {
            findOwnerWithCompletition(new FindThreadCallBack() {
                @Override
                public void onCompletition(ParseObject pThread) {
                    if (pThread != null)
                    {
                        inc_thread = pThread;
                        loadMessages();
                        return;
                    }

                    ParseObject newThread = new ParseObject("Threads");
                    newThread.put("threadOwner", curr_user);
                    ArrayList<String> peoples = new ArrayList<>();
                    peoples.add(inc_user.getObjectId());

                    newThread.put("people", peoples);
                    newThread.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null)
                                return;
                            inc_thread = newThread;
                            loadMessages();
                        }
                    });
                }
            });
        }
    }

    private void loadMessages()
    {
        if (inc_thread == null) { return; }
        if (curr_user == null) { return; }

        if (messageItems != null && loadCount == 0)
            messageItems.clear();

        adapter.clearAll();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Messages");
        query.setLimit(itemsPerPage);
        query.whereEqualTo("thread", inc_thread);
        query.orderByDescending("createdAt");
        query.setSkip(loadCount * itemsPerPage);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0) {
                    msgRecycler.onRefreshComplete();
                    if (loadCount == 0)
                        noMessage();
                    return;
                }

                msgRecycler.onRefreshComplete();
                ArrayList<BaseMessage> tempItems = new ArrayList<>();

                for (ParseObject message : objects) {
                    BaseMessage item = GetDataFromParseObject(message);
                    tempItems.add(item);
                }

                if (tempItems.size() == 1)
                {
                    messageItems.add(0, tempItems.get(0));
                    startPos = 0;
                    endPos = 1;
                    adapter.setListItems(messageItems);
                    adapter.notifyItemInserted(0);
                    updateAvatarTask();
                    if (loadCount > 0)
                        scrollToTopPosition(300);
                }
                else if (tempItems.size() > 1)
                {
                    try {
                        Collections.sort(tempItems, memberArryComparator);
                    } catch (Exception ev) {
                        ev.printStackTrace();
                    }

                    messageItems.addAll(0, tempItems);
                    startPos = 0;
                    endPos = tempItems.size();

                    adapter.setListItems(messageItems);
                    adapter.notifyItemRangeInserted(0, tempItems.size());
                    updateAvatarTask();
                    if (loadCount == 0)
                        scrollToLastPosition(300);
                    else
                        scrollToTopPosition(300);
                }

            }
        });
    }

    private void loadNewMessage()
    {
        if (inc_thread == null) { return; }
        if (curr_user == null) { return; }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Messages");
        query.setLimit(1);
        query.whereEqualTo("thread", inc_thread);
        query.orderByDescending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0) {
                    msgRecycler.onRefreshComplete();
                    return;
                }

                msgRecycler.onRefreshComplete();
                for (ParseObject message : objects) {
                    BaseMessage item = GetDataFromParseObject(message);
                    messageItems.add(item);
                }

                if (messageItems.size() == 1)
                {
                    startPos = adapter.getItemCount();
                    endPos = adapter.getItemCount() + 1;

                    adapter.addItem(messageItems.get(0));
                    adapter.notifyItemChanged(adapter.getItemCount());
                    updateAvatarTask();
                    scrollToLastPosition(300);
                }
            }
        });
    }

    private void noMessage() {
        tvNoMessage.setText("No Messages");
        tvNoMessage.setVisibility(View.VISIBLE);
    }

    private BaseMessage GetDataFromParseObject(ParseObject msg)
    {
        BaseMessage newItem = new BaseMessage();
        newItem.setMsgObject(msg);
        newItem.setMessage(msg.getString("message"));
        ParseUser senduser = msg.getParseUser("sender");
        newItem.setSendUser(senduser);
        if (senduser.has("name"))
            newItem.setName(senduser.getString("name"));
        else if (senduser.getObjectId().equals(curr_user.getObjectId()))
            newItem.setName(curr_user.getString("name"));

        Date dateCreated = msg.getCreatedAt();
        newItem.setCreateAt(dateCreated);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateToday = new Date();

        String strToday = sdf.format(dateToday);
        String strCreated = sdf.format(dateCreated);
        if (strToday.equals(strCreated))
        {
            SimpleDateFormat ttf = new SimpleDateFormat("h:mm a");
            newItem.setTimeAt(ttf.format(dateCreated));
        }
        else
        {
            SimpleDateFormat mtf = new SimpleDateFormat("E d MMM yyyy - h:mm a");
            newItem.setTimeAt(mtf.format(dateCreated));
        }

        return newItem;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llBack:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
                break;
            case R.id.tvChat:
                actionSendMessage();
                break;
        }
    }

    @Override
    public void onBack() {
        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
        ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
    }

    @Override
    public void goToProfileFragment(BaseMessage item) {
        if (item == null) return;

        MyApp.getInstance().setFromParent(ConstValues.ProfileFrom.fromMessage);
        MyApp.getInstance().setShareUser(item.getSendUser());
    }

    public class AvatarWrapper {
        public BaseMessage item;
        public int position;
    }

    public void GetAvatarPhotoFromData(ParseUser user, BaseMessage item, int Position)
    {
        if (item.getAvatar() != null) {
            return;
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
                        /*
                        if (user.getObjectId().equals(curr_user.getObjectId()))
                            myAvatar = bmp;
                        else
                            otherAvatar = bmp;
                        */
                        bmp = null;
                        messageItems.set(Position, item);
                        adapter.setItem(Position, item);
                        adapter.notifyItemChanged(Position);
                    }
                });
            }
        });

    }

    public void notifyUpdate(BaseMessage item, int nPos)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AvatarRunnable myRunnable = new AvatarRunnable(item, nPos);
        executor.submit(myRunnable);
    }

    public void updateAvatarTask()
    {
        if (messageItems == null || messageItems.size() == 0)
            return;

        //listExecutor = Executors.newFixedThreadPool(100);
        for (int i=startPos; i<endPos; i++)
        {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            BaseMessage item = messageItems.get(i);
            AvatarRunnable myRunnable = new AvatarRunnable(item, i);
            executor.submit(myRunnable);
            //AvatarRunnable myRunnable = new AvatarRunnable(item, i);
            //listExecutor.execute(myRunnable);
        }
    }

    public void findOwnerWithCompletition(FindThreadCallBack mCallBack)
    {
        ParseQuery<ParseObject> owner_us = ParseQuery.getQuery("Threads");
        owner_us.whereEqualTo("threadOwner", ParseUser.getCurrentUser());
        owner_us.whereContains("people", inc_user.getObjectId());

        ParseQuery<ParseObject> owner_them = ParseQuery.getQuery("Threads");
        owner_them.whereEqualTo("threadOwner", inc_user);
        owner_them.whereContains("people", ParseUser.getCurrentUser().getObjectId());

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(owner_us);
        queries.add(owner_them);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.orderByDescending("updatedAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0) {
                    mCallBack.onCompletition(null);
                    return;
                }

                ParseObject thread = objects.get(0);
                mCallBack.onCompletition(thread);
            }
        });
    }


    private void actionSendMessage()
    {
        String sendMsg = etMessage.getText().toString();
        if (sendMsg.equals("")) {
            MyApp.getInstance().alertDisplayer("Error", "You must type a message");
            return;
        }

        if (inc_thread == null)
            return;

        tvMsgSend.setAlpha(0.3f);
        tvMsgSend.setEnabled(false);

        ParseObject messageobj = new ParseObject("Messages");
        messageobj.put("thread", inc_thread);
        messageobj.put("message", sendMsg);
        messageobj.put("sender", curr_user);

        messageobj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                tvMsgSend.setAlpha(1.0f);
                tvMsgSend.setEnabled(true);
                if (e != null)
                    return;

                etMessage.setText("");
                BaseMessage newItem = new BaseMessage();
                newItem.setMessage(sendMsg);
                Date currDate = Calendar.getInstance().getTime();
                SimpleDateFormat ttf = new SimpleDateFormat("h:mm a");
                newItem.setTimeAt(ttf.format(currDate));
                newItem.setCreateAt(currDate);
                newItem.setSendUser(curr_user);
                newItem.setMsgObject(null);
                newItem.setName(curr_user.getString("name"));

                messageItems.add(newItem);

                int nPos = adapter.getItemCount();
                startPos = nPos;
                adapter.addItem(newItem);
                adapter.notifyItemInserted(nPos);

                scrollToLastPosition(300);
                tvNoMessage.setVisibility(View.GONE);

                notifyUpdate(newItem, nPos);

                ParseUser threadOwner = inc_thread.getParseUser("threadOwner");
                ParseUser user = null;

                String tMessage = String.format("%s: %s", curr_user.getString("name"), sendMsg);

                if (!threadOwner.getObjectId().equals(curr_user.getObjectId())) {
                    user = threadOwner;
                    NotificationUtils.sendPush(tMessage, user, "Threads", inc_thread);
                    return;
                }
                else
                {
                    List<String> people = inc_thread.getList("people");
                    if ( people == null || people.size() == 0)
                        return;
                    String userid = people.get(0);
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.setLimit(1);
                    query.whereEqualTo("objectId", userid);
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if (e != null || objects == null || objects.size() == 0)
                                return;
                            ParseUser user1 = objects.get(0);
                            NotificationUtils.sendPush(tMessage, user1, "Threads", inc_thread);
                        }
                    });
                }
            }
        });
    }

    private void scrollToLastPosition(int delayTimeMills)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null)
                    msgRecycler.getRefreshableView().smoothScrollToPosition(adapter.getItemCount()-1);

            }
        }, delayTimeMills);
    }

    private void scrollToTopPosition(int delayTimeMills)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null)
                    msgRecycler.getRefreshableView().smoothScrollToPosition(0);

            }
        }, delayTimeMills);
    }

    private final static Comparator<BaseMessage> memberArryComparator = new Comparator<BaseMessage>()
    {

        @Override
        public int compare(BaseMessage lhs, BaseMessage rhs) {
            if (lhs.getCreateAt() == null || rhs.getCreateAt() == null)
                return 0;
            return lhs.getCreateAt().compareTo(rhs.getCreateAt());
        }
    };

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String notiObjId = bundle.getString("objectId", "");

            if (!inc_thread.getObjectId().equals(notiObjId))
                return;

            loadNewMessage();
        }
    };

    public class AvatarRunnable implements Runnable {
        private BaseMessage item;
        private int position;

        public AvatarRunnable(BaseMessage _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {
            GetAvatarPhotoFromData(item.getSendUser(), item, position);
        }
    }
}
