package com.ragnar.server.storage;

import com.ragnar.server.config.FieryConfig;
import com.ragnar.server.util.DateTimeHepler;
import com.ragnar.server.util.FileUtil;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("singleton")

public class DBManage {

    private ConcurrentHashMap<Long, DBSharder> dbSharderList;

    private Logger log;

    @Autowired
    FieryConfig fieryConfig;

    public DBManage() {
        log = LoggerFactory.getLogger(DBManage.class);
        dbSharderList = new ConcurrentHashMap<>();
        log.info("DB Manger init...");
    }

    public DBSharder getDB(Long timestamp) {
        try {

            Long timeshard = DateTimeHepler.getTimesmorning(timestamp);

            //log.info("timestamp:" + timestamp + " shard:" + timeshard + " befor:" + DateTimeHepler.getBeforDay(fieryConfig.getKeepdataday()));
            if (timeshard > DateTimeHepler.getBeforDay(fieryConfig.getKeepdataday())
                    && timeshard <= DateTimeHepler.getCurrentTime()) {

                if (dbSharderList.containsKey(timeshard)) {
                    return dbSharderList.get(timeshard);
                }

                dbSharderList.put(timeshard, new DBSharder(fieryConfig.getDbpath(), timeshard));
                return dbSharderList.get(timeshard);

            } else {
                return null;
            }

        } catch (RocksDBException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }

    }

    public Map<String, String> getDbList() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<Long, DBSharder> e : dbSharderList.entrySet()) {
            String dbname = e.getKey() + "";
            DBSharder dbSharder = e.getValue();

            result.put(dbname, "");
        }
        return result;
    }

    //remove the old db
    @Scheduled(fixedRate = 5000)
    public void Refresh() {
        for (Map.Entry<Long, DBSharder> e : dbSharderList.entrySet()) {
            //check if the ragnarlog is expire
            if (e.getKey() < DateTimeHepler.getBeforDay(fieryConfig.getKeepdataday())) {
                log.info("Remove DB Name:" + e.getKey() + " expireday:" + fieryConfig.getKeepdataday() + " expiredate:" + DateTimeHepler.getBeforDay(fieryConfig.getKeepdataday()));
                e.getValue().close();
                FileUtil.deleteDir(fieryConfig.getDbpath() + "/" + e.getKey());
                dbSharderList.remove(e.getKey());
            }
        }
    }
}
