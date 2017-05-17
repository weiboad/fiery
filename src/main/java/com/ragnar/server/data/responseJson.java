package com.ragnar.server.data;

import java.util.ArrayList;
import java.util.List;

public class responseJson {
    public int code = 0;
    public String msg = "OK";
    public int totalcount = 0;
    public List<MetaLog> result = new ArrayList<MetaLog>();


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<MetaLog> getResult() {
        return result;
    }

    public void setResult(List<MetaLog> result) {
        this.result = result;
    }

    public int getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(int totalcount) {
        this.totalcount = totalcount;
    }
}
