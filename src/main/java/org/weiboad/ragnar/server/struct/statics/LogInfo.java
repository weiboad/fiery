package org.weiboad.ragnar.server.struct.statics;

import org.weiboad.ragnar.server.util.SimHash;

public class LogInfo {
    public SimHash hash;
    //日志文件路径
    private String filePath;
    //所在文件的位置行数
    private Integer fileLine;
    //记录最新的一个相似日志的信息
    private String newLogStr;
    //记录第一个相似的日志信息
    private String oldLogStr;
    //相似日志信息出现的次数
    private Integer count;
    //第一次出现相似日志信息的时间
    private Long startTime;
    //最后出现相似日志信息的时间
    private Long endTime;

    public String getFilePath() {
        return filePath;
    }

    public Integer getFileLine() {
        return fileLine;
    }

    public String getNewLog() {
        return newLogStr;
    }

    public String getOldLog() {
        return oldLogStr;
    }

    public Integer getCount() {
        return count;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileLine(Integer line) {
        this.fileLine = line;
    }

    public void setNewLog(String newlog) {
        this.newLogStr = newlog;
    }

    public void setOldLog(String oldLog) {
        this.oldLogStr = oldLog;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
