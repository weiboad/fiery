package org.weiboad.ragnar.server.struct.statics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.struct.MetaLog;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("singleton")
public class APIStaticTimeSet {
    private ConcurrentHashMap<Long, APIStaticURLSet> apiTopStaticHelper = new ConcurrentHashMap<Long, APIStaticURLSet>();
    Logger log = LoggerFactory.getLogger(APIStaticTimeSet.class);

    public void analyzeMetaLog(MetaLog metainfo) {
        Long shardTime = metainfo.getTime().longValue();
        if (shardTime > 0) {
            shardTime = DateTimeHelper.getTimesMorning(shardTime);
            if (!apiTopStaticHelper.containsKey(shardTime)) {

                APIStaticURLSet apiStaticURLSet = new APIStaticURLSet(shardTime);
                //count ++
                apiStaticURLSet.analyzeMetaLog(metainfo);
                apiTopStaticHelper.put(shardTime, apiStaticURLSet);
            } else {
                //count ++
                apiTopStaticHelper.get(shardTime).analyzeMetaLog(metainfo);
            }
        }
    }

    public Map<String, Integer> getAPITOPStatics() {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Map.Entry<Long, APIStaticURLSet> ent : apiTopStaticHelper.entrySet()) {
            result.put(ent.getKey() + "", ent.getValue().getUrlSize());
        }
        return result;
    }

    public APIStaticURLSet getSharder(Long timestamp, boolean create) {
        Long shardTime = DateTimeHelper.getTimesMorning(timestamp);
        if (!apiTopStaticHelper.containsKey(shardTime)) {
            if (create) {
                APIStaticURLSet apiStaticURLSet = new APIStaticURLSet(shardTime);
                apiTopStaticHelper.put(shardTime, apiStaticURLSet);
                return apiTopStaticHelper.get(shardTime);
            }
            //default not create this one
            return null;
        } else {
            return apiTopStaticHelper.get(shardTime);
        }
    }
}
