package org.weiboad.ragnar.server.storage;

import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.util.DateTimeHelper;
import org.weiboad.ragnar.server.util.FileUtil;

import java.util.HashMap;
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
        log.info("DB Manager init...");
    }

    public DBSharder getDB(Long timestamp) {
        try {

            Long timeshard = DateTimeHelper.getTimesMorning(timestamp);

            //log.info("timestamp:" + timestamp + " shard:" + timeshard + " befor:" + DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday()));
            if (timeshard > DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday())
                    && timeshard <= DateTimeHelper.getCurrentTime()) {

                if (dbSharderList.containsKey(timeshard)) {
                    return dbSharderList.get(timeshard);
                }

                dbSharderList.put(timeshard, new DBSharder(fieryConfig.getDbpath(), timeshard));
                return dbSharderList.get(timeshard);

            } else {
                log.info("out of the date:" + timeshard);
                return null;
            }

        } catch (RocksDBException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }

    }


    public HashMap<String, String> getDBFolderList() {
        HashMap<String, String> dblist = new HashMap<>();
        try {
            dblist = FileUtil.subFolderList(fieryConfig.getDbpath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dblist;
    }


    //remove the old db
    @Scheduled(fixedRate = 5000)
    public void Refresh() {
        for (Map.Entry<Long, DBSharder> e : dbSharderList.entrySet()) {
            //check if the ragnarlog is expire
            if (e.getKey() < DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday())) {
                log.info("Remove DB Name:" + e.getKey() + " expireday:" + fieryConfig.getKeepdataday() + " expiredate:" + DateTimeHelper
                        .getBeforeDay(fieryConfig.getKeepdataday()));
                e.getValue().close();
                FileUtil.deleteDir(fieryConfig.getDbpath() + "/" + e.getKey());
                dbSharderList.remove(e.getKey());
            }
        }
    }
}
