package org.weiboad.ragnar.server.statistics.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.storage.DBManage;
import org.weiboad.ragnar.server.storage.DBSharder;
import org.weiboad.ragnar.server.struct.MetaLog;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("singleton")
public class APIStatisticTimeSet {

    private ConcurrentHashMap<Long, ConcurrentHashMap<String, APIStatisticStruct>> apiTopStaticHelper = new ConcurrentHashMap<Long, ConcurrentHashMap<String, APIStatisticStruct>>();
    private ConcurrentHashMap<String, ConcurrentHashMap<Long, APIStatisticStruct>> apiTopHourStaticHelper = new ConcurrentHashMap<>();

    private Logger log = LoggerFactory.getLogger(APIStatisticTimeSet.class);

    @Autowired
    private FieryConfig fieryConfig;

    @Autowired
    private DBManage dbManage;

    public void analyzeMetaLog(MetaLog metainfo) {

        String url = metainfo.getUrl();
        Long shardTime = DateTimeHelper.getTimesMorning(metainfo.getTime().longValue());
        Long hourShardTime = DateTimeHelper.getHourTime(metainfo.getTime().longValue());

        if (url.trim().length() > 0 && shardTime > 0 && shardTime > DateTimeHelper.getCurrentTime() -
                (fieryConfig.getKeepdataday() * 86400)) {

            //day
            if (!apiTopStaticHelper.containsKey(shardTime)) {
                //day shard
                ConcurrentHashMap<String, APIStatisticStruct> urlshard = new ConcurrentHashMap<>();
                APIStatisticStruct urlInfo = new APIStatisticStruct(metainfo);
                urlshard.put(url, urlInfo);
                apiTopStaticHelper.put(shardTime, urlshard);

            } else {
                //count ++
                if (!apiTopStaticHelper.get(shardTime).containsKey(url)) {
                    //day
                    APIStatisticStruct apiStruct = new APIStatisticStruct(metainfo);
                    apiTopStaticHelper.get(shardTime).put(metainfo.getUrl(), apiStruct);

                } else {
                    //day
                    apiTopStaticHelper.get(shardTime).get(metainfo.getUrl()).analyzeMetaLog(metainfo);

                }
            }

            //hour
            if (!apiTopHourStaticHelper.containsKey(url)) {
                //hour shard
                ConcurrentHashMap<Long, APIStatisticStruct> urlHourshard = new ConcurrentHashMap<>();
                APIStatisticStruct urlHourInfo = new APIStatisticStruct(metainfo);
                urlHourshard.put(hourShardTime, urlHourInfo);
                apiTopHourStaticHelper.put(url, urlHourshard);
            } else {
                if (!apiTopHourStaticHelper.get(url).containsKey(hourShardTime)) {
                    //hour
                    APIStatisticStruct apiHourStruct = new APIStatisticStruct(metainfo);
                    apiTopHourStaticHelper.get(metainfo.getUrl()).put(hourShardTime, apiHourStruct);
                } else {
                    //hour
                    apiTopHourStaticHelper.get(metainfo.getUrl()).get(hourShardTime).analyzeMetaLog(metainfo);
                }
            }

        }//check the time

    }

    public Map<String, Integer> getAPITOPStatics() {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (Map.Entry<Long, ConcurrentHashMap<String, APIStatisticStruct>> ent : apiTopStaticHelper.entrySet()) {
            result.put(ent.getKey() + "", ent.getValue().size());
        }
        return result;
    }

    public ConcurrentHashMap<String, APIStatisticStruct> getDaySharder(Long timestamp, boolean create) {
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

    @PostConstruct
    public void loadStaticDb() {
        log.info("load the Statistic info start...");
        Gson jsonHelper = new Gson();

        Map<String, String> dblist = dbManage.getDBFolderList();
        for (Map.Entry<String, String> db : dblist.entrySet()) {
            String dbshard = db.getKey();
            Long dbShardLong;

            //prevent the shard name is not long
            try {
                dbShardLong = Long.valueOf(dbshard);
            } catch (Exception e) {
                continue;
            }

            //init the set
            ConcurrentHashMap<String, APIStatisticStruct> apiStatisticStructMap = new ConcurrentHashMap<>();
            apiTopStaticHelper.put(dbShardLong, apiStatisticStructMap);

            try {
                DBSharder dbHelper = dbManage.getDB(dbShardLong);

                if (dbHelper == null) {
                    log.info("load db fail:" + dbshard);
                    continue;
                }

                String staticStr = dbHelper.get("apitopstatistic");

                if (staticStr == null) {
                    log.info("load static db info fail:" + dbshard);
                    continue;
                }

                //recovery the statics
                String[] staticArray = staticStr.split("\r\n");
                for (int staticIndex = 0; staticIndex < staticArray.length; staticIndex++) {
                    try {
                        APIStatisticStruct apiStatisticStruct = jsonHelper.fromJson(staticArray[staticIndex], APIStatisticStruct.class);
                        apiTopStaticHelper.get(dbShardLong).put(apiStatisticStruct.getUrl(), apiStatisticStruct);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

        }
    }

    @PreDestroy
    public void dumpStaticDb() {
        //log.info("dump the Statistic info start...");

        for (Map.Entry<Long, ConcurrentHashMap<String, APIStatisticStruct>> ent : apiTopStaticHelper.entrySet()) {
            String staticSting = "";
            Long shardTime = ent.getKey();
            ConcurrentHashMap<String, APIStatisticStruct> apiStatisticStructMap = ent.getValue();

            //fetch all statics
            for (Map.Entry<String, APIStatisticStruct> urlShard : apiStatisticStructMap.entrySet()) {
                String jsonStr = urlShard.getValue().toJson();
                if (jsonStr.trim().length() > 0) {
                    staticSting += (jsonStr + "\r\n");
                }
            }

            //log.info("dump the Statistic info:" + shardTime + " count:" + apiStatisticStructMap.size());

            DBSharder dbSharder = dbManage.getDB(shardTime);
            if (staticSting.length() > 0 && dbSharder != null) {
                dbSharder.put("apitopstatistic", staticSting);
            }
        }
    }

    @Scheduled(fixedRate = 30 * 1000)
    public void cleanUpSharder() {

        //clean up day
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

        //clean up hour
        if (apiTopHourStaticHelper.size() > 0) {
            ArrayList<String> removeUrlMap = new ArrayList<>();

            for (Map.Entry<String, ConcurrentHashMap<Long, APIStatisticStruct>> ent : apiTopHourStaticHelper.entrySet()) {
                ArrayList<Long> removeMap = new ArrayList<>();

                for (Map.Entry<Long, APIStatisticStruct> itemEnt : ent.getValue().entrySet()) {
                    if (itemEnt.getKey() >= DateTimeHelper.getCurrentTime() - fieryConfig.getKeepdataday() * 86400) {
                        continue;
                    }
                    removeMap.add(itemEnt.getKey());
                }

                for (Long removeKey : removeMap) {
                    log.info("Clean up the API Top Day Statistic:" + removeKey);
                    apiTopHourStaticHelper.get(ent.getKey()).remove(removeKey);
                    if (apiTopHourStaticHelper.get(ent.getKey()).size() == 0) {
                        removeUrlMap.add(ent.getKey());
                    }
                }
            }

            //remove the url
            for (String removeUrlKey : removeUrlMap) {
                apiTopHourStaticHelper.remove(removeUrlKey);
            }

        }

        //cycle dump the statistics
        dumpStaticDb();

    }
}
