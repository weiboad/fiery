package com.ragnar.server.data.statics;

import com.ragnar.server.data.MetaLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class APITopURLStaticURLCollect {

    private ConcurrentHashMap<String, APITopURLStaticData> urlList = new ConcurrentHashMap<String, APITopURLStaticData>();

    private Long shardTime;

    public APITopURLStaticURLCollect(Long shardtime) {
        this.shardTime = shardtime;
    }

    Logger log = LoggerFactory.getLogger(APITopURLStaticURLCollect.class);

    public void analyzeMetaLog(MetaLog metainfo) {
        String url = metainfo.getUrl().trim();
        if (urlList.containsKey(url)) {
            urlList.get(url).analyzeMetaLog(metainfo);
        } else {
            APITopURLStaticData apidata = new APITopURLStaticData(url);
            apidata.analyzeMetaLog(metainfo);
            urlList.put(url, apidata);
        }
    }

    public Integer getUrlSize() {
        return urlList.size();
    }

    public List<APITopURLStaticData> getCollectList() {
        try {
            return new ArrayList<APITopURLStaticData>(urlList.values());
        } catch (Exception e) {
            return new ArrayList<APITopURLStaticData>();
        }
    }

    public Long getShardTime() {
        return shardTime;
    }
}