package com.kholabs.khoand.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.kholabs.khoand.Model.SearchUser;
import com.kholabs.khoand.R;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aladar-PC2 on 1/29/2018.
 */

public class SearchUserAdapter extends BaseAdapter {
    private List<SearchUser> userList;
    private LayoutInflater inflater;
    private Context mContext = null;

    public SearchUserAdapter(Context _context) {
        userList = new ArrayList<SearchUser>();
        this.mContext = _context;
        this.inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setListItems(List<SearchUser> list) {
        this.userList.clear();
        this.userList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItem(SearchUser contactItem)
    {
        userList.add(contactItem);
    }

    public void setItem(int position, SearchUser _item)
    {
        userList.set(position, _item);
    }

    public void addItems(List<SearchUser> contactItems)
    {
        for(int i=0;i<contactItems.size();i++)
        {
            userList.add(contactItems.get(i));
        }
    }

    public void clearAll() {
        if (userList != null)
        {
            try
            {
                userList.clear();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            userList = new ArrayList<SearchUser>();
        }
        notifyDataSetChanged();
    }


    public void removeItem(int position) { userList.remove(position); }

    @Override
    public int getCount() {
        return userList == null ? 0 : userList.size();
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder mHolder ;

        final SearchUser data = userList.get(position);
        if (data == null)
            return null;

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.adapter_user, null);
            mHolder = new ViewHolder();

            mHolder.civPhoto = (CircularImageView) convertView.findViewById(R.id.civPhoto);

            mHolder.tvUsername = (TextView) convertView.findViewById(R.id.tvUserName);
            mHolder.tvUsertype = (TextView) convertView.findViewById(R.id.tvUserType);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder)convertView.getTag();
        }

        mHolder.refreshView(data);

        return convertView;

    }

    public class ViewHolder
    {
        private CircularImageView civPhoto;
        private TextView tvUsername, tvUsertype;

        public void refreshView(SearchUser item)
        {
            tvUsername.setText(item.getName());
            tvUsertype.setText(item.getType());

            if (item.getAvatar() != null) {
                civPhoto.setImageBitmap(item.getAvatar());
            }
            else
                civPhoto.setImageResource(R.drawable.blankprofile);

        }
    }
}
