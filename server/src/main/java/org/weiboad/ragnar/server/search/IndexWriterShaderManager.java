package org.weiboad.ragnar.server.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.weiboad.ragnar.server.config.FieryConfig;
import org.weiboad.ragnar.server.struct.MetaLog;
import org.weiboad.ragnar.server.util.DateTimeHelper;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope("singleton")
public class IndexWriterShaderManager {

    private Logger log;

    //init
    private HashMap<String, Analyzer> analyzerList;

    //disk write config
    private HashMap<String, IndexWriterConfig> diskConfigList;

    //Directory
    private HashMap<String, FSDirectory> diskDirectoryList;

    //writer
    private HashMap<String, IndexWriter> diskWriterList;

    //init the struct queue
    private HashMap<String, ConcurrentLinkedQueue<MetaLog>> IndexInputQueueList;

    @Autowired
    private FieryConfig fieryConfig;

    //init
    public IndexWriterShaderManager() {

        log = LoggerFactory.getLogger(IndexWriterShaderManager.class);
        analyzerList = new HashMap<String, Analyzer>();
        diskConfigList = new HashMap<String, IndexWriterConfig>();
        diskDirectoryList = new HashMap<String, FSDirectory>();
        diskWriterList = new HashMap<String, IndexWriter>();
        IndexInputQueueList = new HashMap<String, ConcurrentLinkedQueue<MetaLog>>();
    }

    public synchronized void CheckTheWriterIndex(String dbname) {

        //existed will not create again
        if (analyzerList.containsKey(dbname) || diskConfigList.containsKey(dbname) ||
                diskDirectoryList.containsKey(dbname) || diskWriterList.containsKey(dbname) ||
                IndexInputQueueList.containsKey(dbname)) {
            return;
        }
        log.info("Create Write Index:" + dbname);

        analyzerList.put(dbname, new SmartChineseAnalyzer());
        diskConfigList.put(dbname, new IndexWriterConfig(analyzerList.get(dbname)));
        diskConfigList.get(dbname).setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        //diskConfig.setRAMBufferSizeMB(256.0);

        IndexInputQueueList.put(dbname, new ConcurrentLinkedQueue<MetaLog>());

        try {
            //init director
            diskDirectoryList.put(dbname, FSDirectory.open(Paths.get(fieryConfig.getIndexpath() + "/" + dbname)));

            //init writer
            diskWriterList.put(dbname, new IndexWriter(diskDirectoryList.get(dbname), diskConfigList.get(dbname)));

            //init the folder
            diskWriterList.get(dbname).commit();

        } catch (Exception e) {

            if (analyzerList.containsKey(dbname)) {
                analyzerList.remove(dbname);
            }

            if (diskConfigList.containsKey(dbname)) {
                diskConfigList.remove(dbname);
            }

            if (diskDirectoryList.containsKey(dbname)) {
                diskDirectoryList.remove(dbname);
            }

            if (diskWriterList.containsKey(dbname)) {
                diskWriterList.remove(dbname);
            }

            if (IndexInputQueueList.containsKey(dbname)) {
                IndexInputQueueList.remove(dbname);
            }

            //do nothing
            e.printStackTrace();
            log.error("init Exception:" + e.getMessage());
        }
    }

    public boolean insertProcessQueue(String dbname, MetaLog metalog) {
        //check the db
        this.CheckTheWriterIndex(dbname);

        if (metalog != null && IndexInputQueueList.get(dbname) != null) {
            return IndexInputQueueList.get(dbname).add(metalog);
        }
        return false;
    }

    public void addIndex(String dbname, MetaLog metalog) {
        //check the db
        this.CheckTheWriterIndex(dbname);

        try {
            Document metainfo = metalog.gDoc();
            diskWriterList.get(dbname).addDocument(metainfo);

        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Map<String, String>> getWriteIndexInfo() {

        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        for (Map.Entry<String, IndexWriter> e : diskWriterList.entrySet()) {
            Map<String, String> indexInfo = new LinkedHashMap<>();

            String dbname = e.getKey();
            IndexWriter indexWriter = e.getValue();

            indexInfo.put("count", indexWriter.numDocs() + "");
            indexInfo.put("count_ram", indexWriter.numRamDocs() + "");
            indexInfo.put("memory_bytes", indexWriter.ramBytesUsed() + "");
            indexInfo.put("insertqueue_len", IndexInputQueueList.get(dbname).size() + "");

            result.put(dbname, indexInfo);
        }
        return result;
    }

    //commit the index change
    public boolean commitChange(String dbname) {
        try {
            //dump index
            diskWriterList.get(dbname).commit();

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 5000)
    public void refreshIndex() {
        if (diskWriterList == null || diskWriterList.size() == 0) {
            return;
        }
        Iterator<Map.Entry<String, IndexWriter>> iter = diskWriterList.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, IndexWriter> entry = iter.next();
            String dbname = (String) entry.getKey();

            //check the ragnarlog is time to remove?
            Long dbtime = Long.parseLong(dbname);
            if (dbtime < DateTimeHelper.getBeforeDay(fieryConfig.getKeepdataday())) {
                log.info("Remove the Old Writer Index:" + dbname);

                analyzerList.get(dbname).close();
                analyzerList.remove(dbname);
                diskConfigList.remove(dbname);

                //clean up
                try {
                    diskWriterList.get(dbname).deleteAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }

                //close writer
                try {
                    diskWriterList.get(dbname).close();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }

                //close reader
                try {
                    diskDirectoryList.get(dbname).close();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }

                //remove from the list
                diskWriterList.remove(dbname);
                diskDirectoryList.remove(dbname);
                IndexInputQueueList.remove(dbname);
                continue;
            }

            while (true) {
                MetaLog metainfo = IndexInputQueueList.get(dbname).poll();
                if (metainfo != null) {
                    addIndex(dbname, metainfo);
                } else {
                    //empty will continue the ragnarlog
                    break;
                }
            }

            //Date start = new Date();

            this.commitChange(dbname);

            //Date end = new Date();
            //log.info("Write Index:" + dbname + " cost:" + (end.getTime() - start.getTime() + " docs:" + diskWriterList.get(dbname).numDocs()));
        }
    }

}
