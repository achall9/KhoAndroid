package com.kholabs.khoand.Interface;

import com.kholabs.khoand.Model.ThreadItem;

/**
 * Created by Aladar-PC2 on 2/18/2018.
 */

public interface NotifyListener {
    void OnAddItem(ThreadItem item, int position);
    void OnUpdatedItem(ThreadItem item, int position);
}