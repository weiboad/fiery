package com.ragnar.server.contorller.search;

import com.ragnar.server.contorller.ragnarlog.PutMetalog;
import com.ragnar.server.data.responseJson;
import com.ragnar.server.search.IndexService;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Recent {

    @Autowired
    IndexService indexHelper;

    Logger log = LoggerFactory.getLogger(PutMetalog.class);

    @RequestMapping(value = "/recent", method = RequestMethod.POST)
    @ResponseBody
    public responseJson RecentRequest(@RequestParam(value = "timestamp", required = false) String timetamp,
                                      @RequestParam(value = "startpos", required = false) String startpos,
                                      @RequestParam(value = "limit", required = false) String limit) {

        if (timetamp == null || timetamp.length() == 0) {
            responseJson result = new responseJson();
            result.setCode(400);
            result.setMsg("Plasee set the timestamp paramter!");
            return result;
        }

        long timestamplong;

        try {
            timestamplong = Integer.parseInt(timetamp);
        } catch (Exception e) {
            responseJson result = new responseJson();
            result.setCode(400);
            result.setMsg("Plasee set the timestamp paramter for the long");
            return result;
        }

        int start;
        int datasize;

        try {
            start = Integer.parseInt(startpos);
        } catch (Exception e) {
            start = 0;
        }

        try {
            datasize = Integer.parseInt(limit);
        } catch (Exception e) {
            datasize = 20;
        }
        Sort sort = new Sort(new SortField("time", SortField.Type.DOUBLE, true));
        //QueryParser parser = new QueryParser("*", new StandardAnalyzer());
        Query query;

        query = new MatchAllDocsQuery();
        responseJson result = indexHelper.searchByQuery(timestamplong, query, start, datasize, sort);
        return result;
    }
}
