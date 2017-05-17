package com.ragnar.server.search;


import com.ragnar.server.data.MetaLog;
import com.ragnar.server.data.responseJson;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

public class IndexSearchSharder {

    private Logger log;

    //db name
    private String dbname;

    //init
    private Analyzer analyzer;
    //private IndexWriterConfig diskConfig;

    //Directory
    private FSDirectory diskDirectory;

    //reader
    private DirectoryReader diskReader;

    //searcher
    private IndexSearcher searcher;

    private GroupingSearch groupsearch;

    //init
    public IndexSearchSharder(String indexpath, String dbname) throws Exception {
        //init group search obj
        groupsearch = new GroupingSearch("urlraw");
        groupsearch.setCachingInMB(4.0, true);
        groupsearch.setAllGroups(true);

        //init the search obj
        this.dbname = dbname;

        log = LoggerFactory.getLogger(IndexSearchSharder.class);

        analyzer = new StandardAnalyzer();

        //diskConfig = new IndexWriterConfig(analyzer);
        //diskConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        //diskConfig.setRAMBufferSizeMB(256.0);

        try {
            //init director
            diskDirectory = FSDirectory.open(Paths.get(indexpath + "/" + dbname));

            //init reader
            diskReader = DirectoryReader.open(diskDirectory);

            searcher = new IndexSearcher(diskReader);
        } catch (org.apache.lucene.index.IndexNotFoundException e) {
            log.error("Load Index Not Found:" + dbname);
            throw new Exception(e.getMessage());
            //e.printStackTrace();
        } catch (Exception e) {
            //do nothing
            e.printStackTrace();
            log.error("init Exception:" + e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    public void refreshIndex() {

        Date start = new Date();
        try {
            DirectoryReader tmp = diskReader.openIfChanged(diskReader);
            if (tmp != null) {
                diskReader.close();
                diskReader = tmp;
                searcher = new IndexSearcher(diskReader);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        Date end = new Date();
        log.info("Reload Index:" + dbname + " cost:" + (end.getTime() - start.getTime()) + " totalcount:" + diskReader.numDocs());

    }

    public void close() {

    }

    public responseJson searchByGroup(int offset, int limit) {
        responseJson result = new responseJson();
        ArrayList<MetaLog> metalist = new ArrayList<MetaLog>();

        Query query = new MatchAllDocsQuery();

        TopGroups<BytesRef> searchresult;
        try {
            searchresult = groupsearch.search(searcher, query, offset, limit);

            result.setTotalcount(searchresult.totalGroupCount);

            for (GroupDocs<BytesRef> groupDocs : searchresult.groups) {
                //System.out.println("分组：" + groupDocs.groupValue.utf8ToString());
                //System.out.println("组内记录：" + groupDocs.totalHits);

                //System.out.println("groupDocs.scoreDocs.length:" + groupDocs.scoreDocs.length);
                for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
                    //System.out.println(searcher.doc(scoreDoc.doc));
                    Document doc = searcher.doc(scoreDoc.doc);
                    MetaLog metainfo = new MetaLog();
                    metainfo.init(doc);
                    metalist.add(metainfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        result.setResult(metalist);

        return result;
    }

    /*
        public responseJson searchByQuery(Query query, int start, int limit, Sort sort) {
            responseJson result = new responseJson();
            ArrayList<metaLog> metalist = new ArrayList<metaLog>();

            try {
                //max 2k result
                TopDocs results = searcher.search(query, 2000, sort);

                ScoreDoc[] hits = results.scoreDocs;
                int numTotalHits = results.totalHits;

                //set result count
                result.setTotalcount(numTotalHits);

                limit = Math.min(numTotalHits, limit);

                for (int i = start; i < limit; i++) {

                    Document doc = searcher.doc(hits[i].doc);
                    metaLog metainfo = new metaLog();
                    metainfo.init(doc);
                    metalist.add(metainfo);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }

            result.setResult(metalist);
            return result;
        }
    */
    public responseJson searchIndex(String field, String keyword, int start, int limit) {
        QueryParser parser = new QueryParser(field, analyzer);
        Query query;

        ArrayList<MetaLog> metalist = new ArrayList<MetaLog>();
        responseJson result = new responseJson();

        try {
            query = parser.parse(keyword);

            TopDocs results = searcher.search(query, limit);

            ScoreDoc[] hits = results.scoreDocs;

            int numTotalHits = results.totalHits;

            //set result count
            result.setTotalcount(numTotalHits);

            limit = Math.min(numTotalHits, limit);

            for (int i = start; i < limit; i++) {

                Document doc = searcher.doc(hits[i].doc);
                MetaLog metainfo = new MetaLog();
                metainfo.init(doc);
                metalist.add(metainfo);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        result.setResult(metalist);
        return result;
    }
}
