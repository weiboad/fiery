package org.weiboad.ragnar.server.search;


import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.data.MetaLog;
import org.weiboad.ragnar.server.data.ResponseJson;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.util.Map;

@Component
@Scope("singleton")
public class IndexService {

    private Logger log;

    @Autowired
    private IndexSearchSharderManager searchsharderManager;

    @Autowired
    private IndexWriterShaderManager writerSharderManager;

    @Autowired
    private FieryConfig fieryConfig;

    //init
    public IndexService() {
        log = LoggerFactory.getLogger(IndexService.class);
        log.info("Init the Index Service");
    }

    public boolean insertProcessQueue(MetaLog metalog) {
        long shardtime = DateTimeHelper.getTimesmorning(Math.round(metalog.getTime()));

        if (shardtime > DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday()) && shardtime <= DateTimeHelper.getCurrentTime()) {
            return writerSharderManager.insertProcessQueue(Long.toString(shardtime), metalog);
        } else {
            return false;
        }
    }

    public void addIndex(String dbname, MetaLog metalog) {
        writerSharderManager.addIndex(dbname, metalog);
    }

    public ResponseJson searchIndex(Long timestamp, String field, String keyword, int start, int limit) {
        //log.info("search timestamp:" + timestamp);
        return searchsharderManager.searchIndex(timestamp, field, keyword, start, limit);
    }

    public ResponseJson searchByQuery(Long timestamp, Query query, int start, int limit, Sort sort) {
        return searchsharderManager.searchByQuery(timestamp, query, start, limit, sort);
    }

    public ResponseJson searchByGroup(Long timestamp, int offset, int limit) {
        return searchsharderManager.searchByGroup(timestamp, offset, limit);
    }

    public Map<String, Map<String, String>> getWriteIndexInfo() {
        return writerSharderManager.getWriteIndexInfo();
    }

    public Map<String, Map<String, String>> getSearchIndexInfo() {
        return searchsharderManager.getSearchIndexInfo();
    }

    public int getIndexedDocCount() {
        return searchsharderManager.getIndexedDocCount();
    }

}