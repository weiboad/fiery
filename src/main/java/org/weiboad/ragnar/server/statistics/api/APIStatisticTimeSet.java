package org.weiboad.ragnar.server.statistics.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.struct.MetaLog;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("singleton")
public class APIStatisticTimeSet {

    private ConcurrentHashMap<Long, ConcurrentHashMap<String, APIStatisticStruct>> apiTopStaticHelper = new ConcurrentHashMap<Long, ConcurrentHashMap<String, APIStatisticStruct>>();

    private Logger log = LoggerFactory.getLogger(APIStatisticTimeSet.class);

    @Autowired
    FieryConfig fieryConfig;

    public void analyzeMetaLog(MetaLog metainfo) {

        String url = metainfo.getUrl();
        Long shardTime = metainfo.getTime().longValue();

        if (url.trim().length() > 0 && shardTime > 0 && shardTime > DateTimeHelper.getCurrentTime() -
                (fieryConfig.getKeepdataday() * 86400)) {

            shardTime = DateTimeHelper.getTimesMorning(shardTime);

            if (!apiTopStaticHelper.containsKey(shardTime)) {
                ConcurrentHashMap<String, APIStatisticStruct> urlshard = new ConcurrentHashMap<>();

                //prepare the init struct
                APIStatisticStruct urlinfo = new APIStatisticStruct(metainfo);

                //put to the list
                urlshard.put(url, urlinfo);
                apiTopStaticHelper.put(shardTime, urlshard);

            } else {
                //count ++
                if (!apiTopStaticHelper.get(shardTime).containsKey(url)) {
                    APIStatisticStruct apiStruct = new APIStatisticStruct(metainfo);
                    apiTopStaticHelper.get(shardTime).put(metainfo.getUrl(), apiStruct);
                } else {
                    apiTopStaticHelper.get(shardTime).get(metainfo.getUrl()).analyzeMetaLog(metainfo);
                }
            }
        }

    }

    public Map<String, Integer> getAPITOPStatics() {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Map.Entry<Long, ConcurrentHashMap<String, APIStatisticStruct>> ent : apiTopStaticHelper.entrySet()) {
            result.put(ent.getKey() + "", ent.getValue().size());
        }
        return result;
    }

    public ConcurrentHashMap<String, APIStatisticStruct> getSharder(Long timestamp, boolean create) {
        Long shardTime = DateTimeHelper.getTimesMorning(timestamp);
        if (!apiTopStaticHelper.containsKey(shardTime)) {
            if (create) {
                ConcurrentHashMap<String, APIStatisticStruct> urlshard = new ConcurrentHashMap<>();
                apiTopStaticHelper.put(shardTime, urlshard);
                return urlshard;
            }
            //default not create this one
            return null;
        } else {
            return apiTopStaticHelper.get(shardTime);
        }
    }

    @Scheduled(fixedRate = 30 * 1000)
    public void cleanUpSharder() {
        if (apiTopStaticHelper.size() > 0) {
            ArrayList<Long> removeMap = new ArrayList<>();

            for (Map.Entry<Long, ConcurrentHashMap<String, APIStatisticStruct>> ent : apiTopStaticHelper.entrySet()) {
                if (ent.getKey() >= DateTimeHelper.getCurrentTime() - fieryConfig.getKeepdataday() * 86400) {
                    continue;
                }
                removeMap.add(ent.getKey());
            }

            for (Long removeKey : removeMap) {
                log.info("Clean up the API Top Statistic:" + removeKey);
                apiTopStaticHelper.remove(removeKey);
            }
        }
    }
}
