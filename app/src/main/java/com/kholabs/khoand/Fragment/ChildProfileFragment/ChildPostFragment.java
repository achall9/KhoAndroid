package com.kholabs.khoand.Fragment.ChildProfileFragment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.kholabs.khoand.Adapter.FeedListAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.FragmentPopKeys;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.Fragment.FeedFragment;
import com.kholabs.khoand.Fragment.FeedPostFragment;
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

public class ChildPostFragment extends Fragment implements FeedListAdapter.FeedListItemListener, ConstValues{
    private static final String ARG_FROM = "from";

    ArrayList<Feed> feedArray = new ArrayList<>();
    FeedListAdapter adapter;

    private View rootView;
    private RecyclerView listFeed;
    private ParseUser inc_user;
    private boolean isLoading = false;
    private int fromParent;
    public ChildPostFragment() {
        // Required empty public constructor
    }

    public static ChildPostFragment newInstance(int from) {
        ChildPostFragment fragment = new ChildPostFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FROM, from);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.inc_user = MyApp.getInstance().getShareUser();
        if (getArguments() != null) {
            this.fromParent = getArguments().getInt(ARG_FROM);
        }
        rootView = null;
        isLoading = true;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_child_post, container, false);
            listFeed = (RecyclerView) rootView.findViewById(R.id.list_feed);
            adapter = new FeedListAdapter(getActivity(), this);
            fromParent = ProfileFrom.fromFeedPage;

            listFeed.setAdapter(adapter);
            reloadData();
        }

        return rootView;
    }

    private class loadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //adapter.setHasStableIds(true);

        }
    }

    private void reloadData()
    {

        if (isLoading == false)
            return;
        isLoading = false;

        if (feedArray != null)
            feedArray.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
        query.whereEqualTo("owner", inc_user);
        query.include("owner");
        query.orderByDescending("createdAt");
        query.setLimit(10);

        //if (!MyApp.getInstance().isNetworkAvailable())
            //query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() == 0)
                    return;

                /*if (MyApp.getInstance().isNetworkAvailable())
                {
                    try {
                        ParseObject.pinAll(objects);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                }*/

                for (int i=0; i<objects.size(); i++)
                {
                    Feed item = GetDataFromParseObject(objects.get(i), i);
                    feedArray.add(item);
                }

                adapter.setListItems(feedArray);
            }
        });
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
        newItem.setpUser(pUser);
        newItem.setpAnswer(object.getParseObject("acceptedComment"));
        newItem.setOrigData(object);
        newItem.setId(position+500);
        return newItem;

    }


    @Override
    public void goToPostFragment(Feed item) {
        MyApp.getInstance().setPostFeed(item);
        if (fromParent == ProfileFrom.fromFeedPage)
        {
            MyApp.getInstance().setPostFeed(item);
            MyApp.getInstance().setFromParent(ProfileFrom.fromProfile);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.feed_root_frame, new FeedPostFragment(), "Post")
                    .addToBackStack(FragmentPopKeys.PROFILEPAGE)
                    .commit();
            getActivity().invalidateOptionsMenu();
        } else if (fromParent == ProfileFrom.fromProfile)
        {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.profile_root_frame, new FeedPostFragment(), "Post")
                    .addToBackStack(FragmentPopKeys.PROFILEPAGE)
                    .commit();
            getActivity().invalidateOptionsMenu();
        }


    }
}
