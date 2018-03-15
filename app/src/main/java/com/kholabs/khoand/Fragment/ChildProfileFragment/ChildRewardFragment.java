package com.kholabs.khoand.Fragment.ChildProfileFragment;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kholabs.khoand.Adapter.RewardAdapter;
import com.kholabs.khoand.App.MyApp;
import com.kholabs.khoand.Model.Rewards;
import com.kholabs.khoand.R;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChildRewardFragment extends Fragment {
    private static final String ARG_FROM = "from";

    private ArrayList<Rewards> rewardArray = new ArrayList<>();
    private ListView listReward;
    private RewardAdapter adapter;
    private ParseUser inc_user;
    private Boolean isLoading = false;
    private int fromParent = 0;
    private View rootView;

    public ChildRewardFragment() {
        // Required empty public constructor

    }

    public static ChildRewardFragment newInstance(int from) {
        ChildRewardFragment fragment = new ChildRewardFragment();
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
            rootView = inflater.inflate(R.layout.fragment_child_reward, container, false);

            adapter = new RewardAdapter(getActivity());
            adapter.addItems(rewardArray);
            listReward = (ListView) rootView.findViewById(R.id.listRewards);
            loadRewards();
        }

        return rootView;
    }

    private void loadRewards()
    {
        if (inc_user == null) return;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Rewards");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException error) {
                if (error != null || objects == null || objects.size() == 0)
                    return;

                for (int i=0; i<objects.size(); i++)
                {
                    Rewards item = GetDataParseObject(objects.get(i));
                    rewardArray.add(item);
                }

                adapter = new RewardAdapter(getActivity());
                adapter.setListItems(rewardArray);
                listReward.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                for (int i=0; i<rewardArray.size(); i++)
                    ParseandNotify(rewardArray.get(i), i);
                //new UpdateListDataTask().execute(rewardArray);
            }
        });

    }

    private void updateView(Rewards item, int index){
        View v = listReward.getChildAt(index -
                listReward.getFirstVisiblePosition());

        if(v == null)
            return;

        RewardAdapter.ViewHolder mHolder = (RewardAdapter.ViewHolder)v.getTag();
        mHolder.refreshView(item);
    }

    private Rewards GetDataParseObject(ParseObject data)
    {
        Rewards item = new Rewards();
        item.setTitle(data.getString("title"));
        item.setDescription(data.getString("description"));
        item.setIdentifier(data.getString("identifier"));
        boolean isWeekly = data.getBoolean("updatedWeekly");
        item.weekly = isWeekly;
        item.setStatsLabel("-");
        item.setProgress(0);
        item.setRewardOne(0);
        item.setRewardTwo(0);
        item.setRewardThree(0);
        item.setOrigData(data);
        return item;
    }

    public void ParseandNotify(final Rewards _item, final int position) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable thread = new Runnable() {
            @Override
            public void run() {
                setMedalValues(_item.getOrigData(), _item, inc_user, position);
            }
        };

        executor.submit(thread);

    }

    private void setMedalValues(ParseObject reward, final Rewards item, ParseUser user, final int position)
    {
        String identifier = item.getIdentifier();
        if (identifier.equals("id_response"))
        {
            Date currentTime = Calendar.getInstance().getTime();
            Date lastTime = new Date(currentTime.getTime() - 604800000L);

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Comments");
            query.whereEqualTo("user", user);
            query.whereGreaterThan("createdAt", lastTime);
            query.countInBackground(new CountCallback() {
                @Override
                public void done(int count, ParseException e) {
                    if (count <= 3)
                    {
                        item.setStatsLabel(String.format("%d/10 Responses\nLevel: Bronze", count));
                        item.setRewardOne(1f);
                        item.setRewardTwo(0.3f);
                        item.setRewardThree(0.3f);
                        item.setProgress(0);


                    } else if (count >3 && count < 7)
                    {
                        item.setStatsLabel(String.format("%d/10 Responses\nLevel: Silver", count));
                        item.setRewardOne(1f);
                        item.setRewardTwo(1f);
                        item.setRewardThree(0.3f);
                        int progVal = count * 10;
                        item.setProgress(progVal);
                    } else if (count >= 7)
                    {
                        item.setStatsLabel(String.format("%d/10 Responses\nLevel: Golden", count));
                        item.setRewardOne(1f);
                        item.setRewardTwo(1f);
                        item.setRewardThree(1f);
                        int progVal = count * 10;
                        if (progVal > 100)
                            progVal = 100;
                        item.setProgress(progVal);
                    }
                    if (adapter != null)
                    {
                        adapter.setItem(position, item);
                        updateView(item, position);
                    }

                }
            });
        } else if (identifier.equals("id_supporter"))
        {
            Date currentTime = Calendar.getInstance().getTime();
            Date lastTime = new Date(currentTime.getTime() - 604800000L);

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Supports");
            query.whereEqualTo("user", user);
            query.whereGreaterThan("createdAt", lastTime);
            query.countInBackground(new CountCallback() {
                @Override
                public void done(int count, ParseException e) {
                    if (count <= 3)
                    {
                        item.setStatsLabel(String.format("%d/10 Responses\nLevel: Bronze", count));
                        item.setRewardOne(1f);
                        item.setRewardTwo(0.3f);
                        item.setRewardThree(0.3f);
                        item.setProgress(0);


                    } else if (count >3 && count < 7)
                    {
                        item.setStatsLabel(String.format("%d/10 Responses\nLevel: Silver", count));
                        item.setRewardOne(1f);
                        item.setRewardTwo(1f);
                        item.setRewardThree(0.3f);
                        int progVal = count * 10;
                        item.setProgress(progVal);
                    } else if (count >= 7)
                    {
                        item.setStatsLabel(String.format("%d/10 Responses\nLevel: Golden", count));
                        item.setRewardOne(1f);
                        item.setRewardTwo(1f);
                        item.setRewardThree(1f);
                        int progVal = count * 10;
                        if (progVal > 100)
                            progVal = 100;
                        item.setProgress(progVal);
                    }
                    if (adapter != null)
                    {
                        adapter.setItem(position, item);
                        updateView(item, position);
                    }

                }
            });
        } else if (identifier.equals("id_info"))
        {
            user.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    String name = object.getString("name");
                    Date birthday = object.getDate("dob");
                    ParseFile avatar = object.getParseFile("avatar");
                    List<String> sports = object.getList("sports");
                    String phone = object.getString("phone");

                    if (birthday != null && avatar !=null && name != null && sports != null && sports.size() > 0 && phone != null)
                    {
                        item.setStatsLabel("10/10 Responses\nLevel: Golden");
                        item.setRewardOne(1f);
                        item.setRewardTwo(1f);
                        item.setRewardThree(1f);
                        item.setProgress(100);
                    } else
                    {
                        if (birthday!= null && avatar!=null && name!=null  && phone!=null)
                        {
                            item.setStatsLabel("7/10 Responses\nLevel: Silver");
                            item.setRewardOne(1f);
                            item.setRewardTwo(1f);
                            item.setRewardThree(0.3f);
                            item.setProgress(70);
                        }
                        else
                        {
                            if (birthday!= null && avatar!=null && name!=null)
                            {
                                item.setStatsLabel("4/10 Responses\nLevel: Bronze");
                                item.setRewardOne(1f);
                                item.setRewardTwo(0.3f);
                                item.setRewardThree(0.3f);
                                item.setProgress(40);
                            } else
                            {
                                item.setStatsLabel("0/10 Responses\nLevel: Bronze");
                                item.setRewardOne(1f);
                                item.setRewardTwo(0.3f);
                                item.setRewardThree(0.3f);
                                item.setProgress(0);
                            }
                        }
                    }

                    if (adapter != null)
                    {
                        adapter.setItem(position, item);
                        updateView(item, position);
                    }
                }
            });
        }
    }



}
