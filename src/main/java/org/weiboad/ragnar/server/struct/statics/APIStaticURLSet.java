package org.weiboad.ragnar.server.struct.statics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weiboad.ragnar.server.struct.MetaLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class APIStaticURLSet {

    private ConcurrentHashMap<String, APIStaticStruct> urlList = new ConcurrentHashMap<String, APIStaticStruct>();

    private Long shardTime;

    public APIStaticURLSet(Long shardtime) {
        this.shardTime = shardtime;
    }

    Logger log = LoggerFactory.getLogger(APIStaticURLSet.class);

    public void analyzeMetaLog(MetaLog metainfo) {
        String url = metainfo.getUrl().trim();
        if (urlList.containsKey(url)) {
            urlList.get(url).analyzeMetaLog(metainfo);
        } else {
            APIStaticStruct apidata = new APIStaticStruct(url);
            apidata.analyzeMetaLog(metainfo);
            urlList.put(url, apidata);
        }
    }

    public Integer getUrlSize() {
        return urlList.size();
    }

    public List<APIStaticStruct> getCollectList() {
        try {
            return new ArrayList<APIStaticStruct>(urlList.values());
        } catch (Exception e) {
            return new ArrayList<APIStaticStruct>();
        }
    }

    public Long getShardTime() {
        return shardTime;
    }
}