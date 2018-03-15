package com.kholabs.khoand.Adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kholabs.khoand.CustomView.ImageView.RoundedImageView;
import com.kholabs.khoand.Model.Rewards;
import com.kholabs.khoand.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aladar-PC2 on 2/2/2018.
 */

public class RewardAdapter extends BaseAdapter {
    private List<Rewards> rewardList;
    private LayoutInflater inflater;
    private Context mContext = null;

    public RewardAdapter(Context _context) {
        rewardList = new ArrayList<Rewards>();
        this.mContext = _context;
        this.inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setListItems(List<Rewards> list) {
        this.rewardList = list;
    }

    public void addItem(Rewards contactItem)
    {
        rewardList.add(contactItem);
    }

    public void setItem(int position, Rewards _item)
    {
        rewardList.set(position, _item);
    }

    public void addItems(List<Rewards> contactItems)
    {
        for(int i=0;i<contactItems.size();i++)
        {
            rewardList.add(contactItems.get(i));
        }
    }

    public void clearAll() {
        if (rewardList != null)
        {
            try
            {
                rewardList.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            rewardList = new ArrayList<Rewards>();
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return rewardList == null ? 0 : rewardList.size();
    }

    @Override
    public Object getItem(int position) {
        return rewardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder ;

        final Rewards data = rewardList.get(position);
        if (data == null)
            return null;

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.childview_reward_fragment, null);
            mHolder = new ViewHolder();

            mHolder.tvRewardsTitle = (TextView) convertView.findViewById(R.id.tvRewardTitle);
            mHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
            mHolder.tvUpdatedWeekly = (TextView) convertView.findViewById(R.id.tvUpdatedWeekly);
            mHolder.tvResponseVal = (TextView) convertView.findViewById(R.id.tvResponseVal);
            mHolder.ivRewardSymbol = (ImageView) convertView.findViewById(R.id.ivRewardSymbol);
            mHolder.ivGold = (ImageView) convertView.findViewById(R.id.ivGold);
            mHolder.ivSilver = (ImageView) convertView.findViewById(R.id.ivSilver);
            mHolder.ivBronze = (ImageView) convertView.findViewById(R.id.ivBronze);
            mHolder.progressStep = (ProgressBar) convertView.findViewById(R.id.progress_step);
            mHolder.rivBackground = (RoundedImageView) convertView.findViewById(R.id.rIvbackground);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder)convertView.getTag();
        }

        mHolder.refreshView(data);

        return convertView;
    }

    public class ViewHolder {
        private int position = 0;
        private TextView tvRewardsTitle, tvDescription, tvUpdatedWeekly;
        private TextView tvResponseVal;
        private ImageView ivRewardSymbol;
        private ImageView ivBronze, ivSilver, ivGold;
        private ProgressBar progressStep;
        private RoundedImageView rivBackground;

        public void refreshView(Rewards item)
        {
            tvRewardsTitle.setText(item.getTitle());
            tvDescription.setText(item.getDescription());
            tvResponseVal.setText(item.getStatsLabel());

            if (item.weekly == true)
                tvUpdatedWeekly.setAlpha(1.0f);
            else
                tvUpdatedWeekly.setAlpha(0.0f);

            if (item.getIdentifier().equals("id_response"))
            {
                rivBackground.setImageResource(R.drawable.pattern_blue_purple);
                ivRewardSymbol.setImageResource(R.drawable.misc_post);
            } else if (item.getIdentifier().equals("id_supporter"))
            {
                rivBackground.setImageResource(R.drawable.pattern_green);
                ivRewardSymbol.setImageResource(R.drawable.misc_like);
            } else if (item.getIdentifier().equals("id_info"))
            {
                rivBackground.setImageResource(R.drawable.pattern_orange_purple);
                ivRewardSymbol.setImageResource(R.drawable.misc_tick_filled);
            }

            ivGold.setAlpha(item.getRewardThree());
            ivSilver.setAlpha(item.getRewardTwo());
            ivBronze.setAlpha(item.getRewardOne());

            setProgressAnimation(item.getProgress());
        }

        public void setProgressAnimation(int position)
        {
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                ObjectAnimator animation = ObjectAnimator.ofInt(progressStep, "progress", position);
                animation.setDuration(300);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
            }
            else
                progressStep.setProgress(position);
        }
    }
}
