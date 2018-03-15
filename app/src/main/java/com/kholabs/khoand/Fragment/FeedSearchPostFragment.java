package com.kholabs.khoand.Fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.kholabs.khoand.Adapter.FeedListAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.Common.FragmentBase;
import com.kholabs.khoand.Common.FragmentPopKeys;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshBase;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshListView;
import com.kholabs.khoand.Model.Feed;
import com.kholabs.khoand.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.kholabs.khoand.Activity.MainActivity.keyword;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedSearchPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedSearchPostFragment extends FragmentBase implements FeedListAdapter.FeedListItemListener, ConstValues{
    private static final String ARG_KEYWORD = "keyword";
    private String strKeyWord = "";
    public static ArrayList<Feed> selectData = new ArrayList<Feed>();
    FeedListAdapter adapter;

    private View rootView;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private RecyclerView mRecyclerView;
    private TextView tvNoComment;
    private static ParentFragmentListener caller;
    private loadDataTask myTask;

    public interface ParentFragmentListener {
        public void GoToNextPostFragment();
    }


    public FeedSearchPostFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FeedSearchPostFragment newInstance(String keyword, ParentFragmentListener _caller) {
        FeedSearchPostFragment fragment = new FeedSearchPostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEYWORD, keyword);
        fragment.setArguments(args);
        caller = _caller;
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            strKeyWord = getArguments().getString(ARG_KEYWORD);
        }
        myTask = null;
        rootView = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_search_post, container, false);
            tvNoComment = (TextView) rootView.findViewById(R.id.txtNoComment);

            mRecyclerView = (RecyclerView)rootView.findViewById(R.id.list_feed);
            linearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);

            adapter = new FeedListAdapter(getActivity(), this);
            adapter.addItems(selectData);

            initEvent();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.setAdapter(adapter);
                    myTask = new loadDataTask();
                    myTask.execute();
                }
            }, 500);
            //new SetupThreadTask().execute();
        }
        return rootView;
    }

    public class loadDataTask extends AsyncTask<Void, List<ParseObject>, Void> {
        @Override
        protected void onPreExecute()
        {
            showDialog();
        }

        @Override
        protected Void doInBackground(Void... datas) {
            if (strKeyWord == null || strKeyWord.equals(""))
                return null;

            if (selectData != null)
                selectData.clear();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
            query.whereMatches("content", strKeyWord, "i");
            query.include("owner");

            query.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null || objects == null || objects.size() == 0) {
                        List<ParseObject> threads = null;
                        publishProgress(threads);
                        return;
                    }

                    publishProgress(objects);
                }
            });


            return  null;
        }


        @Override
        protected void onPostExecute(Void result) {
            hideDialog();
        }

        @Override
        protected void onProgressUpdate(List<ParseObject>... values) {

            List<ParseObject> objects = values[0];

            if (objects == null)
            {
                hideDialog();
            } else if (objects != null)
            {
                for (int i = 0; i < objects.size(); i++) {
                    Feed item = GetDataFromParseObject(objects.get(i), i);
                    selectData.add(item);
                }

                adapter.setListItems(selectData);
                scrollToLastPosition(300);
            }

        }
    }

    private void reloadData()
    {
        if (strKeyWord == null || strKeyWord.equals(""))
            return;

        tvNoComment.setVisibility(View.GONE);

        if (selectData != null)
            selectData.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
        query.whereMatches("content", strKeyWord, "i");
        query.include("owner");

        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) { noResults(); return; }
                if (objects == null) { noResults(); return; }
                if (objects.size() == 0) { noResults(); return; }


                for (int i = 0; i < objects.size(); i++) {
                    Feed item = GetDataFromParseObject(objects.get(i), i);
                    selectData.add(item);
                }

                adapter.setListItems(selectData);
                scrollToLastPosition(300);
            }
        });
    }

    public void setKeyWord(String newKey) {
        strKeyWord =    newKey;
        myTask = new loadDataTask();
        myTask.execute();
    }

    public void noResults()
    {
        tvNoComment.setVisibility(View.VISIBLE);
    }

    private void initEvent() {

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void scrollToLastPosition(int delayTimeMills)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getItemCount() > 0) {
                    mRecyclerView.smoothScrollToPosition(mRecyclerView.getChildCount());
                }
            }
        }, delayTimeMills);
    }

    private Feed GetDataFromParseObject(ParseObject object, int position)
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
        newItem.setId(position+500);
        return newItem;

    }

    @Override
    public void goToPostFragment(Feed item) {
        MyApp.getInstance().setPostFeed(item);
        MyApp.getInstance().setFromParent(PostPageFrom.fromSearchFeed);

        caller.GoToNextPostFragment();
    }


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
    public void onDetach() {
        super.onDetach();
        if (myTask != null)
            myTask.cancel(true);
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

        }
    }
}
