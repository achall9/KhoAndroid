package com.kholabs.khoand.Fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.kholabs.khoand.Adapter.SearchUserAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultBus;
import com.kholabs.khoand.Common.ActivityResults.ActivityResultEvent;
import com.kholabs.khoand.Common.FragmentPopKeys;
import com.kholabs.khoand.Context.ConstValues;
import com.kholabs.khoand.Model.SearchUser;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FeedSearchUserFragment extends Fragment implements ConstValues{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_KEYWORD = "keyword";
    private String strKeyWord = "";

    private View rootView;
    ArrayList<SearchUser> userArray = new ArrayList<>();
    SearchUserAdapter adapter;
    private ListView listUser;
    private TextView tvNoComment;
    private ExecutorService executor;

    private boolean isLoadingEnable = false;
    private static ParentFragmentListener caller;

    public interface ParentFragmentListener {
        public void GoToNextFragment();
    }

    public FeedSearchUserFragment()
    {

    }

    // TODO: Rename and change types and number of parameters
    public static FeedSearchUserFragment newInstance(String keyword, ParentFragmentListener _caller) {
        FeedSearchUserFragment fragment = new FeedSearchUserFragment();
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

        isLoadingEnable = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_search_user, container, false);
            adapter = new SearchUserAdapter(getActivity());
            adapter.addItems(userArray);

            listUser = (ListView) rootView.findViewById(R.id.listUsers);
            tvNoComment = (TextView) rootView.findViewById(R.id.txtNoComment);

            initEvent();
            reloadUser();
        }

        return rootView;
    }

    private void initEvent() {
        listUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SearchUser user = userArray.get(position);

                MyApp.getInstance().setShareUser(user.getpUser());
                MyApp.getInstance().setFromParent(ProfileFrom.fromFeedPage);

                caller.GoToNextFragment();
            }
        });
    }

    public void setKeyWord(String newKey) {
        strKeyWord =    newKey;
        isLoadingEnable = true;
        reloadUser();
    }

    public void noResults()
    {
        tvNoComment.setVisibility(View.VISIBLE);
    }

    public void reloadUser() {
        if (!isLoadingEnable)
            return;
        isLoadingEnable = false;

        if (strKeyWord == null || strKeyWord.equals(""))
            return;

        tvNoComment.setVisibility(View.GONE);

        if (userArray != null)
            userArray.clear();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        //query.whereMatches("name", String.format("?i)%s", strKeyWord));
        query.whereMatches("name", strKeyWord, "i");
        query.findInBackground(new FindCallback<ParseUser>() {

            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) { noResults(); return; }
                if (objects == null) { noResults(); return; }
                if (objects.size() == 0) { noResults(); return; }

                for (int i=0; i<objects.size(); i++)
                {
                    ParseUser member = (ParseUser)objects.get(i);
                    SearchUser Useritem = GetDataFromParseObject(member);
                    userArray.add(Useritem);
                }

                adapter = new SearchUserAdapter(getActivity());
                listUser.setAdapter(adapter);
                adapter.setListItems(userArray);

                //new UpdateListDataTask().execute(userArray);
                updateListData();
            }
        });
    }

    private SearchUser GetDataFromParseObject(ParseUser member)
    {
        SearchUser item = new SearchUser();
        item.setName(member.getString("name"));
        item.setType("Athlete");
        boolean isTherapist = member.getBoolean("isTherapist");
        if (isTherapist)
            item.setType("Therapist");
        item.setpUser(member);
        return item;
    }

    private void updateView(SearchUser item, int index){
        View v = listUser.getChildAt(index -
                listUser.getFirstVisiblePosition());

        if(v == null)
            return;

        SearchUserAdapter.ViewHolder mHolder = (SearchUserAdapter.ViewHolder)v.getTag();
        mHolder.refreshView(item);
    }


    public void GetAvatarPhotoFromData(ParseUser user, final SearchUser _item, final int position)
    {
        if (_item.getAvatar() != null)
            return;


        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null || object == null)
                    return;
                ParseFile pFile = object.getParseFile("avatar");

                if (pFile == null)
                    return;

                pFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        if (e != null || data == null)
                            return;

                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        _item.setAvatar(bmp);
                        bmp = null;

                        if (adapter != null) {
                            adapter.setItem(position, _item);
                            userArray.set(position, _item);
                            updateView(_item, position);
                        }

                    }
                });

            }
        });
    }

    public class AvatarWrapper
    {
        public SearchUser item;
        public int position;
    }

    private void updateListData()
    {
        if (userArray.size() == 0)
            return;

        for (int i=0; i<userArray.size(); i++)
        {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            AvatarRunnable thread = new AvatarRunnable(userArray.get(i), i);
            executor.submit(thread);
        }
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
    public void onDestroy()
    {
        //if (executor != null)
            //executor.shutdownNow();
        super.onDestroy();
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

    public class AvatarRunnable implements Runnable {
        private SearchUser item;
        private int position;

        public AvatarRunnable(SearchUser _item, int _pos)
        {
            this.item = _item;
            this.position = _pos;
        }

        @Override
        public void run() {

            GetAvatarPhotoFromData(item.getpUser(), item, position);
        }
    }
}
