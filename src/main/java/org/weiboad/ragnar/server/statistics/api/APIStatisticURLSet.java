package org.weiboad.ragnar.server.statistics.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weiboad.ragnar.server.struct.MetaLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class APIStatisticURLSet {

    private ConcurrentHashMap<String, APIStatisticStruct> urlList = new ConcurrentHashMap<String, APIStatisticStruct>();

    private Long shardTime;

    public APIStatisticURLSet(Long shardtime) {
        this.shardTime = shardtime;
    }

    Logger log = LoggerFactory.getLogger(APIStatisticURLSet.class);

    public void analyzeMetaLog(MetaLog metainfo) {
        String url = metainfo.getUrl().trim();
        if (urlList.containsKey(url)) {
            urlList.get(url).analyzeMetaLog(metainfo);
        } else {
            APIStatisticStruct apidata = new APIStatisticStruct(url);
            apidata.analyzeMetaLog(metainfo);
            urlList.put(url, apidata);
        }
    }

    public Integer getUrlSize() {
        return urlList.size();
    }

    public List<APIStatisticStruct> getCollectList() {
        try {
            return new ArrayList<APIStatisticStruct>(urlList.values());
        } catch (Exception e) {
            return new ArrayList<APIStatisticStruct>();
        }
    }

    public Long getShardTime() {
        return shardTime;
    }
}