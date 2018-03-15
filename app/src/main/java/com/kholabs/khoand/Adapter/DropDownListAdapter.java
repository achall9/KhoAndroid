package com.kholabs.khoand.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.kholabs.khoand.R;

public class DropDownListAdapter extends RecyclerView.Adapter<DropDownListAdapter.ViewHolder> {

	private ArrayList<String> mListItems;
	private TextView mSelectedItems;
	private ArrayList<Boolean> checkSelected;

	public interface DropDownItemListener {
		public void AddCustomField(int position);
	}

	private DropDownItemListener caller;
	
	public void addItem(String field)
	{
		mListItems.add(mListItems.size()-1, field);
		if (checkSelected != null)
			checkSelected.add(true);
		notifyItemInserted(mListItems.size()-1);
		replaceText();
	}

	public DropDownListAdapter(Context context, ArrayList<String> items, TextView tv, DropDownItemListener _caller) {
		this.mListItems = new ArrayList<String>();
		this.mListItems.addAll(items);
		this.mSelectedItems = tv;
		this.caller = _caller;

		this.checkSelected= new ArrayList<>();

		for (int i=0; i<mListItems.size()-1; i++)
			checkSelected.add(i, false);
	}

	public void setCheckArray(String joinStr)
	{
		for (int i=0; i<mListItems.size()-1; i++) {
			if (joinStr.contains(mListItems.get(i)))
				checkSelected.set(i, true);
		}

		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return mListItems.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drop_down_list_row, parent, false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.tvSportType.setText(mListItems.get(position));

		if (position == mListItems.size() -1)
			holder.ivCheckBox.setVisibility(View.INVISIBLE);
		else
		{
			if (checkSelected.get(position) == true)
				holder.ivCheckBox.setImageResource(R.drawable.misc_tick_filled);
			else
				holder.ivCheckBox.setImageResource(R.drawable.misc_tick_unfilled);
		}

		holder.rlItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mListItems.get(position).equals("Add Custom Field"))
				{
					caller.AddCustomField(position);
				}
				else {
					boolean isChecked = checkSelected.get(position);
					checkSelected.set(position, !isChecked);
					notifyItemChanged(position);
					replaceText();
				}
			}
		});
	}


	/*
	 * Function which updates the selected values display and information(checkSelected[])
	 * */
	private void replaceText(){
		mSelectedItems.setText(R.string.select_sports);

		String strSelected = "";
		for (int i=0; i<checkSelected.size(); i++)
		{
			if (checkSelected.get(i))
				strSelected = strSelected + mListItems.get(i) + ",";
		}

		if (strSelected.length() > 0)
			strSelected = strSelected.substring(0, strSelected.length() - 1);

		mSelectedItems.setText(strSelected);
	}

	public class ViewHolder extends RecyclerView.ViewHolder{
		public TextView tvSportType;
		public ImageView ivCheckBox;
		public RelativeLayout rlItem;

		public ViewHolder(View itemView) {
			super(itemView);
			tvSportType = (TextView) itemView.findViewById(R.id.tvItemType);
			ivCheckBox= (ImageView) itemView.findViewById(R.id.ivCheckBox);
			rlItem = (RelativeLayout) itemView.findViewById(R.id.rl_item);
		}

	}
}
