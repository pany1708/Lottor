package com.blueskykong.tm.server.entity;

import com.blueskykong.tm.common.netty.bean.TxTransactionItem;

/**
 * @author keets
 * @data 2018/4/3.
 */
public class TxTransactionItemAdapter extends TxTransactionItem {
    String groupId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
