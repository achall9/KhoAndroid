package com.kholabs.khoand.Interface;

import com.kholabs.khoand.Model.Feed;

/**
 * Created by Aladar-PC2 on 2/20/2018.
 */

public interface FeedNotifyListener {
    void OnAddItem(Feed item, int position);
    void OnUpdatedItem(Feed item, int position);
}
