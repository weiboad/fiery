package org.weiboad.ragnar.server.data.statics;

public class ErrorInfo {
    //first and second log str
    public String content;
    //记录最新的一个相似日志的信息
    public String newLogStr;
    //记录第一个相似的日志信息
    public String oldLogStr;
    //相似日志信息出现的次数
    public int count;
    //第一次出现相似日志信息的时间
    public String startTime;
    //最后出现相似日志信息的时间
    public String endTime;

    public String oldtraceid;

    public String oldrpcid;

    public String newtraceid;

    public String newrpcid;
}
