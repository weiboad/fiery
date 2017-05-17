package com.ragnar.server.data.statics;

import com.ragnar.server.data.MetaLog;
import com.ragnar.server.util.DateTimeHepler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("singleton")
public class APITopURLStaticShardCollect {
    private ConcurrentHashMap<Long, APITopURLStaticURLCollect> apiTopStaticHelper = new ConcurrentHashMap<Long, APITopURLStaticURLCollect>();
    Logger log = LoggerFactory.getLogger(APITopURLStaticShardCollect.class);

    public void analyzeMetaLog(MetaLog metainfo) {
        Long shardTime = metainfo.getTime().longValue();
        if (shardTime > 0) {
            shardTime = DateTimeHepler.getTimesmorning(shardTime);
            if (!apiTopStaticHelper.containsKey(shardTime)) {

                APITopURLStaticURLCollect apiTopURLStaticURLCollect = new APITopURLStaticURLCollect(shardTime);
                //count ++
                apiTopURLStaticURLCollect.analyzeMetaLog(metainfo);
                apiTopStaticHelper.put(shardTime, apiTopURLStaticURLCollect);
            } else {
                //count ++
                apiTopStaticHelper.get(shardTime).analyzeMetaLog(metainfo);
            }
        }
    }

    public Map<String, Integer> getAPITOPStatics() {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Map.Entry<Long, APITopURLStaticURLCollect> ent : apiTopStaticHelper.entrySet()) {
            result.put(ent.getKey() + "", ent.getValue().getUrlSize());
        }
        return result;
    }

    public APITopURLStaticURLCollect getSharder(Long timestamp, boolean create) {
        Long shardTime = DateTimeHepler.getTimesmorning(timestamp);
        if (!apiTopStaticHelper.containsKey(shardTime)) {
            if (create) {
                APITopURLStaticURLCollect apiTopURLStaticURLCollect = new APITopURLStaticURLCollect(shardTime);
                apiTopStaticHelper.put(shardTime, apiTopURLStaticURLCollect);
                return apiTopStaticHelper.get(shardTime);
            }
            //default not create this one
            return null;
        } else {
            return apiTopStaticHelper.get(shardTime);
        }
    }
}
