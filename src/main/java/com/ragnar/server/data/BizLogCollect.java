package com.ragnar.server.data;

import java.util.ArrayList;

public class BizLogCollect {
    public String key;
    public String rpcid;
    public String timestamp;


    public ArrayList<BizLog> val;

    public ArrayList<BizLog> getVal() {
        return val;
    }

    public void setVal(ArrayList<BizLog> val) {
        this.val = val;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRpcid() {
        return rpcid;
    }

    public void setRpcid(String rpcid) {
        this.rpcid = rpcid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
