package org.weiboad.ragnar.server.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.struct.MetaLog;
import org.weiboad.ragnar.server.struct.ResponseJson;
import org.weiboad.ragnar.server.util.DateTimeHelper;
import org.weiboad.ragnar.server.util.FileUtil;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("singleton")
public class IndexSearchSharderManager {

    private Logger log;

    @Autowired
    private FieryConfig fieryConfig;

    //init
    private ConcurrentHashMap<String, Analyzer> analyzerList = new ConcurrentHashMap<>();

    //Directory
    private ConcurrentHashMap<String, FSDirectory> directorList = new ConcurrentHashMap<>();

    //reader
    private ConcurrentHashMap<String, DirectoryReader> readerList = new ConcurrentHashMap<>();

    private MultiReader allInOneReader;
    //searcher
    private IndexSearcher searcher;

    //init flag
    private int isinit = 0;

    //group searcher
    //private GroupingSearch groupsearch;

    public IndexSearchSharderManager() {
        log = LoggerFactory.getLogger(IndexSearchSharderManager.class);
    }

    public Map<String, Map<String, String>> getSearchIndexInfo() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        for (Map.Entry<String, DirectoryReader> e : readerList.entrySet()) {

            String dbname = e.getKey();
            DirectoryReader diskReader = e.getValue();

            Map<String, String> indexInfo = new LinkedHashMap<>();

            indexInfo.put("count", diskReader.numDocs() + "");

            result.put(dbname, indexInfo);
        }
        return result;
    }

    public int getIndexedDocCount() {
        if (allInOneReader != null) {
            return allInOneReader.numDocs();
        }
        return 0;
    }

    public ResponseJson searchByQuery(Long timestamp, Query query, int start, int limit, Sort sort) {

        String timeSharder = String.valueOf(DateTimeHelper.getTimesMorning(timestamp));

        ResponseJson responeJson = new ResponseJson();

        //ignore the out of date search
        if (timestamp > DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday()) && timestamp <= DateTimeHelper.getCurrentTime()) {

            //fixed the index not load
            if (!readerList.contains(timeSharder)) {
                boolean loadRet = this.openIndex(timeSharder, fieryConfig.getIndexpath()+"/"+timeSharder);
                if (!loadRet) {
                    responeJson.setCode(304);
                    responeJson.setMsg("Index not found ...");
                    return responeJson;
                }
            }

            ArrayList<MetaLog> metalist = new ArrayList<MetaLog>();

            try {
                //max 2k result
                TopDocs results = searcher.search(query, 2000, sort);

                ScoreDoc[] hits = results.scoreDocs;
                int numTotalHits = results.totalHits;

                //set result count
                responeJson.setTotalcount(numTotalHits);

                limit = Math.min(numTotalHits, limit);

                for (int i = start; i < limit; i++) {
                    Document doc = searcher.doc(hits[i].doc);
                    MetaLog metainfo = new MetaLog();
                    metainfo.init(doc);
                    metalist.add(metainfo);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                //e.printStackTrace();
            }

            responeJson.setResult(metalist);
        } else {
            responeJson.setCode(300);
            responeJson.setMsg("Index Expire ...");
            return responeJson;
        }

        return responeJson;
    }

    public ResponseJson searchIndex(Long timestamp, String field, String keyword, int start, int limit) {
        ResponseJson respone = new ResponseJson();
/*
        if (timestamp > DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday()) && timestamp <= DateTimeHelper.getCurrentTime()) {
            IndexSearchSharder indexshardobj = acquireShardIndex(timestamp);
            if (indexshardobj != null) {
                return indexshardobj.searchIndex(field, keyword, start, limit);
            } else {
                respone.setCode(300);
                respone.setMsg("Index not Found Fail...");
                return respone;
            }
        }
        */

        return respone;
    }

    public ResponseJson searchByGroup(Long timestamp, int offset, int limit) {

        ResponseJson respone = new ResponseJson();
/*
        if (timestamp > DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday()) && timestamp <= DateTimeHelper.getCurrentTime()) {
            IndexSearchSharder indexshardobj = acquireShardIndex(timestamp);
            //search the group
            if (indexshardobj != null) {
                return indexshardobj.searchByGroup(offset, limit);
            } else {
                respone.setCode(300);
                respone.setMsg("Index not Found Fail...");
                return respone;
            }
        }
                */

        return respone;
    }

    private boolean openIndex(String foldername, String folderpath) {
        try {
            Analyzer analyzer = new StandardAnalyzer();

            //diskConfig = new IndexWriterConfig(analyzer);
            //diskConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            //diskConfig.setRAMBufferSizeMB(256.0);
            //init director

            FSDirectory diskDirectory = FSDirectory.open(Paths.get(folderpath));

            //init reader
            DirectoryReader diskReader = DirectoryReader.open(diskDirectory);

            //every thing is ok
            if (analyzer != null && diskDirectory != null && diskReader != null) {
                analyzerList.put(foldername, analyzer);
                directorList.put(foldername, diskDirectory);
                readerList.put(foldername, diskReader);

                log.info("Load Index Success:" + foldername + " path:" + folderpath);
                return true;
            } else {
                log.error("Load Index Fail:" + foldername);
                return false;
            }

        } catch (org.apache.lucene.index.IndexNotFoundException xe) {
            log.error("Load Index Not Found:" + foldername);
            //throw new Exception(e.getMessage());
            //e.printStackTrace();
        } catch (Exception xxe) {
            //do nothing
            xxe.printStackTrace();
            log.error("load index Exception:" + xxe.getMessage());
            //throw new Exception(e.getMessage());
        }
        return false;
    }

    //reload searcher
    private void reloadSearch() {
        //ok load all index to searcher
        if (readerList.size() > 0) {
            try {
                DirectoryReader[] dictList = readerList.values().toArray(new DirectoryReader[readerList.size()]);
                this.allInOneReader = new MultiReader(dictList);
                searcher = new IndexSearcher(this.allInOneReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(fixedRate = 5000)
    public void refreshAllIndex() {

        /////////////////////
        //init first time
        //autowired not work on construct
        //so try this way
        /////////////////////


        //log.info("scan the index folder:" + fieryConfig.getIndexpath());

        HashMap<String, String> dictlist = new HashMap<>();

        //get the file list
        try {
            dictlist = FileUtil.subFolderList(fieryConfig.getIndexpath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //load all index if not contain on  folder
        for (Map.Entry<String, String> e : dictlist.entrySet()) {
            String foldername = e.getKey();
            String folderpath = e.getValue();
            if (!readerList.containsKey(foldername)) {
                log.info("start load index foldername:" + foldername + " abspath:" + folderpath);
                //open index
                boolean ret = this.openIndex(foldername, folderpath);

                //warning this may cause bug
                //loaded fail? clean it
                if (!ret) {
                    FileUtil.deleteDir(folderpath);
                }
            }
        }


        /////////////////////
        // recycle expire index
        /////////////////////
        for (Map.Entry<String, DirectoryReader> e : readerList.entrySet()) {
            String dbname = e.getKey();

            try {
                Long dbtime = Long.parseLong(dbname);

                if (dbtime < DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday())) {
                    //closed all
                    if (analyzerList.containsKey(dbname)) {
                        analyzerList.get(dbname).close();
                        analyzerList.remove(dbname);
                    }
                    if (directorList.containsKey(dbname)) {
                        directorList.get(dbname).close();
                        directorList.remove(dbname);
                    }
                    if (readerList.containsKey(dbname)) {
                        readerList.get(dbname).close();
                        readerList.remove(dbname);
                    }

                    //remove the folder
                    FileUtil.deleteDir(fieryConfig.getIndexpath() + "/" + dbname);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        /////////////////////
        // refresh already loaded index
        /////////////////////
        for (Map.Entry<String, DirectoryReader> e : readerList.entrySet()) {

            String dbname = e.getKey();
            DirectoryReader diskReader = e.getValue();

            try {
                Date start = new Date();

                DirectoryReader tmp = DirectoryReader.openIfChanged(diskReader);
                if (tmp != null) {
                    diskReader.close();
                    diskReader = tmp;
                    readerList.put(dbname, diskReader);
                    Date end = new Date();
                    log.info("Reload Index:" + dbname + " cost:" + (end.getTime() - start.getTime()) + " totalcount:" + diskReader.numDocs());
                }

            } catch (Exception exx) {
                exx.printStackTrace();
                log.error(exx.getMessage());
            }
        }
        /////////////////////
        //refresh the all in one searcher
        /////////////////////

        this.reloadSearch();

    }
}
