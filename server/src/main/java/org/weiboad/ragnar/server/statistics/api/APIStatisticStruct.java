package org.weiboad.ragnar.server.statistics.api;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weiboad.ragnar.server.struct.MetaLog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class APIStatisticStruct {

    private String url = "";

    private Long totalCount = 0L;

    private float longestTime = 0F;

    private float shortestTime = 99999F;

    private Long ms200Count = 0L;
    private Double ms200Cost = 0D;

    private Long ms500Count = 0L;
    private Double ms500Cost = 0D;

    private Long ms1000Count = 0L;
    private Double ms1000Cost = 0D;

    private Long msLongCount = 0L;
    private Double msLongCost = 0D;

    //code && count
    private ConcurrentHashMap<String, AtomicLong> code_count = new ConcurrentHashMap<String, AtomicLong>();

    private transient Logger log = LoggerFactory.getLogger(APIStatisticStruct.class);

    public APIStatisticStruct(String url) {
        this.url = url;
    }
    public APIStatisticStruct(MetaLog metaLog) {
        this.url = metaLog.getUrl();
        analyzeMetaLog(metaLog);
    }

    public String getUrl() {
        return url;
    }

    public Double getMs200Cost() {
        return ms200Cost;
    }

    public Double getMs500Cost() {
        return ms500Cost;
    }

    public Double getMs1000Cost() {
        return ms1000Cost;
    }

    public Double getMsLongCost() {
        return msLongCost;
    }

    public float getLongestTime() {
        return longestTime;
    }

    public float getShortestTime() {
        return shortestTime;
    }


    public Long getTotalCount() {
        return totalCount;
    }

    public Long getMs200Count() {
        return ms200Count;
    }

    public Long getMs500Count() {
        return ms500Count;
    }

    public Long getMs1000Count() {
        return ms1000Count;
    }

    public Long getMsLongCount() {
        return msLongCount;
    }

    public ConcurrentHashMap<String, AtomicLong> getCode_count() {
        return code_count;
    }


    //increment the statics by metalog obj
    public void analyzeMetaLog(MetaLog metaLog) {

        //log.info(metaLog.getUrl());
        //total count
        totalCount++;

        Float elapsed = metaLog.getElapsed_ms();

        if (elapsed > longestTime) {
            longestTime = elapsed;
        }

        if (elapsed < shortestTime) {
            shortestTime = elapsed;
        }

        //cost time calc
        if (elapsed <= 0.2f) {
            ms200Count++;
            ms200Cost += elapsed;
        } else if (elapsed <= 0.5f) {
            ms500Count++;
            ms500Cost += elapsed;
        } else if (elapsed <= 1.0f) {
            ms1000Count++;
            ms1000Cost += elapsed;
        } else if (elapsed > 1.0f) {
            msLongCount++;
            msLongCost += elapsed;
        }

        String httpCode = metaLog.getHttpcode();
        if (httpCode.isEmpty()) {
            httpCode = "none";
        }

        if (code_count.containsKey(httpCode)) {
            code_count.get(httpCode).incrementAndGet();
        } else {
            code_count.put(httpCode, new AtomicLong(1));
        }

    }

    public String toJson() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(this);
        return jsonString;
    }
}
