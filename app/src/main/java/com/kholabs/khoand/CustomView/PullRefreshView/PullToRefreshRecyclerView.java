package com.kholabs.khoand.CustomView.PullRefreshView;
 
import android.content.Context; 
import android.support.v7.widget.RecyclerView; 
import android.util.AttributeSet;

import com.kholabs.khoand.R;

public class PullToRefreshRecyclerView extends PullToRefreshBase<RecyclerView> { 
 
    public PullToRefreshRecyclerView(Context context) { 
        super(context); 
    } 
 
    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) { 
        super(context, attrs); 
    } 
 
    public PullToRefreshRecyclerView(Context context, Mode mode) { 
        super(context, mode); 
    } 
 
    public PullToRefreshRecyclerView(Context context, Mode mode, AnimationStyle style) { 
        super(context, mode, style); 
    } 
 
    @Override 
    public final Orientation getPullToRefreshScrollDirection() { 
        return Orientation.VERTICAL; 
    } 
 
    @Override 
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) { 
        RecyclerView recyclerView; 
        recyclerView = new RecyclerView(context, attrs); 
        recyclerView.setId(R.id.recyclerview);
        return recyclerView; 
    } 
 
    @Override 
    protected boolean isReadyForPullStart() { 
        if (mRefreshableView.getChildCount() <= 0) 
            return true; 
        int firstVisiblePosition = mRefreshableView.getChildPosition(mRefreshableView.getChildAt(0)); 
        if (firstVisiblePosition == 0) 
            return mRefreshableView.getChildAt(0).getTop() == 0; 
        else 
            return false; 
 
    } 
 
    @Override 
    protected boolean isReadyForPullEnd() {
        if (mRefreshableView.getChildCount() <= 0)
            return false;

        int lastVisiblePosition = mRefreshableView.getChildPosition(mRefreshableView.getChildAt(mRefreshableView.getChildCount() -1)); 
        if (lastVisiblePosition >= mRefreshableView.getAdapter().getItemCount()-1) { 
            return mRefreshableView.getChildAt(mRefreshableView.getChildCount() - 1).getBottom() <= mRefreshableView.getBottom(); 
        } 
        return false; 
    } 
 
}