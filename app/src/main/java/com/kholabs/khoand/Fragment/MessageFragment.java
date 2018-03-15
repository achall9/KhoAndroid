package com.kholabs.khoand.Fragment;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Adapter.MessageAdapter;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.FragmentBase;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshBase;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshRecyclerView;
import com.kholabs.khoand.Model.ThreadItem;
import com.kholabs.khoand.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageFragment extends FragmentBase implements MessageAdapter.ItemClick {

    private ArrayList<ThreadItem> threadItems = new ArrayList<ThreadItem>();
    private MessageAdapter adapter;
    private PullToRefreshRecyclerView msgRecycler;
    private LinearLayoutManager linearLayoutManager;
    private View rootView;
    private TextView tvNoMessage;
    private loadDataTask myTask = null;

    public MessageFragment() {

    }

    public static MessageFragment newInstance() {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        rootView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_message, container, false);

            msgRecycler = (PullToRefreshRecyclerView) rootView.findViewById(R.id.listmessage);
            linearLayoutManager = new LinearLayoutManager(getActivity());
            msgRecycler.getRefreshableView().setLayoutManager(linearLayoutManager);
            tvNoMessage = (TextView) rootView.findViewById(R.id.txtNoMessage);
            tvNoMessage.setVisibility(View.GONE);

            adapter = new MessageAdapter(getActivity(), threadItems, MessageFragment.this);
            initEvent();

            msgRecycler.getRefreshableView().setHasFixedSize(true);
            msgRecycler.getRefreshableView().setAdapter(adapter);
            msgRecycler.setScrollingWhileRefreshingEnabled(true);
            msgRecycler.setMode(PullToRefreshBase.Mode.BOTH);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new loadDataTask().execute();
                }
            }, 500);
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
    public void onDestroy()
    {
        super.onDestroy();
        //if (adapter != null)
            //adapter.cancelExecutor();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //if (adapter != null)
            //adapter.completeExecutor();
    }

    @Override
    public void onPause()
    {
        super.onPause();;
    }

    private void initEvent() {
        msgRecycler.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                new loadDataTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {

            }
        });

    }

    public void noMessage()
    {
        tvNoMessage.setText("No Messages");
        tvNoMessage.setVisibility(View.VISIBLE);

    }

    private void AddAllThreads(ArrayList<ThreadItem> threads)
    {
        if (threads == null || threads.size() ==0) {
            noMessage();
            return;
        }

        if (threads.size() == 1)
        {
            tvNoMessage.setVisibility(View.GONE);
            ThreadItem item = threads.get(0);
            threadItems.add(item);
            adapter.setListItems(threadItems);
            return;
        }

        tvNoMessage.setVisibility(View.GONE);

        try {
            Collections.sort(threads, memberArryComparator);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (ThreadItem thread : threads)
            threadItems.add(thread);
        adapter.setListItems(threadItems);
    }

    @Override
    public void onClick(View view, int position) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.message_root_frame, new MessageChatFragment(), "BaseMessage")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class loadDataTask extends AsyncTask<Void, ArrayList<ThreadItem>, Void> {

        @Override
        protected void onPreExecute()
        {
            showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (threadItems != null)
                threadItems.clear();

            ParseUser currentUser = ParseUser.getCurrentUser();
            ParseQuery<ParseObject> queryOwner = ParseQuery.getQuery("Threads");
            queryOwner.whereEqualTo("threadOwner", currentUser);

            ParseQuery<ParseObject> queryContainer = ParseQuery.getQuery("Threads");
            queryContainer.whereContains("people", currentUser.getObjectId());

            List<ParseQuery<ParseObject>> queries = new ArrayList<>();
            queries.add(queryOwner);
            queries.add(queryContainer);

            ParseQuery<ParseObject> query = ParseQuery.or(queries);
            query.orderByDescending("updatedAt");
            query.include("threadOwner");

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0) {
                        ArrayList<ThreadItem> threads = null;
                        publishProgress(threads);
                        return;
                    }

                    ArrayList<ThreadItem> threads = new ArrayList<>();
                    int nTotalThreads = objects.size();
                    final int[] nCurrIdx = {0};

                    for (ParseObject thread : objects)
                    {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Messages");
                        query.setLimit(1);
                        query.whereEqualTo("thread", thread);
                        query.orderByDescending("createdAt");
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> messages, ParseException e) {
                                if (messages != null && messages.size() > 0) {
                                    ParseObject message = messages.get(0);
                                    ThreadItem threadItem = new ThreadItem();
                                    threadItem.setThread(thread);
                                    threadItem.setMessage(message);
                                    threads.add(threadItem);
                                }

                                nCurrIdx[0]++;

                                if (nCurrIdx[0] == nTotalThreads)
                                    publishProgress(threads);
                            }
                        });
                    }
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hideDialog();
        }

        @Override
        protected void onProgressUpdate(ArrayList<ThreadItem>... values) {
            ArrayList<ThreadItem> objects = values[0];
            if (objects == null)
            {
                msgRecycler.onRefreshComplete();
                hideDialog();
                noMessage();
            } else
            {
                msgRecycler.onRefreshComplete();
                hideDialog();
                AddAllThreads(objects);
            }
        }
    }

    private final static Comparator<ThreadItem> memberArryComparator = new Comparator<ThreadItem>()
    {

        @Override
        public int compare(ThreadItem lhs, ThreadItem rhs) {
            if (lhs.getCreatedAt() == null || rhs.getCreatedAt() == null)
                return 0;
            return lhs.getCreatedAt().compareTo(rhs.getCreatedAt());
        }
    };

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            myTask = new loadDataTask();
            myTask.execute();
        }
    };

}
