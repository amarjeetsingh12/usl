package com.flipkart.gap.usl.core.config;

/**
 * Created by amarjeet.singh on 12/05/16.
 */
public class HzConfig {

    private String groupName;

    private boolean local = true;

    private String zkConnection;

    public HzConfig() {
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getZkConnection() {
        return zkConnection;
    }

    public void setZkConnection(String zkConnection) {
        this.zkConnection = zkConnection;
    }
}
