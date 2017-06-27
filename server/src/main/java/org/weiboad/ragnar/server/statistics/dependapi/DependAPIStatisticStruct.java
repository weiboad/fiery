package org.weiboad.ragnar.server.statistics.dependapi;

import java.util.HashMap;
import java.util.Map;

public class DependAPIStatisticStruct {
    //耗时最短的时间
    public Double fastTime;

    public Double getFastTime() {
        return fastTime;
    }

    public void setFastTime(Double fastTime) {
        this.fastTime = fastTime;
    }

    //耗时最长的时间
    public Double slowTime;

    public Double getSlowTime() {
        return slowTime;
    }

    public void setSlowTime(Double slowTime) {
        this.slowTime = slowTime;
    }

    //统计分段耗时时长
    public Double[] sumTime = {0.0, 0.0, 0.0, 0.0};

    public Double[] getSumTime() {
        return sumTime;
    }

    public void setSumTime(Double[] sumTime) {
        this.sumTime = sumTime;
    }

    //统计分段耗时时长的个数
    public int[] sumCount = {0, 0, 0, 0};

    public int[] getSumCount() {
        return sumCount;
    }

    public void setSumCount(int[] sumCount) {
        this.sumCount = sumCount;
    }

    //统计url返回码的次数
    public Map<String, Integer> codeMap = new HashMap<String, Integer>();

    public Map<String, Integer> getCodeMap() {
        return codeMap;
    }

    public void setCodeMap(Map<String, Integer> codeMap) {
        this.codeMap = codeMap;
    }
}
