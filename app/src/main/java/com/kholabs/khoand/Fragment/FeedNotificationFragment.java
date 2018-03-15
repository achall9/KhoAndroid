package com.kholabs.khoand.Fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Adapter.NotificationAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.FragmentBase;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshBase;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshRecyclerView;
import com.kholabs.khoand.Model.Feed;
import com.kholabs.khoand.Model.Notification;
import com.kholabs.khoand.R;
import com.kholabs.khoand.Utils.MyUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FeedNotificationFragment extends FragmentBase implements View.OnClickListener, NotificationAdapter.NotificationTapListener{

    private View rootView;
    private ImageView ivCancel;
    private TextView tvClear, tvNoNotifications;

    private ArrayList<Notification> notifyItems = new ArrayList<Notification>();
    private NotificationAdapter adapter;
    private PullToRefreshRecyclerView mRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    private ParseUser currUser;

    public FeedNotificationFragment() {
    }

    public static FeedNotificationFragment newInstance(String param1, String param2) {
        FeedNotificationFragment fragment = new FeedNotificationFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_notification, container, false);

            mRecyclerView = (PullToRefreshRecyclerView) rootView.findViewById(R.id.listNotifications);
            linearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.getRefreshableView().setLayoutManager(linearLayoutManager);
            tvNoNotifications = (TextView) rootView.findViewById(R.id.txtNoNotifications);
            tvNoNotifications.setVisibility(View.GONE);
            ivCancel = (ImageView)rootView.findViewById(R.id.ivCancel);
            tvClear = (TextView)rootView.findViewById(R.id.tvClear);

            adapter = new NotificationAdapter(getActivity(), FeedNotificationFragment.this);
            initEvent();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.getRefreshableView().setHasFixedSize(true);
                    mRecyclerView.getRefreshableView().setAdapter(adapter);
                    mRecyclerView.setScrollingWhileRefreshingEnabled(true);
                    mRecyclerView.setMode(PullToRefreshBase.Mode.BOTH);

                    loadData();
                }
            }, 500);
        }

        IntentFilter notificationIntent = new IntentFilter();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNotificationReceiver, notificationIntent);
        return rootView;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocalBroadcastManager.getInstance((getActivity())).unregisterReceiver(mNotificationReceiver);
    }

    private void initEvent()
    {
        ivCancel.setOnClickListener(this);
        tvClear.setOnClickListener(this);

        mRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {

            }
        });

    }

    private void loadData()
    {
        if (notifyItems != null)
            notifyItems.clear();

        showDialog();
        currUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException err) {
                if (err != null) {
                    noNotifications();
                    hideDialog();
                    return;
                }

                List<HashMap<String, String>> notiObjArr = object.getList("notifications");

                if (notiObjArr == null || notiObjArr.size() == 0) {
                    noNotifications();
                    hideDialog();
                    return;
                }


                if (notiObjArr.size() > 1) {
                    try {
                        Collections.sort(notiObjArr, dateComparator);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


                for (int i=0; i<notiObjArr.size(); i++)
                {
                    HashMap<String, String> obj = notiObjArr.get(i);
                    Notification notItem = new Notification();
                    notItem.setStrTitle(obj.get("title"));
                    notItem.setStrContent(obj.get("body"));
                    notItem.setStrDate(obj.get("date"));
                    notItem.setCurrDate(MyUtils.convertDateFromStringMM(notItem.getStrDate()));
                    notItem.setData(obj);
                    notifyItems.add(notItem);
                }

                hideDialog();
                adapter.setListItems(notifyItems);
                adapter.notifyDataSetChanged();

                scrollToLastPosition(300);
            }
        });
    }

    private void scrollToLastPosition(int delayTimeMills)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null)
                    mRecyclerView.getRefreshableView().smoothScrollToPosition(adapter.getItemCount()-1);

            }
        }, delayTimeMills);
    }

    private void noNotifications()
    {
        tvNoNotifications.setVisibility(View.VISIBLE);
    }

    private void clearNotifications()
    {
        currUser.remove("notifications");
        MyApp.getInstance().pref.setNotificationBadges(0);
        showDialog();
        currUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                hideDialog();
                adapter.clearAll();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivCancel:
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                break;
            case R.id.tvClear:
                //Clear All Notification
                clearNotifications();
                break;
        }
    }

    @Override
    public void goToMessageChat(ParseObject msgThread) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();

        MyApp.getInstance().setMsgThread(msgThread);
        ((MainActivity)getActivity()).openMessageThread();
    }

    @Override
    public void goToPostPage(Feed feedObj, String ncObjId) {
        MyApp.getInstance().setPostFeed(feedObj);
        MyApp.getInstance().setNcObjectId(ncObjId);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();

        ((MainActivity)getActivity()).openPostPage();
    }

    /*
    private class loadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }
    */

    private final static Comparator<HashMap<String, String>> dateComparator = new Comparator<HashMap<String, String>>()
    {

        @Override
        public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
            Date lDate = MyUtils.convertDateFromStringMM(lhs.get("date"));
            Date rDate = MyUtils.convertDateFromStringMM(rhs.get("date"));

            if (lDate == null || rDate == null)
                return 0;
            return lDate.compareTo(rDate);
        }
    };

    private final BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String notiClass = bundle.getString("class", "");
            String notiAlert = bundle.getString("alert", "");
            String notiObjId = bundle.getString("objectId", "");

            Notification notItem = new Notification();
            notItem.setStrTitle(notiClass);
            notItem.setStrContent(notiAlert);

            Date nowTime = Calendar.getInstance().getTime();

            notItem.setStrDate(MyUtils.getFormatStringFromDate(nowTime));
            notItem.setCurrDate(nowTime);

            HashMap<String, String> objData = new HashMap<>();
            objData.put("class", notiClass);
            objData.put("objectId", notiObjId);

            if (notiClass.equals("Comments")) {
                String notiCommentId = bundle.getString("commentId", "");
                objData.put("commentId", notiCommentId);
            }

            notItem.setData(objData);
            notifyItems.add(notItem);
            adapter.notifyItemInserted(adapter.getItemCount());
        }
    };
}
