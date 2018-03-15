package com.kholabs.khoand.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.kholabs.khoand.Activity.MainActivity;
import com.kholabs.khoand.Adapter.FeedListAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.FragmentBase;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshBase;
import com.kholabs.khoand.CustomView.PullRefreshView.PullToRefreshRecyclerView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestionFragment extends FragmentBase implements FeedListAdapter.FeedListItemListener,
        MainActivity.onKeyBackPressedListener
{

    private ParseUser inc_user;
    private ParseUser curr_user;
    private int fromParent;

    private PullToRefreshRecyclerView mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private View rootView;
    private LinearLayout llBack;

    public static ArrayList<Feed> selectData = new ArrayList<Feed>();
    private FeedListAdapter adapter;

    private int loadCount = 0;
    private int itemsPerPage = 10;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        }
    };

    public QuestionFragment() {
    }

    public static QuestionFragment newInstance(String param1, String param2) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        this.rootView = null;
        this.fromParent = MyApp.getInstance().getFromParent();
        this.inc_user = MyApp.getInstance().getShareUser();
        this.curr_user = ParseUser.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_question, container, false);
            ((MainActivity)getActivity()).setOnkeyBackPressedListener(this);

            llBack = (LinearLayout)rootView.findViewById(R.id.llBack);
            mRecyclerView = (PullToRefreshRecyclerView) rootView.findViewById(R.id.list_feed);
            linearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.getRefreshableView().setLayoutManager(linearLayoutManager);
            adapter = new FeedListAdapter(getActivity(), this);

            mRecyclerView.getRefreshableView().setHasFixedSize(true);
            mRecyclerView.getRefreshableView().setAdapter(adapter);
            mRecyclerView.setScrollingWhileRefreshingEnabled(true);
            mRecyclerView.setMode(PullToRefreshBase.Mode.BOTH);
            selectData.clear();

            initEvent();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    reloadData(true);
                }
            }, 500);
        }

        return  rootView;
    }

    private void reloadData(boolean isDialogShow)
    {
        if (inc_user == null)
            return;

        if (loadCount > 0)
        {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
            query.whereEqualTo("owner", inc_user);
            query.setLimit(itemsPerPage);
            query.orderByDescending("createdAt");
            query.include("owner");
            query.setSkip(itemsPerPage * loadCount);

            if (isDialogShow)
                showDialog();

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> Posts, ParseException e) {
                    if (e != null || Posts == null || Posts.size() == 0) {
                        mRecyclerView.onRefreshComplete();
                        if (isDialogShow)
                            hideDialog();
                        return;
                    }

                    mRecyclerView.onRefreshComplete();
                    ArrayList<Feed> addItems = new ArrayList<>();

                    for (int i=0; i<Posts.size(); i++)
                    {
                        Feed item = GetDataFromParseObject(Posts.get(i), i);
                        addItems.add(item);
                    }

                    int startPos = selectData.size();
                    selectData.addAll(addItems);
                    adapter.setUpdatePosition(startPos);
                    adapter.setListItems(selectData);

                    if (isDialogShow)
                        hideDialog();
                    scrollToLastPosition(300);

                }
            });

            return;
        } else
        {
            if (getActivity() == null)
                return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (selectData != null) {
                        selectData.clear();
                    }
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
                    query.whereEqualTo("owner", inc_user);
                    query.setLimit(itemsPerPage);
                    query.orderByDescending("createdAt");
                    query.include("owner");

                    if (isDialogShow)
                        showDialog();

                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> Posts, ParseException e) {
                            if (e != null || Posts == null || Posts.size() == 0) {
                                mRecyclerView.onRefreshComplete();
                                if (isDialogShow)
                                    hideDialog();
                                return;
                            }

                            if (isDialogShow)
                                hideDialog();

                            mRecyclerView.onRefreshComplete();

                            for (int i=0; i<Posts.size(); i++)
                            {
                                Feed item = GetDataFromParseObject(Posts.get(i), i);
                                selectData.add(item);
                            }

                            adapter.setUpdatePosition(0);
                            adapter.setListItems(selectData);
                            scrollToLastPosition(300);


                        }
                    });
                }
            });
        }

    }


    private void initEvent() {
        mRecyclerView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<RecyclerView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadCount = 0;
                reloadData(false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                loadCount++;
                reloadData(false);
            }
        });


        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
            }
        });
    }

    @Override
    public void goToPostFragment(Feed item) {
        MyApp.getInstance().setPostFeed(item);
        MyApp.getInstance().setFromParent(ConstValues.PostPageFrom.fromSupport);

        if (fromParent == ConstValues.ProfileFrom.fromProfile)
        {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.profile_root_frame, new FeedPostFragment(), "Post")
                    .addToBackStack(null)
                    .commit();
        }
        else if (fromParent == ConstValues.ProfileFrom.fromFeedPage)
        {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.feed_root_frame, new FeedPostFragment(), "Post")
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onBack() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
        ((MainActivity)getActivity()).setOnkeyBackPressedListener(null);
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

    private void scrollToTopPosition(int delayTimeMills)
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null)
                    mRecyclerView.getRefreshableView().smoothScrollToPosition(0);

            }
        }, delayTimeMills);
    }
    private void scrollToLastPosition(int delayTimeMills)
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null && adapter.getItemCount() > 0)
                    mRecyclerView.getRefreshableView().smoothScrollToPosition(adapter.getItemCount()-1);

            }
        }, delayTimeMills);
    }

}